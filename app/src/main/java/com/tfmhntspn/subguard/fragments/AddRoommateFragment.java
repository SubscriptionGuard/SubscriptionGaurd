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

import com.tfmhntspn.subguard.R;
import com.tfmhntspn.subguard.databinding.FragmentAddRoommateBinding;
import com.tfmhntspn.subguard.adapters.ConnectionRequestAdapter;

import java.util.HashMap;

public class AddRoommateFragment extends Fragment {

    private FragmentAddRoommateBinding binding;

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

        binding.rvSentRequests.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        ConnectionRequestAdapter adapter = new ConnectionRequestAdapter(
                new HashMap<>(),
                connection -> {
                    Toast.makeText(
                            requireContext(),
                            "Connection cancellation will be available when Firebase is connected.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );

        binding.rvSentRequests.setAdapter(adapter);
        updateEmptyState(adapter);


        binding.btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        binding.btnSendRequest.setOnClickListener(v -> {
            String id = binding.etConnectionId.getText() != null
                    ? binding.etConnectionId.getText().toString().trim() : "";

            if (id.isEmpty()) {
                binding.etConnectionId.setError("Please enter a SubscriptionGuard ID");
                return;
            }

            if (!id.matches("SG-[0-9]{6}")) {
                binding.etConnectionId.setError("Use the format SG-123456");
                return;
            }

            binding.etConnectionId.setError(null);

            Toast.makeText(
                    requireContext(),
                    "Invite sent to roommate (" + id + ")",
                    Toast.LENGTH_SHORT
            ).show();
        });

        binding.btnCopyId.setOnClickListener(v -> {
            String myId = binding.tvYourId.getText().toString();
            ClipboardManager clipboard = (ClipboardManager) requireContext()
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                clipboard.setPrimaryClip(ClipData.newPlainText("SubscriptionGuard ID", myId));
            }
            Toast.makeText(requireContext(), "Copied ID: " + myId, Toast.LENGTH_SHORT).show();
        });

        binding.btnShareId.setOnClickListener(v -> {
            String myId = binding.tvYourId.getText().toString();
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Join my SubscriptionGuard household with ID: " + myId);
            startActivity(Intent.createChooser(shareIntent, "Share SubscriptionGuard ID"));
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
