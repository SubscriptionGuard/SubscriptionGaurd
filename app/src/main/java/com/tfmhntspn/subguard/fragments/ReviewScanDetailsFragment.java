
package com.tfmhntspn.subguard.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tfmhntspn.subguard.R;
import com.tfmhntspn.subguard.databinding.FragmentReviewScanDetailsBinding;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReviewScanDetailsFragment extends Fragment {

    private FragmentReviewScanDetailsBinding binding;

    private String scannedText = "";
    private String selectedRenewalDate = "";

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentReviewScanDetailsBinding.inflate(
                inflater, container, false
        );

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();

        if (args != null) {
            scannedText = args.getString("scanned_text", "");
        }

        populateScannedDetails();

        binding.etScannedDate.setOnClickListener(v ->
                showDatePicker()
        );

        binding.btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp()
        );

        binding.btnScanAgain.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp()
        );

        binding.btnSaveScanned.setOnClickListener(v ->
                validateSubscription()
        );
    }

    private void populateScannedDetails() {

        if (scannedText.trim().isEmpty()) {
            showMessage("No scanned text was received");
            return;
        }

        String serviceName = extractServiceName(scannedText);
        String cost = extractCost(scannedText);

        if (!serviceName.isEmpty()) {
            binding.etScannedService.setText(serviceName);
        }

        if (!cost.isEmpty()) {
            binding.etScannedCost.setText(cost);
        }

        String lowerText = scannedText.toLowerCase(Locale.ROOT);

        if (lowerText.contains("annual")
                || lowerText.contains("yearly")
                || lowerText.contains("per year")) {

            binding.spinnerScannedBillingCycle.setSelection(1);
        }

        if (lowerText.contains("netflix")
                || lowerText.contains("spotify")
                || lowerText.contains("disney")
                || lowerText.contains("crunchyroll")) {

            binding.spinnerScannedCategory.setSelection(0);
        }
    }

    private String extractServiceName(String text) {

        String[] services = {
                "Netflix",
                "Spotify",
                "Disney+",
                "YouTube Premium",
                "Amazon Prime",
                "Apple Music",
                "Apple TV",
                "Crunchyroll",
                "Hulu",
                "Adobe",
                "Microsoft 365"
        };

        for (String service : services) {
            if (text.toLowerCase(Locale.ROOT).contains(
                    service.toLowerCase(Locale.ROOT))) {
                return service;
            }
        }

        return "";
    }

    private String extractCost(String text) {

        // Prefer an explicitly labelled total.
        Pattern totalPattern = Pattern.compile(
                "(?im)\\b(?:grand\\s+total|amount\\s+paid|total\\s+paid|total\\s+due|total)\\b" +
                        "[^\\r\\n\\d]{0,25}" +
                        "(?:CAD\\s*)?\\$?\\s*(\\d{1,6}[.,]\\d{2})"
        );

        Matcher totalMatcher = totalPattern.matcher(text);

        String lastTotal = "";

        while (totalMatcher.find()) {
            lastTotal = totalMatcher.group(1);
        }

        if (!lastTotal.isEmpty()) {
            return lastTotal.replace(',', '.');
        }

        // For receipts with a single clearly identified amount.
        Pattern pricePattern = Pattern.compile(
                "(?i)(?:CAD\\s*\\$?\\s*|\\$\\s*)" +
                        "(\\d{1,6}[.,]\\d{2})"
        );

        Matcher priceMatcher = pricePattern.matcher(text);

        String foundPrice = "";
        int matches = 0;

        while (priceMatcher.find()) {
            foundPrice = priceMatcher.group(1);
            matches++;
        }

        if (matches == 1) {
            return foundPrice.replace(',', '.');
        }

        return "";
    }

    private void showDatePicker() {

        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePicker = new DatePickerDialog(
                requireContext(),
                (datePickerView, year, month, day) -> {

                    selectedRenewalDate = String.format(
                            Locale.US,
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            day
                    );

                    binding.etScannedDate.setText(
                            selectedRenewalDate
                    );
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        datePicker.getDatePicker().setMinDate(
                today.getTimeInMillis()
        );

        datePicker.show();
    }

    private void validateSubscription() {

        String serviceName = binding.etScannedService.getText()
                .toString().trim();

        String costText = binding.etScannedCost.getText()
                .toString().trim();

        if (serviceName.isEmpty()) {
            binding.etScannedService.setError(
                    "Enter a service name"
            );
            return;
        }

        if (costText.isEmpty()) {
            binding.etScannedCost.setError(
                    "Enter the subscription cost"
            );
            return;
        }

        double cost;

        try {
            cost = Double.parseDouble(costText);
        } catch (NumberFormatException e) {
            binding.etScannedCost.setError(
                    "Enter a valid amount"
            );
            return;
        }

        if (!Double.isFinite(cost) || cost <= 0) {
            binding.etScannedCost.setError(
                    "Cost must be greater than zero"
            );
            return;
        }

        if (selectedRenewalDate.isEmpty()) {
            showMessage("Select the next renewal date");
            return;
        }

        String billingCycle = binding.spinnerScannedBillingCycle
                .getSelectedItem().toString();

        String category = binding.spinnerScannedCategory
                .getSelectedItem().toString();

        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm subscription")
                .setMessage(
                        "Service: " + serviceName +
                                "\nCost: CAD $" +
                                String.format(Locale.US, "%.2f", cost) +
                                "\nBilling: " + billingCycle +
                                "\nCategory: " + category +
                                "\nNext renewal: " + selectedRenewalDate +
                                "\n\nIs everything correct?"
                )
                .setNegativeButton("Edit", null)
                .setPositiveButton("Save", (dialog, which) ->
                        saveSubscription(
                                serviceName,
                                cost,
                                billingCycle,
                                category,
                                selectedRenewalDate
                        )
                )
                .show();
    }

    private void saveSubscription(
            String serviceName,
            double cost,
            String billingCycle,
            String category,
            String renewalDate
    ) {

        FirebaseUser user = FirebaseAuth.getInstance()
                .getCurrentUser();

        if (user == null) {
            showMessage("Please sign in to save a subscription");
            return;
        }

        String uid = user.getUid();

        DatabaseReference subscriptionsRef =
                FirebaseDatabase.getInstance()
                        .getReference("subscriptions")
                        .child(uid);

        DatabaseReference newSubscriptionRef =
                subscriptionsRef.push();

        String subscriptionId = newSubscriptionRef.getKey();

        if (subscriptionId == null) {
            showMessage("Unable to create subscription");
            return;
        }

        Map<String, Object> subscription = new HashMap<>();

        subscription.put("id", subscriptionId);
        subscription.put("name", serviceName);
        subscription.put("cost", cost);
        subscription.put("currency", "CAD");
        subscription.put("billingCycle", billingCycle);
        subscription.put("category", category);
        subscription.put("nextRenewalDate", renewalDate);

        // Scanned subscriptions start with reminders disabled.
        subscription.put("reminderEnabled", false);

        binding.btnSaveScanned.setEnabled(false);

        newSubscriptionRef.setValue(subscription)
                .addOnSuccessListener(unused -> {

                    if (!isAdded() || binding == null) return;

                    showMessage("Subscription saved successfully");

                    NavHostFragment.findNavController(this).navigate(
                            R.id.action_reviewScanDetailsFragment_to_subscriptionsFragment
                    );
                })
                .addOnFailureListener(e -> {

                    if (!isAdded() || binding == null) return;

                    binding.btnSaveScanned.setEnabled(true);

                    showMessage(
                            "Unable to save: " + e.getMessage()
                    );
                });
    }

    private void showMessage(String message) {

        if (!isAdded()) return;

        Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_LONG
        ).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
