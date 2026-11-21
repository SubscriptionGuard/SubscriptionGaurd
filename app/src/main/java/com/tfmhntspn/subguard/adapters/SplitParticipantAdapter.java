package com.tfmhntspn.subguard.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tfmhntspn.subguard.databinding.ItemSplitParticipantBinding;
import com.tfmhntspn.subguard.models.SplitParticipant;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SplitParticipantAdapter
        extends RecyclerView.Adapter<SplitParticipantAdapter.ViewHolder> {

    public interface OnAmountChangedListener {
        void onAmountChanged();
    }

    private final List<SplitParticipant> participants = new ArrayList<>();
    private final Map<String, String> customInputs = new HashMap<>();

    private final OnAmountChangedListener listener;
    private boolean customMode = false;

    public SplitParticipantAdapter(OnAmountChangedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        ItemSplitParticipantBinding binding =
                ItemSplitParticipantBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                );

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {

        SplitParticipant participant = participants.get(position);

        holder.binding.tvParticipantName.setText(
                participant.getDisplayName()
        );

        holder.binding.tvParticipantAmount.setText(
                formatMoney(participant.getAmountCents())
        );

        // Remove the previous listener before updating recycled views
        if (holder.textWatcher != null) {
            holder.binding.etParticipantAmount
                    .removeTextChangedListener(holder.textWatcher);
        }

        String userId = participant.getUserId();

        holder.binding.etParticipantAmount.setText(
                customInputs.getOrDefault(userId, "")
        );

        holder.binding.tvParticipantAmount.setVisibility(
                customMode ? View.GONE : View.VISIBLE
        );

        holder.binding.etParticipantAmount.setVisibility(
                customMode ? View.VISIBLE : View.GONE
        );

        holder.textWatcher = new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                customInputs.put(userId, s.toString());

                if (listener != null) {
                    listener.onAmountChanged();
                }
            }
        };

        holder.binding.etParticipantAmount
                .addTextChangedListener(holder.textWatcher);
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    public void updateParticipants(List<SplitParticipant> newParticipants) {
        participants.clear();
        participants.addAll(newParticipants);

        // Remove draft amounts for people no longer selected
        customInputs.keySet().removeIf(id -> {
            for (SplitParticipant p : participants) {
                if (id.equals(p.getUserId())) {
                    return false;
                }
            }
            return true;
        });

        notifyDataSetChanged();
    }

    public void setCustomMode(boolean enabled) {
        customMode = enabled;
        notifyDataSetChanged();
    }

    public void clearCustomAmounts() {
        customInputs.clear();
        notifyDataSetChanged();
    }

    public boolean isCustomMode() {
        return customMode;
    }

    // Returns null if any custom amount is missing or invalid
    public long[] getCustomAmountsCents() {

        long[] amounts = new long[participants.size()];

        for (int i = 0; i < participants.size(); i++) {

            String text = customInputs.get(
                    participants.get(i).getUserId()
            );

            if (text == null || !text.matches("\\d+(\\.\\d{1,2})?")) {
                return null;
            }

            try {
                amounts[i] = new BigDecimal(text)
                        .movePointRight(2)
                        .longValueExact();
            } catch (ArithmeticException e) {
                return null;
            }
        }

        return amounts;
    }


    public long getTotalAllocatedCents() {

        long total = 0;

        for (SplitParticipant participant : participants) {

            String input = customInputs.get(participant.getUserId());

            // Empty fields contribute zero while the user is typing
            if (input == null || input.trim().isEmpty()) {
                continue;
            }

            // Invalid values must not be treated as valid money
            if (!input.matches("\\d+(\\.\\d{1,2})?")) {
                return -1;
            }

            try {
                long amount = new BigDecimal(input)
                        .movePointRight(2)
                        .longValueExact();

                total = Math.addExact(total, amount);

            } catch (ArithmeticException e) {
                return -1;
            }
        }

        return total;
    }

    private String formatMoney(long cents) {
        return String.format(
                Locale.US,
                "$%d.%02d",
                cents / 100,
                cents % 100
        );
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final ItemSplitParticipantBinding binding;
        TextWatcher textWatcher;

        public ViewHolder(ItemSplitParticipantBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
