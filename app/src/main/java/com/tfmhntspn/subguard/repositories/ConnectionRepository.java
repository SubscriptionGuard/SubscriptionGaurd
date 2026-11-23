package com.tfmhntspn.subguard.repositories;

import com.tfmhntspn.subguard.models.Connection;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class ConnectionRepository {

    private final DatabaseReference requestsRef;

    public ConnectionRepository() {
        requestsRef = FirebaseDatabase.getInstance()
                .getReference("connectionRequests");
    }

    public interface RequestCallback {
        void onSuccess(String requestId);
        void onFailure(String error);
    }


    public interface LoadRequestsCallback {
        void onSuccess(List<Connection> requests);
        void onFailure(String error);
    }


    public void sendConnectionRequest(
            String senderUid,
            String receiverUid,
            RequestCallback callback
    ) {

        if (senderUid == null || senderUid.isEmpty()
                || receiverUid == null || receiverUid.isEmpty()) {

            callback.onFailure("Invalid user information.");
            return;
        }

        // Prevent users from connecting with themselves
        if (senderUid.equals(receiverUid)) {
            callback.onFailure(
                    "You cannot send a connection request to yourself."
            );
            return;
        }

        // Generate a unique request ID
        DatabaseReference newRequestRef = requestsRef.push();

        String requestId = newRequestRef.getKey();

        if (requestId == null) {
            callback.onFailure("Could not generate request ID.");
            return;
        }

        // Prepare the request
        Map<String, Object> requestData = new HashMap<>();

        requestData.put("senderId", senderUid);
        requestData.put("receiverId", receiverUid);
        requestData.put("status", "pending");
        requestData.put("createdAt", ServerValue.TIMESTAMP);

        // Save to Firebase
        newRequestRef.setValue(requestData)
                .addOnSuccessListener(unused ->
                        callback.onSuccess(requestId)
                )
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage())
                );
    }


    public void getSentRequests(
            String currentUid,
            LoadRequestsCallback callback
    ) {

        if (currentUid == null || currentUid.isEmpty()) {
            callback.onFailure("User is not signed in.");
            return;
        }

        // Find requests sent by the current user
        Query query = requestsRef
                .orderByChild("senderId")
                .equalTo(currentUid);

        query.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot
                    ) {

                        List<Connection> requests =
                                new ArrayList<>();

                        for (DataSnapshot requestSnapshot
                                : snapshot.getChildren()) {

                            String requestId =
                                    requestSnapshot.getKey();

                            String senderId = requestSnapshot
                                    .child("senderId")
                                    .getValue(String.class);

                            String receiverId = requestSnapshot
                                    .child("receiverId")
                                    .getValue(String.class);

                            String status = requestSnapshot
                                    .child("status")
                                    .getValue(String.class);

                            if (requestId == null ||
                                    senderId == null ||
                                    receiverId == null ||
                                    status == null) {
                                continue;
                            }

                            // Show pending requests only
                            if (!status.equals("pending")) {
                                continue;
                            }

                            requests.add(
                                    new Connection(
                                            requestId,
                                            senderId,
                                            receiverId,
                                            status
                                    )
                            );
                        }

                        callback.onSuccess(requests);
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error
                    ) {
                        callback.onFailure(error.getMessage());
                    }
                }
        );
    }


    public void cancelConnectionRequest(
            String requestId,
            String currentUid,
            RequestCallback callback
    ) {

        if (requestId == null || requestId.isEmpty()
                || currentUid == null || currentUid.isEmpty()) {

            callback.onFailure("Invalid request information.");
            return;
        }

        DatabaseReference requestRef =
                requestsRef.child(requestId);

        // Remove the request from Firebase.
        // Security rules verify that the signed-in user
        // is the sender and the request is still pending.
        requestRef.removeValue()
                .addOnSuccessListener(unused ->
                        callback.onSuccess(requestId)
                )
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage())
                );
    }


}
