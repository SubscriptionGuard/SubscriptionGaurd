package com.tfmhntspn.subguard.fragments;

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
import com.tfmhntspn.subguard.databinding.FragmentShareSubscriptionBinding;
import com.tfmhntspn.subguard.utils.SplitCalculator;
import com.tfmhntspn.subguard.adapters.SplitParticipantAdapter;
import com.tfmhntspn.subguard.models.SplitParticipant;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ShareSubscriptionFragment extends Fragment {

    private FragmentShareSubscriptionBinding binding;
    private SplitParticipantAdapter participantAdapter;
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

        // Temporary participants until connections are loaded
        participants.add(new SplitParticipant("demo-owner", "You", 0));
        participants.add(new SplitParticipant("demo-maya", "Maya", 0));
        participants.add(new SplitParticipant("demo-jordan", "Jordan", 0));

        // Calculate initial Netflix split
        updateEqualSplit();

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

        if (participants.isEmpty() || selectedPriceCents <= 0) {
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

    private void validateCurrentSplit() {

        if (binding == null) {
            return;
        }

        binding.btnSendForApproval.setEnabled(isSplitValid());

        binding.btnSendForApproval.setAlpha(
                isSplitValid() ? 1.0f : 0.5f
        );
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
