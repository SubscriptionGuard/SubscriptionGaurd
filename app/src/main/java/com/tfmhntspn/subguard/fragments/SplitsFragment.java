package com.tfmhntspn.subguard.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.tfmhntspn.subguard.R;
import com.tfmhntspn.subguard.databinding.FragmentSplitsBinding;

public class SplitsFragment extends Fragment {

    private FragmentSplitsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSplitsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnHouseholdSettings.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_splitsFragment_to_householdSettingsFragment));

        binding.btnAddRoommate.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_splitsFragment_to_addRoommateFragment));

        binding.btnInvitedPriya.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_splitsFragment_to_inviteApprovalFragment));

        binding.avatarMaya.setOnClickListener(v ->
                navigateToReminder("Maya Chen", "$26.50"));

        binding.avatarJordan.setOnClickListener(v ->
                navigateToReminder("Jordan Lee", "$16.50"));

        binding.cardOwedSummary.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_splitsFragment_to_sendReminderFragment));

        binding.chipOweMaya.setOnClickListener(v ->
                navigateToReminder("Maya Chen", "$26.50"));

        binding.chipOweJordan.setOnClickListener(v ->
                navigateToReminder("Jordan Lee", "$16.50"));

        binding.cardPendingChange.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_splitsFragment_to_reviewChangeFragment));

        binding.btnViewClassicSplits.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_splitsFragment_to_roommateSplitsV1Fragment));

        binding.cardSharedTelus.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_splitsFragment_to_reviewChangeFragment));

        binding.cardSharedHydro.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_splitsFragment_to_sendReminderFragment));

        binding.btnShareSubscription.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_splitsFragment_to_shareSubscriptionFragment));
    }

    private void navigateToReminder(String roommateName, String amountOwed) {
        Bundle args = new Bundle();
        args.putString("roommate_name", roommateName);
        args.putString("amount_owed", amountOwed);
        NavHostFragment.findNavController(this).navigate(
                R.id.action_splitsFragment_to_sendReminderFragment,
                args
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
