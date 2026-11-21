package com.tfmhntspn.subguard.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.view.View;
import android.content.res.ColorStateList;


import androidx.core.content.ContextCompat;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.tfmhntspn.subguard.R;
import com.tfmhntspn.subguard.databinding.FragmentShareSubscriptionBinding;
import com.tfmhntspn.subguard.utils.SplitCalculator;
import com.tfmhntspn.subguard.adapters.SplitParticipantAdapter;
import com.tfmhntspn.subguard.models.SplitParticipant;
import com.tfmhntspn.subguard.adapters.ConnectionSelectionAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ShareSubscriptionFragment extends Fragment {

    private FragmentShareSubscriptionBinding binding;
    private SplitParticipantAdapter participantAdapter;
    private ConnectionSelectionAdapter connectionAdapter;
    private final List<SplitParticipant> participants = new ArrayList<>();

    private long selectedPriceCents = 1699;
    private boolean isCustomSplit = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentShareSubscriptionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Back button
        binding.btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        // Set up participant RecyclerView
        binding.rvSplitParticipants.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        participantAdapter = new SplitParticipantAdapter(
                this::validateCurrentSplit
        );

        binding.rvSplitParticipants.setAdapter(participantAdapter);

        // Set up the connection selection list
        binding.rvSelectConnections.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        connectionAdapter = new ConnectionSelectionAdapter(
                selectedConnections -> {

                    // Rebuild the participant list when selection changes
                    updateSelectedParticipants(selectedConnections);
                }
        );

        binding.rvSelectConnections.setAdapter(connectionAdapter);

        // Temporary connections until Firebase is available
        List<ConnectionSelectionAdapter.ConnectionOption> connections =
                new ArrayList<>();

        connections.add(
                new ConnectionSelectionAdapter.ConnectionOption(
                        "demo-maya", "Maya"
                )
        );

        connections.add(
                new ConnectionSelectionAdapter.ConnectionOption(
                        "demo-jordan", "Jordan"
                )
        );

        connections.add(
                new ConnectionSelectionAdapter.ConnectionOption(
                        "demo-sarah", "Sarah"
                )
        );

        connectionAdapter.updateConnections(connections);


        // Subscription selection
        binding.pickNetflix.setOnClickListener(v -> {
            selectedPriceCents = 1699;
            selectSubscription(0);
        });

        binding.pickSpotify.setOnClickListener(v -> {
            selectedPriceCents = 1199;
            selectSubscription(1);
        });

        binding.pickCrave.setOnClickListener(v -> {
            selectedPriceCents = 1999;
            selectSubscription(2);
        });

        // Equal Split
        binding.btnSplitEqual.setOnClickListener(v -> {

            isCustomSplit = false;

            binding.btnSplitEqual.setBackgroundResource(
                    R.drawable.bg_pill_green
            );

            binding.btnSplitCustom.setBackgroundResource(
                    R.drawable.bg_pill_elevated
            );

            updateEqualSplit();
            participantAdapter.setCustomMode(false);
            validateCurrentSplit();
        });

        // Custom Split
        binding.btnSplitCustom.setOnClickListener(v -> {

            isCustomSplit = true;

            binding.btnSplitCustom.setBackgroundResource(
                    R.drawable.bg_pill_green
            );

            binding.btnSplitEqual.setBackgroundResource(
                    R.drawable.bg_pill_elevated
            );

            participantAdapter.setCustomMode(true);
            validateCurrentSplit();
        });

        // Submit proposed split
        binding.btnSendForApproval.setOnClickListener(v -> {

            if (!isSplitValid()) {
                Toast.makeText(
                        requireContext(),
                        "Split amounts must equal the subscription total.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            Toast.makeText(
                    requireContext(),
                    "Split is valid. Firebase saving is not connected yet.",
                    Toast.LENGTH_LONG
            ).show();
        });
    }


    private void updateSelectedParticipants(
            List<ConnectionSelectionAdapter.ConnectionOption> selectedConnections) {

        participants.clear();

        // Always include the subscription payer
        participants.add(
                new SplitParticipant("demo-owner", "You", 0)
        );

        // Add each selected connection
        for (ConnectionSelectionAdapter.ConnectionOption connection
                : selectedConnections) {

            participants.add(
                    new SplitParticipant(
                            connection.userId,
                            connection.displayName,
                            0
                    )
            );
        }

        if (isCustomSplit) {
            participantAdapter.updateParticipants(participants);
        } else {
            updateEqualSplit();
        }

        validateCurrentSplit();
    }



    private void selectSubscription(int selected) {

        binding.pickNetflix.setBackgroundResource(
                selected == 0
                        ? R.drawable.bg_pill_green
                        : R.drawable.bg_card_rounded
        );

        binding.pickSpotify.setBackgroundResource(
                selected == 1
                        ? R.drawable.bg_pill_green
                        : R.drawable.bg_card_rounded
        );

        binding.pickCrave.setBackgroundResource(
                selected == 2
                        ? R.drawable.bg_pill_green
                        : R.drawable.bg_card_rounded
        );

        String price = String.format(
                Locale.US,
                "$%.2f",
                selectedPriceCents / 100.0
        );

        binding.tvSplitHeader.setText(
                "2 · SPLIT " + price + " WITH"
        );

        participantAdapter.clearCustomAmounts();

        if (!isCustomSplit) {
            updateEqualSplit();
        }

        validateCurrentSplit();
    }

    private void updateEqualSplit() {

        if (participants.isEmpty()) {
            participantAdapter.updateParticipants(participants);
            return;
        }

        long[] amounts = SplitCalculator.calculateEqualSplit(
                selectedPriceCents,
                participants.size()
        );

        for (int i = 0; i < participants.size(); i++) {
            participants.get(i).setAmountCents(amounts[i]);
        }

        participantAdapter.updateParticipants(participants);
    }


    private boolean isSplitValid() {

        // Require the payer and at least one other participant
        if (participants.size() < 2 || selectedPriceCents <= 0) {
            return false;
        }

        if (!isCustomSplit) {
            return true;
        }

        long[] amounts = participantAdapter.getCustomAmountsCents();

        return amounts != null &&
                SplitCalculator.validateCustomSplit(
                        selectedPriceCents,
                        amounts
                );
    }


    private void updateSplitSummary() {

        // Equal split: show only subscription total
        if (!isCustomSplit) {

            binding.layoutEqualTotal.setVisibility(View.VISIBLE);
            binding.layoutAllocationProgress.setVisibility(View.GONE);

            binding.tvSubscriptionTotal.setText(
                    formatMoney(selectedPriceCents)
            );

            return;
        }

        // Custom split: show allocation progress
        binding.layoutEqualTotal.setVisibility(View.GONE);
        binding.layoutAllocationProgress.setVisibility(View.VISIBLE);

        long allocated = participantAdapter.getTotalAllocatedCents();

        // Invalid or excessively large input
        if (allocated < 0) {

            binding.tvAllocationAmount.setText("Invalid amount");

            binding.progressAllocation.setProgress(0);

            binding.tvAllocationStatus.setText(
                    "Enter valid amounts with up to 2 decimal places"
            );

            setAllocationColor(R.color.danger_red);
            return;
        }

        // Example: $12.00 / $16.99
        binding.tvAllocationAmount.setText(
                formatMoney(allocated)
                        + " / "
                        + formatMoney(selectedPriceCents)
        );

        // Convert allocation into a progress percentage
        int progress = (int) Math.min(
                1000,
                (allocated / (double) selectedPriceCents) * 1000
        );

        binding.progressAllocation.setProgress(progress);

        long difference = selectedPriceCents - allocated;

        if (difference > 0) {

            // Under the total
            binding.tvAllocationStatus.setText(
                    formatMoney(difference) + " left to allocate"
            );

            setAllocationColor(R.color.accent_blue);

        } else if (difference == 0) {

            // Exactly matches the subscription cost
            binding.tvAllocationStatus.setText(
                    participants.size() >= 2
                            ? "Ready to send for approval"
                            : "Select at least one connection"
            );

            setAllocationColor(R.color.accent_green_text);

        } else {

            // Over the subscription cost
            binding.tvAllocationStatus.setText(
                    formatMoney(-difference) + " over the total"
            );

            setAllocationColor(R.color.danger_red);
        }
    }

    private void setAllocationColor(int colorResource) {

        int color = ContextCompat.getColor(
                requireContext(),
                colorResource
        );

        binding.tvAllocationAmount.setTextColor(color);
        binding.tvAllocationStatus.setTextColor(color);

        binding.progressAllocation.setProgressTintList(
                ColorStateList.valueOf(color)
        );
    }

    private String formatMoney(long cents) {
        return String.format(
                Locale.US,
                "$%d.%02d",
                cents / 100,
                cents % 100
        );
    }




    private void validateCurrentSplit() {

        if (binding == null) {
            return;
        }

        updateSplitSummary();

        boolean valid = isSplitValid();

        binding.btnSendForApproval.setEnabled(valid);
        binding.btnSendForApproval.setAlpha(
                valid ? 1.0f : 0.5f
        );
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}
