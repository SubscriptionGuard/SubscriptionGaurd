package com.tfmhntspn.subguard.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
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
import com.tfmhntspn.subguard.models.Subscription;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    public static final String PREFS_NAME = "subguard_user_prefs";
    public static final String KEY_CUSTOM_NAME = "custom_display_name";

    public static final String FIELD_BURN_RATE = "burnRate";
    public static final String FIELD_ACTIVE_COUNT = "activeCount";
    public static final String FIELD_MONTHLY_BUDGET = "monthlyBudget";
    private static final double DEFAULT_MONTHLY_BUDGET = 150.0;

    private FragmentHomeBinding binding;
    private DatabaseReference subscriptionsRef;
    private ValueEventListener subscriptionsListener;
    private DatabaseReference userRef;
    private ValueEventListener userBudgetListener;

    private double currentMonthlyBudget = DEFAULT_MONTHLY_BUDGET;
    private double currentBurnRate = 0.0;
    private int currentActiveCount = 0;

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
        observeDashboardDataFromFirebase();

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

    private void observeDashboardDataFromFirebase() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentMonthlyBudget = prefs.getFloat(ProfileFragment.KEY_MONTHLY_BUDGET, (float) DEFAULT_MONTHLY_BUDGET);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            updateBurnRateUi(0.0, 0, currentMonthlyBudget);
            updateTopCategoriesUi(0.0, 0.0, 0.0);
            binding.tvNoUpcomingRenewals.setVisibility(View.VISIBLE);
            return;
        }

        String uid = currentUser.getUid();

        // 1. Observe user's monthlyBudget from users/{uid}
        userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        userBudgetListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding == null) return;
                Double budgetVal = snapshot.child(FIELD_MONTHLY_BUDGET).getValue(Double.class);
                if (budgetVal != null && budgetVal > 0) {
                    currentMonthlyBudget = budgetVal;
                }
                updateBurnRateUi(currentBurnRate, currentActiveCount, currentMonthlyBudget);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read user budget", error.toException());
            }
        };
        userRef.addValueEventListener(userBudgetListener);

        // 2. Observe user's subscriptions from subscriptions/{uid} (saved by AddSubscriptionFragment & ReviewScanDetailsFragment)
        subscriptionsRef = FirebaseDatabase.getInstance().getReference("subscriptions").child(uid);
        subscriptionsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (binding == null) return;

                double monthlyBurnTotal = 0.0;
                double streamingMonthly = 0.0;
                double utilitiesMonthly = 0.0;
                double foodMonthly = 0.0;
                int activeCount = 0;
                int upcomingCount = 0;

                binding.layoutUpcomingRenewals.removeAllViews();
                Calendar todayCal = getStartOfToday();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Subscription sub = child.getValue(Subscription.class);
                    if (sub == null) continue;

                    activeCount++;
                    double cost = sub.getCost();
                    double monthlyEquivalent = "Yearly".equalsIgnoreCase(sub.getBillingCycle())
                            ? cost / 12.0 : cost;
                    monthlyBurnTotal += monthlyEquivalent;

                    String category = sub.getCategory() != null ? sub.getCategory() : "";
                    if ("Streaming".equalsIgnoreCase(category)) {
                        streamingMonthly += monthlyEquivalent;
                    } else if ("Utilities".equalsIgnoreCase(category)) {
                        utilitiesMonthly += monthlyEquivalent;
                    } else if ("Food".equalsIgnoreCase(category)) {
                        foodMonthly += monthlyEquivalent;
                    }

                    // Check if nextRenewalDate ("yyyy-MM-dd") is 1 to 7 days away; remove on charge day (<= 0)
                    int daysUntil = calculateDaysUntilCharge(sub.getNextRenewalDate(), todayCal);
                    if (daysUntil >= 1 && daysUntil <= 7) {
                        String name = sub.getName() != null ? sub.getName() : "Subscription";
                        addUpcomingRenewalRow(name, cost, daysUntil);
                        upcomingCount++;
                    }
                }

                currentBurnRate = Math.round(monthlyBurnTotal * 100.0) / 100.0;
                currentActiveCount = activeCount;

                updateBurnRateUi(currentBurnRate, currentActiveCount, currentMonthlyBudget);
                updateTopCategoriesUi(streamingMonthly, utilitiesMonthly, foodMonthly);
                binding.tvNoUpcomingRenewals.setVisibility(upcomingCount == 0 ? View.VISIBLE : View.GONE);

                // Keep users/{uid}/burnRate and activeCount synced in Firebase
                Map<String, Object> summaryUpdates = new HashMap<>();
                summaryUpdates.put(FIELD_BURN_RATE, currentBurnRate);
                summaryUpdates.put(FIELD_ACTIVE_COUNT, currentActiveCount);
                userRef.updateChildren(summaryUpdates);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to read subscriptions", error.toException());
            }
        };
        subscriptionsRef.addValueEventListener(subscriptionsListener);
    }

    private int calculateDaysUntilCharge(@Nullable String renewalDateStr, @NonNull Calendar todayCal) {
        if (renewalDateStr == null || renewalDateStr.trim().isEmpty()) {
            return -1;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date parsed = sdf.parse(renewalDateStr.trim());
            if (parsed == null) return -1;

            Calendar chargeCal = getStartOfToday();
            chargeCal.setTime(parsed);
            chargeCal.set(Calendar.HOUR_OF_DAY, 0);
            chargeCal.set(Calendar.MINUTE, 0);
            chargeCal.set(Calendar.SECOND, 0);
            chargeCal.set(Calendar.MILLISECOND, 0);

            long diffMillis = chargeCal.getTimeInMillis() - todayCal.getTimeInMillis();
            return (int) TimeUnit.MILLISECONDS.toDays(diffMillis);
        } catch (ParseException e) {
            return -1;
        }
    }

    private Calendar getStartOfToday() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
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

    private void updateTopCategoriesUi(double streaming, double utilities, double food) {
        binding.tvCatStreaming.setText(String.format(Locale.US, "● Streaming $%.2f", streaming));
        binding.tvCatUtilities.setText(String.format(Locale.US, "● Utilities $%.2f", utilities));
        binding.tvCatFood.setText(String.format(Locale.US, "● Food $%.2f", food));

        double total = streaming + utilities + food;
        float wStreaming = total > 0 ? (float) Math.max(5.0, (streaming / total) * 100.0) : 1f;
        float wUtilities = total > 0 ? (float) Math.max(5.0, (utilities / total) * 100.0) : 1f;
        float wFood = total > 0 ? (float) Math.max(5.0, (food / total) * 100.0) : 1f;

        setBarWeight(binding.barCatStreaming, wStreaming);
        setBarWeight(binding.barCatUtilities, wUtilities);
        setBarWeight(binding.barCatFood, wFood);
    }

    private void setBarWeight(@NonNull View bar, float weight) {
        ViewGroup.LayoutParams lp = bar.getLayoutParams();
        if (lp instanceof LinearLayout.LayoutParams) {
            ((LinearLayout.LayoutParams) lp).weight = weight;
            bar.setLayoutParams(lp);
        }
    }

    private void addUpcomingRenewalRow(String name, double price, int daysUntil) {
        Context context = requireContext();
        int pad = Math.round(14 * context.getResources().getDisplayMetrics().density);

        LinearLayout row = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = pad / 2;
        row.setLayoutParams(params);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundResource(R.drawable.bg_card_rounded);
        row.setPadding(pad, pad, pad, pad);

        TextView titleView = new TextView(context);
        titleView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        titleView.setText(String.format(Locale.US, "%s  ·  $%.2f", name, price));
        titleView.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
        titleView.setTypeface(titleView.getTypeface(), Typeface.BOLD);

        TextView daysView = new TextView(context);
        daysView.setText(daysUntil == 1 ? "In 1 day" : "In " + daysUntil + " days");
        daysView.setTextColor(ContextCompat.getColor(context, R.color.accent_green_text));
        daysView.setTypeface(daysView.getTypeface(), Typeface.BOLD);

        row.addView(titleView);
        row.addView(daysView);
        binding.layoutUpcomingRenewals.addView(row);
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
        if (subscriptionsRef != null && subscriptionsListener != null) {
            subscriptionsRef.removeEventListener(subscriptionsListener);
        }
        if (userRef != null && userBudgetListener != null) {
            userRef.removeEventListener(userBudgetListener);
        }
        binding = null;
    }
}
