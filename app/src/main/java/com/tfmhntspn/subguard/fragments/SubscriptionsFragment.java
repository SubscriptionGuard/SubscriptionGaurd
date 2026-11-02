package com.tfmhntspn.subguard.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.tfmhntspn.subguard.R;
import com.tfmhntspn.subguard.databinding.FragmentSubscriptionsBinding;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class SubscriptionsFragment extends Fragment {

    private FragmentSubscriptionsBinding binding;
    private String selectedCategory = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSubscriptionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnAddNewSubscription.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_subscriptionsFragment_to_addSubscriptionFragment));

        Map<String, TextView> chips = new LinkedHashMap<>();
        chips.put("All", binding.chipAll);
        chips.put("Streaming", binding.chipStreaming);
        chips.put("Food", binding.chipFood);
        chips.put("Utilities", binding.chipUtilities);

        for (Map.Entry<String, TextView> entry : chips.entrySet()) {
            String category = entry.getKey();
            TextView chipView = entry.getValue();
            chipView.setOnClickListener(v -> {
                selectedCategory = category;
                updateChipStyles(chips, category);
                String currentQuery = binding.etSearchSubscriptions.getText() != null
                        ? binding.etSearchSubscriptions.getText().toString() : "";
                applyFilters(currentQuery, selectedCategory);
            });
        }

        binding.etSearchSubscriptions.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters(s != null ? s.toString() : "", selectedCategory);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.cardSubNetflix.setOnClickListener(v ->
                navigateToDetails("Netflix", "N", "$16.99", "/ month · Standard", "Oct 24, 2026", "Streaming", "$203.88"));
        binding.cardSubSpotify.setOnClickListener(v ->
                navigateToDetails("Spotify", "S", "$11.99", "/ month · Individual", "Oct 27, 2026", "Streaming", "$143.88"));
        binding.cardSubTelus.setOnClickListener(v ->
                navigateToDetails("Telus WiFi", "T", "$30.00", "/ month · PureFibre", "Oct 28, 2026", "Utilities", "$360.00"));
        binding.cardSubDoordash.setOnClickListener(v ->
                navigateToDetails("DoorDash DashPass", "DD", "$8.99", "/ month · DashPass", "Nov 15, 2026", "Food", "$107.88"));
        binding.cardSubCrave.setOnClickListener(v ->
                navigateToDetails("Crave", "C", "$19.99", "/ month · Premium", "Nov 9, 2026", "Streaming", "$239.88"));
        binding.cardSubDisney.setOnClickListener(v ->
                navigateToDetails("Disney+", "D+", "$14.99", "/ month · Standard", "Nov 2, 2026", "Streaming", "$179.88"));
    }

    private void updateChipStyles(Map<String, TextView> chips, String active) {
        int greenColor = ContextCompat.getColor(requireContext(), R.color.primary_green);
        int onGreenColor = ContextCompat.getColor(requireContext(), R.color.button_on_green);
        int secondaryText = ContextCompat.getColor(requireContext(), R.color.text_secondary);

        for (Map.Entry<String, TextView> entry : chips.entrySet()) {
            String cat = entry.getKey();
            TextView chip = entry.getValue();
            if (cat.equals(active)) {
                chip.setBackgroundResource(R.drawable.bg_pill_green);
                chip.setBackgroundTintList(ColorStateList.valueOf(greenColor));
                chip.setTextColor(onGreenColor);
            } else {
                chip.setBackgroundResource(R.drawable.bg_pill_elevated);
                chip.setBackgroundTintList(null);
                chip.setTextColor(secondaryText);
            }
        }
    }

    private void applyFilters(String query, String category) {
        String q = query.trim().toLowerCase(Locale.ROOT);

        binding.cardSubNetflix.setVisibility(shouldShow("Netflix", "Streaming", q, category));
        binding.cardSubSpotify.setVisibility(shouldShow("Spotify", "Streaming", q, category));
        binding.cardSubTelus.setVisibility(shouldShow("Telus WiFi", "Utilities", q, category));
        binding.cardSubDoordash.setVisibility(shouldShow("DoorDash DashPass", "Food", q, category));
        binding.cardSubCrave.setVisibility(shouldShow("Crave", "Streaming", q, category));
        binding.cardSubDisney.setVisibility(shouldShow("Disney+", "Streaming", q, category));
    }

    private int shouldShow(String name, String itemCategory, String q, String category) {
        boolean matchesQuery = q.isEmpty() || name.toLowerCase(Locale.ROOT).contains(q);
        boolean matchesCategory = "All".equals(category) || itemCategory.equals(category);
        return (matchesQuery && matchesCategory) ? View.VISIBLE : View.GONE;
    }

    private void navigateToDetails(String name, String badge, String price, String plan,
                                   String renewal, String category, String yearly) {
        Bundle args = new Bundle();
        args.putString("service_name", name);
        args.putString("service_badge", badge);
        args.putString("service_price", price);
        args.putString("service_plan", plan);
        args.putString("next_renewal", renewal);
        args.putString("category", category);
        args.putString("yearly_cost", yearly);
        NavHostFragment.findNavController(this).navigate(
                R.id.action_subscriptionsFragment_to_subscriptionDetailsFragment,
                args
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
