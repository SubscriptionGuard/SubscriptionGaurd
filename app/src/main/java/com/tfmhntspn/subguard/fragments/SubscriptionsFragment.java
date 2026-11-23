
package com.tfmhntspn.subguard.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
import com.tfmhntspn.subguard.adapters.SubscriptionAdapter;
import com.tfmhntspn.subguard.databinding.FragmentSubscriptionsBinding;
import com.tfmhntspn.subguard.models.Subscription;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SubscriptionsFragment extends Fragment {

    private FragmentSubscriptionsBinding binding;
    private SubscriptionAdapter adapter;

    private final List<Subscription> allSubscriptions = new ArrayList<>();

    private String selectedCategory = "All";

    private DatabaseReference subscriptionsRef;
    private ValueEventListener subscriptionsListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentSubscriptionsBinding.inflate(
                inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Open the Add Subscription screen
        binding.btnAddNewSubscription.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(
                        R.id.action_subscriptionsFragment_to_addSubscriptionFragment
                ));

        // Set up the subscription list
        adapter = new SubscriptionAdapter(this::openSubscriptionDetails);

        binding.recyclerSubscriptions.setLayoutManager(
                new LinearLayoutManager(requireContext()));

        binding.recyclerSubscriptions.setAdapter(adapter);

        // Set up category filters
        Map<String, TextView> chips = new LinkedHashMap<>();
        chips.put("All", binding.chipAll);
        chips.put("Streaming", binding.chipStreaming);
        chips.put("Food", binding.chipFood);
        chips.put("Utilities", binding.chipUtilities);

        for (Map.Entry<String, TextView> entry : chips.entrySet()) {

            String category = entry.getKey();
            TextView chip = entry.getValue();

            chip.setOnClickListener(v -> {
                selectedCategory = category;
                updateChipStyles(chips, category);
                applyFilters();
            });
        }

        // Update results as the user types
        binding.etSearchSubscriptions.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence s,
                                                  int start,
                                                  int count,
                                                  int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s,
                                              int start,
                                              int before,
                                              int count) {
                        applyFilters();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

        // Load the signed-in user's subscriptions
        loadSubscriptions();
    }

    private void loadSubscriptions() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            binding.tvSubscriptionsSummary.setText("Please log in");
            binding.tvEmptySubscriptions.setText(
                    "Log in to see your subscriptions.");
            binding.tvEmptySubscriptions.setVisibility(View.VISIBLE);
            return;
        }

        subscriptionsRef = FirebaseDatabase.getInstance()
                .getReference("subscriptions")
                .child(user.getUid());

        binding.tvSubscriptionsSummary.setText("Loading subscriptions...");

        subscriptionsListener = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (binding == null) return;

                allSubscriptions.clear();

                for (DataSnapshot child : snapshot.getChildren()) {

                    Subscription subscription =
                            child.getValue(Subscription.class);

                    if (subscription != null) {
                        if (subscription.getId() == null) {
                            subscription.setId(child.getKey());
                        }
                        allSubscriptions.add(subscription);
                    }
                }

                updateSummary();
                applyFilters();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                if (binding == null) return;

                Toast.makeText(requireContext(),
                        "Unable to load subscriptions: "
                                + error.getMessage(),
                        Toast.LENGTH_LONG).show();

                binding.tvSubscriptionsSummary.setText(
                        "Unable to load subscriptions");

                binding.tvEmptySubscriptions.setText(
                        "Could not load subscriptions.");
                binding.tvEmptySubscriptions.setVisibility(View.VISIBLE);
            }
        };

        subscriptionsRef.addValueEventListener(subscriptionsListener);
    }

    private void updateSummary() {

        double monthlyTotal = 0;

        for (Subscription subscription : allSubscriptions) {

            double cost = subscription.getCost();

            if ("Yearly".equalsIgnoreCase(
                    subscription.getBillingCycle())) {
                monthlyTotal += cost / 12.0;
            } else {
                monthlyTotal += cost;
            }
        }

        binding.tvSubscriptionsSummary.setText(String.format(
                Locale.CANADA,
                "%d subscriptions · $%.2f / month equivalent",
                allSubscriptions.size(),
                monthlyTotal
        ));
    }

    private void applyFilters() {

        if (binding == null || adapter == null) return;

        String query = binding.etSearchSubscriptions.getText() != null
                ? binding.etSearchSubscriptions.getText()
                .toString().trim().toLowerCase(Locale.ROOT)
                : "";

        List<Subscription> filtered = new ArrayList<>();

        for (Subscription subscription : allSubscriptions) {

            String name = subscription.getName() != null
                    ? subscription.getName() : "";

            String category = subscription.getCategory() != null
                    ? subscription.getCategory() : "";

            boolean matchesSearch =
                    name.toLowerCase(Locale.ROOT).contains(query);

            boolean matchesCategory =
                    selectedCategory.equals("All")
                            || selectedCategory.equalsIgnoreCase(category);

            if (matchesSearch && matchesCategory) {
                filtered.add(subscription);
            }
        }

        adapter.setSubscriptions(filtered);

        if (filtered.isEmpty()) {
            binding.tvEmptySubscriptions.setText(
                    "No subscriptions found. Tap + to add one.");
            binding.tvEmptySubscriptions.setVisibility(View.VISIBLE);
        } else {
            binding.tvEmptySubscriptions.setVisibility(View.GONE);
        }
    }

    private void updateChipStyles(Map<String, TextView> chips,
                                  String active) {

        int greenColor = ContextCompat.getColor(
                requireContext(), R.color.primary_green);

        int onGreenColor = ContextCompat.getColor(
                requireContext(), R.color.button_on_green);

        int secondaryText = ContextCompat.getColor(
                requireContext(), R.color.text_secondary);

        for (Map.Entry<String, TextView> entry : chips.entrySet()) {

            TextView chip = entry.getValue();

            if (entry.getKey().equals(active)) {

                chip.setBackgroundResource(R.drawable.bg_pill_green);
                chip.setBackgroundTintList(
                        ColorStateList.valueOf(greenColor));
                chip.setTextColor(onGreenColor);

            } else {

                chip.setBackgroundResource(R.drawable.bg_pill_elevated);
                chip.setBackgroundTintList(null);
                chip.setTextColor(secondaryText);
            }
        }
    }

    private void openSubscriptionDetails(Subscription subscription) {

        String name = subscription.getName() != null
                ? subscription.getName() : "";

        String cycle = subscription.getBillingCycle() != null
                ? subscription.getBillingCycle() : "Monthly";

        String badge = name.isEmpty() ? "?"
                : name.substring(0, 1).toUpperCase(Locale.ROOT);

        String price = String.format(
                Locale.CANADA, "$%.2f", subscription.getCost());

        double yearlyCost = "Yearly".equalsIgnoreCase(cycle)
                ? subscription.getCost()
                : subscription.getCost() * 12;

        Bundle args = new Bundle();
        args.putString("subscription_id", subscription.getId());
        args.putString("service_name", name);
        args.putString("service_badge", badge);
        args.putString("service_price", price);
        args.putString("service_plan", "/ " + cycle.toLowerCase(Locale.ROOT));
        args.putString("next_renewal",
                subscription.getNextRenewalDate());
        args.putString("category", subscription.getCategory());
        args.putString("yearly_cost", String.format(
                Locale.CANADA, "$%.2f", yearlyCost));

        NavHostFragment.findNavController(this).navigate(
                R.id.action_subscriptionsFragment_to_subscriptionDetailsFragment,
                args
        );
    }

    @Override
    public void onDestroyView() {

        if (subscriptionsRef != null && subscriptionsListener != null) {
            subscriptionsRef.removeEventListener(subscriptionsListener);
        }

        subscriptionsListener = null;
        subscriptionsRef = null;

        if (binding != null) {
            binding.recyclerSubscriptions.setAdapter(null);
        }

        adapter = null;
        binding = null;

        super.onDestroyView();
    }
}
