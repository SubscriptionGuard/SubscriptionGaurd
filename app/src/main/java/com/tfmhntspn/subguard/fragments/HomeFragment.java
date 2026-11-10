package com.tfmhntspn.subguard.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tfmhntspn.subguard.R;
import com.tfmhntspn.subguard.databinding.FragmentHomeBinding;

import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    public static final String PREFS_NAME = "subguard_user_prefs";
    public static final String KEY_CUSTOM_NAME = "custom_display_name";

    public static final String FIELD_BURN_RATE = "burnRate";
    public static final String FIELD_ACTIVE_COUNT = "activeCount";
    public static final String FIELD_MONTHLY_BUDGET = "monthlyBudget";
    private static final double DEFAULT_MONTHLY_BUDGET = 150.0;

    private FragmentHomeBinding binding;
    private DatabaseReference userSummaryRef;
    private ValueEventListener burnRateListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bindUserGreetingAndAvatar();
        observeBurnRateFromFirebase();

        binding.btnThemeToggle.setOnClickListener(v -> {
            int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            int nextMode = (currentNightMode == Configuration.UI_MODE_NIGHT_YES)
                    ? AppCompatDelegate.MODE_NIGHT_NO
                    : AppCompatDelegate.MODE_NIGHT_YES;
            AppCompatDelegate.setDefaultNightMode(nextMode);
        });

        binding.avatarProfile.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_homeFragment_to_profileFragment));

        binding.cardActionAdd.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_homeFragment_to_addSubscriptionFragment));

        binding.cardActionScan.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_homeFragment_to_scanReceiptFragment));

        binding.cardActionSimulator.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_homeFragment_to_simulatorFragment));

        binding.btnSeeAllRenewals.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_homeFragment_to_subscriptionsFragment));
    }

    private void observeBurnRateFromFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            updateBurnRateUi(0.0, 0, DEFAULT_MONTHLY_BUDGET);
            return;
        }

        userSummaryRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUser.getUid());

        burnRateListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding == null) return;

                Double burnRateVal = snapshot.child(FIELD_BURN_RATE).getValue(Double.class);
                Long activeCountVal = snapshot.child(FIELD_ACTIVE_COUNT).getValue(Long.class);
                Double budgetVal = snapshot.child(FIELD_MONTHLY_BUDGET).getValue(Double.class);

                double burnRate = burnRateVal != null ? Math.max(0.0, burnRateVal) : 0.0;
                int activeCount = activeCountVal != null ? Math.max(0, activeCountVal.intValue()) : 0;
                double monthlyBudget = (budgetVal != null && budgetVal > 0)
                        ? budgetVal : DEFAULT_MONTHLY_BUDGET;

                updateBurnRateUi(burnRate, activeCount, monthlyBudget);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read burnRate from Firebase", error.toException());
            }
        };

        userSummaryRef.addValueEventListener(burnRateListener);
    }

    private void updateBurnRateUi(double burnRate, int activeCount, double monthlyBudget) {
        double annualProjected = burnRate * 12.0;
        int percentUsed = monthlyBudget > 0
                ? (int) Math.round((burnRate / monthlyBudget) * 100.0) : 0;
        int clampedProgress = Math.max(0, Math.min(100, percentUsed));
        double remaining = Math.max(0.0, monthlyBudget - burnRate);

        binding.txtMonthlyTotal.setText(String.format(Locale.US, "$%.2f", burnRate));
        binding.txtAnnualTotal.setText(
                String.format(Locale.US, "$%,.2f projected this year", annualProjected)
        );
        binding.tvActiveChip.setText(activeCount + " active");
        binding.progressBudget.setProgress(clampedProgress);
        binding.tvBudgetUsed.setText(
                String.format(Locale.US, "Budget $%.0f of $%.0f", burnRate, monthlyBudget)
        );
        binding.tvBudgetRemaining.setText(
                String.format(Locale.US, "%d%% · $%.2f left", percentUsed, remaining)
        );
    }

    private void bindUserGreetingAndAvatar() {
        String fullName = resolveUserDisplayName(requireContext());
        String greetingName = extractFirstName(fullName);
        String initials = extractInitials(fullName);

        binding.tvGreeting.setText("Hi, " + greetingName + " 👋");
        binding.avatarProfile.setText(initials);
    }

    public static String resolveUserDisplayName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedCustomName = prefs.getString(KEY_CUSTOM_NAME, null);
        if (savedCustomName != null && !savedCustomName.trim().isEmpty()) {
            return savedCustomName.trim();
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            if (displayName != null && !displayName.trim().isEmpty()) {
                return displayName.trim();
            }

            String email = user.getEmail();
            if (email != null && !email.trim().isEmpty()) {
                return formatNameFromEmail(email.trim());
            }
        }

        return "P";
    }

    public static String formatNameFromEmail(String email) {
        int atIndex = email.indexOf('@');
        String localPart = (atIndex > 0) ? email.substring(0, atIndex) : email;
        String[] parts = localPart.split("[._\\-]+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    sb.append(part.substring(1));
                }
            }
        }
        return sb.length() > 0 ? sb.toString() : "User";
    }

    public static String extractFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "P";
        }
        String[] tokens = fullName.trim().split("\\s+");
        String first = tokens[0];
        return Character.toUpperCase(first.charAt(0)) + (first.length() > 1 ? first.substring(1) : "");
    }

    public static String extractInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "P";
        }
        String[] tokens = fullName.trim().split("\\s+");
        if (tokens.length >= 2 && !tokens[0].isEmpty() && !tokens[tokens.length - 1].isEmpty()) {
            return (String.valueOf(tokens[0].charAt(0)) + tokens[tokens.length - 1].charAt(0))
                    .toUpperCase(Locale.ROOT);
        }
        return String.valueOf(tokens[0].charAt(0)).toUpperCase(Locale.ROOT);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userSummaryRef != null && burnRateListener != null) {
            userSummaryRef.removeEventListener(burnRateListener);
        }
        binding = null;
    }
}
