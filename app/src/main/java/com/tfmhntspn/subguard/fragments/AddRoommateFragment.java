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

import com.tfmhntspn.subguard.R;
import com.tfmhntspn.subguard.databinding.FragmentAddRoommateBinding;

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

        binding.btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        binding.btnSendRequest.setOnClickListener(v -> {
            String id = binding.etRoommateId.getText() != null
                    ? binding.etRoommateId.getText().toString().trim() : "";
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

        binding.cardSentRequestPriya.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_addRoommateFragment_to_inviteApprovalFragment));

        binding.btnCancelRequest.setOnClickListener(v -> {
            binding.cardSentRequestPriya.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "Invite cancelled", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
