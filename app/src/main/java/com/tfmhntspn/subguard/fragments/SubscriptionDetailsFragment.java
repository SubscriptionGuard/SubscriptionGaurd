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

import com.tfmhntspn.subguard.R;
import com.tfmhntspn.subguard.databinding.FragmentSubscriptionDetailsBinding;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tfmhntspn.subguard.reminders.ReminderScheduler;

public class SubscriptionDetailsFragment extends Fragment {

    private FragmentSubscriptionDetailsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSubscriptionDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            String serviceName = args.getString("service_name");
            if (serviceName != null) binding.tvServiceName.setText(serviceName);

            String serviceBadge = args.getString("service_badge");
            if (serviceBadge != null) binding.tvServiceBadge.setText(serviceBadge);

            String servicePrice = args.getString("service_price");
            if (servicePrice != null) binding.tvServicePrice.setText(servicePrice);

            String servicePlan = args.getString("service_plan");
            if (servicePlan != null) binding.tvServicePlan.setText(servicePlan);

            String nextRenewal = args.getString("next_renewal");
            if (nextRenewal != null) binding.tvNextRenewal.setText(nextRenewal);

            String category = args.getString("category");
            if (category != null) binding.tvServiceCategory.setText(category);

            String yearlyCost = args.getString("yearly_cost");
            if (yearlyCost != null) binding.tvYearlyCost.setText(yearlyCost);
        }

        binding.btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        binding.btnEditSubscription.setOnClickListener(v -> {

            String subscriptionId = args != null
                    ? args.getString("subscription_id") : null;

            if (subscriptionId == null || subscriptionId.isEmpty()) {
                Toast.makeText(requireContext(),
                        "Subscription ID not found",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Bundle editArgs = new Bundle();
            editArgs.putString("subscription_id", subscriptionId);

            NavHostFragment.findNavController(this).navigate(
                    R.id.action_subscriptionDetailsFragment_to_addSubscriptionFragment,
                    editArgs
            );
        });

        binding.cardHowToCancel.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_subscriptionDetailsFragment_to_cancellationGuideFragment));

        binding.cardWastedMoney.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_subscriptionDetailsFragment_to_simulatorFragment));

        binding.btnDeleteSubscription.setOnClickListener(v ->
                confirmDeleteSubscription());
    }


    private void confirmDeleteSubscription() {

        Bundle args = getArguments();
        String subscriptionId = args != null
                ? args.getString("subscription_id") : null;

        if (subscriptionId == null || subscriptionId.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Subscription ID not found",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String name = binding.tvServiceName.getText().toString();

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete subscription?")
                .setMessage("Are you sure you want to delete " + name + "?")
                .setNegativeButton("Cancel", (dialog, which) ->
                        dialog.dismiss())
                .setPositiveButton("Delete", (dialog, which) ->
                        deleteSubscription(subscriptionId))
                .show();
    }

    private void deleteSubscription(String subscriptionId) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(requireContext(),
                    "Please log in to delete subscriptions",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference subscriptionRef = FirebaseDatabase.getInstance()
                .getReference("subscriptions")
                .child(user.getUid())
                .child(subscriptionId);

        binding.btnDeleteSubscription.setEnabled(false);

        subscriptionRef.removeValue()
                .addOnSuccessListener(unused -> {
                    if (binding == null || !isAdded()) return;

                    ReminderScheduler.cancelReminder(
                            requireContext(),
                            user.getUid(),
                            subscriptionId
                    );

                    Toast.makeText(requireContext(),
                            "Subscription deleted successfully",
                            Toast.LENGTH_SHORT).show();

                    NavHostFragment.findNavController(this).navigateUp();
                })
                .addOnFailureListener(e -> {
                    if (binding == null || !isAdded()) return;

                    binding.btnDeleteSubscription.setEnabled(true);

                    Toast.makeText(requireContext(),
                            "Unable to delete: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
