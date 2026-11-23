package com.tfmhntspn.subguard.repositories;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import com.tfmhntspn.subguard.utils.PublicIdGenerator;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {

    private final DatabaseReference usersRef;
    private final DatabaseReference publicIdsRef;

    public UserRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        publicIdsRef = database.getReference("publicIds");
    }


    public interface PublicIdCallback {
        void onSuccess(String publicId);
        void onFailure(String error);
    }


    public interface SaveProfileCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public void saveUserProfile(
            FirebaseUser user,
            String name,
            String email,
            SaveProfileCallback callback
    ) {

        if (user == null) {
            callback.onFailure("No authenticated user found.");
            return;
        }

        String uid = user.getUid();
        DatabaseReference userRef = usersRef.child(uid);

        userRef.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        Map<String, Object> updates = new HashMap<>();

                        // New profile: save name and email
                        if (!snapshot.exists()) {

                            updates.put(
                                    "name",
                                    name != null ? name : ""
                            );

                            updates.put(
                                    "email",
                                    email != null ? email : ""
                            );

                        } else {

                            // Preserve existing display name
                            if (!snapshot.hasChild("name")) {
                                updates.put(
                                        "name",
                                        name != null ? name : ""
                                );
                            }

                            // Synchronize email
                            if (email != null &&
                                    !email.equals(
                                            snapshot.child("email")
                                                    .getValue(String.class))) {

                                updates.put("email", email);
                            }
                        }

                        String existingId = snapshot
                                .child("publicId")
                                .getValue(String.class);

                        // Existing users keep their Public ID
                        if (existingId != null &&
                                !existingId.isEmpty()) {

                            saveUpdates(userRef, updates, callback);
                            return;
                        }

                        // No Public ID: generate and reserve one
                        reserveUniquePublicId(
                                uid,
                                0,
                                new PublicIdCallback() {

                                    @Override
                                    public void onSuccess(String publicId) {

                                        updates.put("publicId", publicId);

                                        saveUpdates(
                                                userRef,
                                                updates,
                                                callback
                                        );
                                    }

                                    @Override
                                    public void onFailure(String error) {

                                        callback.onFailure(error);
                                    }
                                }
                        );
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        callback.onFailure(error.getMessage());
                    }
                }
        );
    }


    private void saveUpdates(
            DatabaseReference userRef,
            Map<String, Object> updates,
            SaveProfileCallback callback
    ) {

        if (updates.isEmpty()) {
            callback.onSuccess();
            return;
        }

        userRef.updateChildren(updates)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage())
                );
    }




    private void reserveUniquePublicId(
            String uid,
            int attempts,
            PublicIdCallback callback
    ) {

        // Prevent unlimited retries
        if (attempts >= 10) {
            callback.onFailure(
                    "Unable to generate a unique ID. Please try again."
            );
            return;
        }

        // Generate a candidate ID, such as SG-2M9D04
        String candidateId =
                PublicIdGenerator.generatePublicId();

        DatabaseReference idRef =
                publicIdsRef.child(candidateId);

        idRef.runTransaction(new Transaction.Handler() {

            @NonNull
            @Override
            public Transaction.Result doTransaction(
                    @NonNull MutableData currentData
            ) {

                String existingUid =
                        currentData.getValue(String.class);

                // ID is available: reserve it
                if (existingUid == null) {
                    currentData.setValue(uid);
                    return Transaction.success(currentData);
                }

                // The same user already owns this ID
                if (existingUid.equals(uid)) {
                    return Transaction.success(currentData);
                }

                // ID belongs to someone else
                return Transaction.abort();
            }

            @Override
            public void onComplete(
                    DatabaseError error,
                    boolean committed,
                    DataSnapshot snapshot
            ) {

                // Firebase operation failed
                if (error != null) {
                    callback.onFailure(error.getMessage());
                    return;
                }

                if (committed) {

                    // Successfully reserved
                    callback.onSuccess(candidateId);

                } else {

                    // ID was taken; try another
                    reserveUniquePublicId(
                            uid,
                            attempts + 1,
                            callback
                    );
                }
            }
        });
    }

}
