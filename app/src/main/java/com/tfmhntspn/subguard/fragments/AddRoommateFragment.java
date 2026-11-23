package com.tfmhntspn.subguard.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.tfmhntspn.subguard.R;
import com.tfmhntspn.subguard.databinding.FragmentAddRoommateBinding;
import com.tfmhntspn.subguard.adapters.ConnectionRequestAdapter;
import com.tfmhntspn.subguard.repositories.ConnectionRepository;
import com.tfmhntspn.subguard.models.Connection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.List;

public class AddRoommateFragment extends Fragment {

    private FragmentAddRoommateBinding binding;
    private ConnectionRequestAdapter requestAdapter;
    private ConnectionRepository connectionRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddRoommateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        connectionRepository = new ConnectionRepository();

        binding.rvSentRequests.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );


        requestAdapter = new ConnectionRequestAdapter(
                new HashMap<>(),
                connection -> {

                    FirebaseUser currentUser =
                            FirebaseAuth.getInstance().getCurrentUser();

                    if (currentUser == null) {
                        Toast.makeText(
                                requireContext(),
                                "Please sign in first.",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    connectionRepository.cancelConnectionRequest(
                            connection.getConnectionId(),
                            currentUser.getUid(),
                            new ConnectionRepository.RequestCallback() {

                                @Override
                                public void onSuccess(String requestId) {

                                    if (binding == null || !isAdded()) return;

                                    Toast.makeText(
                                            requireContext(),
                                            "Connection request cancelled.",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    loadSentRequests(
                                            requestAdapter,
                                            connectionRepository
                                    );
                                }

                                @Override
                                public void onFailure(String error) {

                                    if (binding == null || !isAdded()) return;

                                    Toast.makeText(
                                            requireContext(),
                                            "Unable to cancel request: " + error,
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            }
                    );
                }
        );


        binding.rvSentRequests.setAdapter(requestAdapter);
        updateEmptyState(requestAdapter);

        loadYourPublicId();

        loadSentRequests(requestAdapter, connectionRepository);


        binding.btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());


        binding.btnSendRequest.setOnClickListener(v -> {

            String id = binding.etConnectionId.getText() != null
                    ? binding.etConnectionId.getText()
                    .toString()
                    .trim()
                    .toUpperCase(Locale.ROOT)
                    : "";

            // Check for empty input
            if (id.isEmpty()) {
                binding.etConnectionId.setError(
                        "Please enter a SubscriptionGuard ID"
                );
                return;
            }

            // Validate ID format
            if (!id.matches("SG-[A-Z0-9]{6}")) {
                binding.etConnectionId.setError(
                        "Use the format SG-XXXXXX"
                );
                return;
            }

            binding.etConnectionId.setError(null);

            FirebaseUser currentUser =
                    FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser == null) {
                Toast.makeText(
                        requireContext(),
                        "Please sign in first.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // Look up the public ID in Firebase
            DatabaseReference lookupRef =
                    FirebaseDatabase.getInstance()
                            .getReference("publicIds")
                            .child(id);

            binding.btnSendRequest.setEnabled(false);

            lookupRef.addListenerForSingleValueEvent(
                    new ValueEventListener() {

                        @Override
                        public void onDataChange(
                                @NonNull DataSnapshot snapshot) {

                            if (binding == null) return;

                            binding.btnSendRequest.setEnabled(true);

                            String targetUid =
                                    snapshot.getValue(String.class);

                            // No user has this public ID
                            if (targetUid == null) {

                                binding.etConnectionId.setError(
                                        "No user found with this ID"
                                );
                                return;
                            }

                            // Prevent connecting with yourself
                            if (targetUid.equals(currentUser.getUid())) {

                                binding.etConnectionId.setError(
                                        "You cannot connect with yourself"
                                );
                                return;
                            }


                            // User exists: send a real connection request
                            binding.btnSendRequest.setEnabled(false);

                            connectionRepository.sendConnectionRequest(
                                    currentUser.getUid(),
                                    targetUid,
                                    new ConnectionRepository.RequestCallback() {

                                        @Override
                                        public void onSuccess(String requestId) {

                                            if (binding == null || !isAdded()) return;

                                            binding.btnSendRequest.setEnabled(true);

                                            Toast.makeText(
                                                    requireContext(),
                                                    "Connection request sent!",
                                                    Toast.LENGTH_SHORT
                                            ).show();

                                            // Clear input after successful saving
                                            binding.etConnectionId.setText("");
                                            loadSentRequests(requestAdapter, connectionRepository);
                                        }

                                        @Override
                                        public void onFailure(String error) {

                                            if (binding == null || !isAdded()) return;

                                            binding.btnSendRequest.setEnabled(true);

                                            Toast.makeText(
                                                    requireContext(),
                                                    "Could not send request: " + error,
                                                    Toast.LENGTH_LONG
                                            ).show();
                                        }
                                    }
                            );

                        }

                        @Override
                        public void onCancelled(
                                @NonNull DatabaseError error) {

                            if (binding == null) return;

                            binding.btnSendRequest.setEnabled(true);

                            Toast.makeText(
                                    requireContext(),
                                    "Lookup failed: " + error.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );
        });

    }

    private void updateEmptyState(ConnectionRequestAdapter adapter) {

        boolean isEmpty = adapter.getItemCount() == 0;

        binding.tvNoSentRequests.setVisibility(
                isEmpty ? View.VISIBLE : View.GONE
        );

        binding.rvSentRequests.setVisibility(
                isEmpty ? View.GONE : View.VISIBLE
        );
    }


    private void loadSentRequests(
            ConnectionRequestAdapter adapter,
            ConnectionRepository repository
    ) {

        FirebaseUser user =
                FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            adapter.updateRequests(new ArrayList<>());
            updateEmptyState(adapter);
            return;
        }

        repository.getSentRequests(
                user.getUid(),
                new ConnectionRepository.LoadRequestsCallback() {

                    @Override
                    public void onSuccess(List<Connection> requests) {

                        if (binding == null || !isAdded()) return;

                        adapter.updateRequests(requests);
                        updateEmptyState(adapter);
                    }

                    @Override
                    public void onFailure(String error) {

                        if (binding == null || !isAdded()) return;

                        Toast.makeText(
                                requireContext(),
                                "Unable to load sent requests: " + error,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }



    private void loadYourPublicId() {

        FirebaseUser user =
                FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            binding.tvYourId.setText("Not signed in");
            binding.btnCopyId.setEnabled(false);
            binding.btnShareId.setEnabled(false);
            return;
        }

        binding.tvYourId.setText("Loading...");
        binding.btnCopyId.setEnabled(false);
        binding.btnShareId.setEnabled(false);

        DatabaseReference idRef =
                FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(user.getUid())
                        .child("publicId");

        idRef.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot snapshot) {

                        if (binding == null) return;

                        String publicId =
                                snapshot.getValue(String.class);

                        if (publicId == null || publicId.isEmpty()) {
                            binding.tvYourId.setText("ID unavailable");
                            return;
                        }

                        binding.tvYourId.setText(publicId);

                        binding.btnCopyId.setEnabled(true);
                        binding.btnShareId.setEnabled(true);

                        // Copy ID
                        binding.btnCopyId.setOnClickListener(v -> {

                            ClipboardManager clipboard =
                                    (ClipboardManager) requireContext()
                                            .getSystemService(
                                                    Context.CLIPBOARD_SERVICE
                                            );

                            if (clipboard == null) return;

                            clipboard.setPrimaryClip(
                                    ClipData.newPlainText(
                                            "SubscriptionGuard ID",
                                            publicId
                                    )
                            );
                        });

                        // Share ID
                        binding.btnShareId.setOnClickListener(v -> {

                            Intent intent = new Intent(Intent.ACTION_SEND);

                            intent.setType("text/plain");

                            intent.putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Connect with me on SubscriptionGuard! "
                                            + "My ID is " + publicId
                            );

                            startActivity(
                                    Intent.createChooser(
                                            intent,
                                            "Share Connection ID"
                                    )
                            );
                        });
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        if (binding == null) return;

                        binding.tvYourId.setText("Unable to load ID");
                        binding.btnCopyId.setEnabled(false);
                        binding.btnShareId.setEnabled(false);
                    }
                }
        );
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
