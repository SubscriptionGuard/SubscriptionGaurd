package com.tfmhntspn.subguard.fragments;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
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

import com.tfmhntspn.subguard.R;
import com.tfmhntspn.subguard.databinding.FragmentAddSubscriptionBinding;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.tfmhntspn.subguard.reminders.ReminderScheduler;

import com.tfmhntspn.subguard.models.Subscription;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;

import com.tfmhntspn.subguard.reminders.RenewalReminderWorker;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class AddSubscriptionFragment extends Fragment {

    private FragmentAddSubscriptionBinding binding;

    private String selectedBillingCycle = "Monthly";
    private String selectedCategory = "Streaming";
    private String selectedRenewalDate = null;
    private String editingSubscriptionId = null;


    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {

                        if (binding == null) return;

                        if (!isGranted) {
                            binding.switchReminder.setChecked(false);

                            Toast.makeText(requireContext(),
                                    "Notifications are disabled. Subscription will be saved without reminders.",
                                    Toast.LENGTH_LONG).show();
                        }

                        saveSubscription();
                    }
            );


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddSubscriptionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();

        if (args != null) {
            editingSubscriptionId = args.getString("subscription_id");
        }
        binding.btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        binding.cardScanReceipt.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_addSubscriptionFragment_to_scanReceiptFragment));

        binding.btnCycleMonthly.setOnClickListener(v -> {
            selectedBillingCycle = "Monthly";
            selectToggle(binding.btnCycleMonthly, binding.btnCycleYearly);
        });

        binding.btnCycleYearly.setOnClickListener(v -> {
            selectedBillingCycle = "Yearly";
            selectToggle(binding.btnCycleYearly, binding.btnCycleMonthly);
        });

        List<TextView> categoryViews = Arrays.asList(
                binding.catStreaming,
                binding.catFood,
                binding.catUtilities,
                binding.catOther
        );
        for (TextView chip : categoryViews) {
            chip.setOnClickListener(v -> {
                selectedCategory = chip.getText().toString();
                updateCategorySelection(categoryViews, chip);
            });
        }

        binding.btnPickDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(
                    requireContext(),
                    (datePicker, year, month, dayOfMonth) -> {
                        String[] months = new String[]{
                                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
                        };
                        selectedRenewalDate = String.format(
                                Locale.US,
                                "%04d-%02d-%02d",
                                year,
                                month + 1,
                                dayOfMonth
                        );

                        binding.btnPickDate.setText(months[month] + " " + dayOfMonth + ", " + year);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        binding.btnSaveSubscription.setOnClickListener(v -> attemptSaveSubscription());

        // Check whether we're adding or editing a subscription
        if (editingSubscriptionId != null) {

            // Existing subscription: load its saved information
            binding.btnSaveSubscription.setText("Save Changes");
            loadSubscriptionForEditing();

        } else {

            // New subscription: use the empty form
            binding.btnSaveSubscription.setText("Save Subscription");
        }
    }


    private void attemptSaveSubscription() {

        boolean wantsReminder = binding.switchReminder.isChecked();

        boolean needsPermission =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                        && ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED;

        if (wantsReminder && needsPermission) {
            notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
            );
            return;
        }

        saveSubscription();
    }



    private void loadSubscriptionForEditing() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null || editingSubscriptionId == null) {
            Toast.makeText(requireContext(),
                    "Unable to load subscription",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference subscriptionRef = FirebaseDatabase.getInstance()
                .getReference("subscriptions")
                .child(user.getUid())
                .child(editingSubscriptionId);

        // Disable saving until the existing data has loaded
        binding.btnSaveSubscription.setEnabled(false);

        subscriptionRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (binding == null || !isAdded()) return;

                if (!snapshot.exists()) {
                    Toast.makeText(requireContext(),
                            "Subscription not found",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Subscription subscription =
                        snapshot.getValue(Subscription.class);

                if (subscription == null) {
                    Toast.makeText(requireContext(),
                            "Unable to read subscription",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Fill in the existing service information
                binding.etServiceName.setText(subscription.getName());

                binding.etCost.setText(String.format(
                        Locale.US,
                        "%.2f",
                        subscription.getCost()
                ));

                // Restore the billing cycle selection
                selectedBillingCycle = "Yearly".equalsIgnoreCase(
                        subscription.getBillingCycle())
                        ? "Yearly" : "Monthly";

                if ("Yearly".equals(selectedBillingCycle)) {
                    selectToggle(
                            binding.btnCycleYearly,
                            binding.btnCycleMonthly
                    );
                } else {
                    selectToggle(
                            binding.btnCycleMonthly,
                            binding.btnCycleYearly
                    );
                }

                // Restore the selected category
                selectedCategory = subscription.getCategory() != null
                        ? subscription.getCategory() : "Other";

                List<TextView> categoryViews = Arrays.asList(
                        binding.catStreaming,
                        binding.catFood,
                        binding.catUtilities,
                        binding.catOther
                );

                TextView selectedChip;

                switch (selectedCategory) {
                    case "Streaming":
                        selectedChip = binding.catStreaming;
                        break;

                    case "Food":
                        selectedChip = binding.catFood;
                        break;

                    case "Utilities":
                        selectedChip = binding.catUtilities;
                        break;

                    default:
                        selectedCategory = "Other";
                        selectedChip = binding.catOther;
                        break;
                }

                updateCategorySelection(categoryViews, selectedChip);

                // Restore the renewal date
                selectedRenewalDate = subscription.getNextRenewalDate();

                if (selectedRenewalDate != null
                        && !selectedRenewalDate.isEmpty()) {
                    binding.btnPickDate.setText(selectedRenewalDate);
                } else {
                    selectedRenewalDate = null;
                    binding.btnPickDate.setText("Pick a date");
                }

                // Restore the reminder preference
                binding.switchReminder.setChecked(
                        subscription.isReminderEnabled()
                );

                binding.btnSaveSubscription.setEnabled(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                if (binding == null || !isAdded()) return;

                Toast.makeText(requireContext(),
                        "Failed to load: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }


    private void saveSubscription() {

        String name = binding.etServiceName.getText().toString().trim();
        String costText = binding.etCost.getText().toString().trim();

        // Check required fields
        if (name.isEmpty()) {
            binding.etServiceName.setError("Enter a service name");
            binding.etServiceName.requestFocus();
            return;
        }

        if (costText.isEmpty()) {
            binding.etCost.setError("Enter the subscription cost");
            binding.etCost.requestFocus();
            return;
        }

        double cost;

        try {
            cost = Double.parseDouble(costText);
        } catch (NumberFormatException e) {
            binding.etCost.setError("Enter a valid amount");
            return;
        }

        if (!Double.isFinite(cost) || cost <= 0 || cost > 1000000) {
            binding.etCost.setError("Enter a valid subscription cost");
            return;
        }

        if (selectedRenewalDate == null) {
            Toast.makeText(requireContext(),
                    "Please select a renewal date",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Save under the currently signed-in account
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(requireContext(),
                    "Please log in before adding a subscription",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference subscriptionsRef = FirebaseDatabase.getInstance()
                .getReference("subscriptions")
                .child(user.getUid());


        boolean isEditing = editingSubscriptionId != null;

        // Reuse the existing ID when editing.
        // Generate a new ID only when adding a subscription.
        String subscriptionId = isEditing
                ? editingSubscriptionId
                : subscriptionsRef.push().getKey();

        if (subscriptionId == null || subscriptionId.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Unable to identify subscription",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> subscription = new HashMap<>();

        subscription.put("name", name);
        subscription.put("cost", Math.round(cost * 100.0) / 100.0);
        subscription.put("currency", "CAD");
        subscription.put("billingCycle", selectedBillingCycle);
        subscription.put("category", selectedCategory);
        subscription.put("nextRenewalDate", selectedRenewalDate);
        subscription.put("reminderEnabled", binding.switchReminder.isChecked());

        DatabaseReference subscriptionRef =
                subscriptionsRef.child(subscriptionId);

        binding.btnSaveSubscription.setEnabled(false);

        // Editing updates existing fields. Adding creates a new record.
        com.google.android.gms.tasks.Task<Void> saveTask;

        if (isEditing) {
            saveTask = subscriptionRef.updateChildren(subscription);
        } else {
            subscription.put("id", subscriptionId);
            saveTask = subscriptionRef.setValue(subscription);
        }

        saveTask.addOnSuccessListener(unused -> {

            if (binding == null || !isAdded()) return;

            binding.btnSaveSubscription.setEnabled(true);

            //Schedule or cancel the renewal reminder
            if (binding.switchReminder.isChecked()) {

                ReminderScheduler.scheduleReminder(
                        requireContext(),
                        user.getUid(),
                        subscriptionId,
                        name,
                        selectedRenewalDate,
                        selectedBillingCycle
                );

            } else {

                ReminderScheduler.cancelReminder(
                        requireContext(),
                        user.getUid(),
                        subscriptionId
                );
            }

            String message = isEditing
                    ? "Subscription updated successfully"
                    : "Subscription saved successfully";

            Toast.makeText(requireContext(),
                    message,
                    Toast.LENGTH_SHORT).show();

            NavHostFragment.findNavController(this).navigate(
                    R.id.action_addSubscriptionFragment_to_subscriptionsFragment
            );

        }).addOnFailureListener(e -> {

            if (binding == null || !isAdded()) return;

            binding.btnSaveSubscription.setEnabled(true);

            Toast.makeText(requireContext(),
                    "Failed to save: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        });

    }


    private void selectToggle(TextView selected, TextView unselected) {
        int greenColor = ContextCompat.getColor(requireContext(), R.color.primary_green);
        int onGreenColor = ContextCompat.getColor(requireContext(), R.color.button_on_green);
        int secondaryText = ContextCompat.getColor(requireContext(), R.color.text_secondary);

        selected.setBackgroundResource(R.drawable.bg_pill_green);
        selected.setBackgroundTintList(ColorStateList.valueOf(greenColor));
        selected.setTextColor(onGreenColor);

        unselected.setBackground(null);
        unselected.setTextColor(secondaryText);
    }

    private void updateCategorySelection(List<TextView> all, TextView selected) {
        int greenColor = ContextCompat.getColor(requireContext(), R.color.primary_green);
        int onGreenColor = ContextCompat.getColor(requireContext(), R.color.button_on_green);
        int secondaryText = ContextCompat.getColor(requireContext(), R.color.text_secondary);

        for (TextView view : all) {
            if (view == selected) {
                view.setBackgroundResource(R.drawable.bg_pill_green);
                view.setBackgroundTintList(ColorStateList.valueOf(greenColor));
                view.setTextColor(onGreenColor);
            } else {
                view.setBackgroundResource(R.drawable.bg_pill_elevated);
                view.setBackgroundTintList(null);
                view.setTextColor(secondaryText);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
