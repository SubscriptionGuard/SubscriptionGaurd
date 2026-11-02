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

        binding.btnEditSubscription.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_subscriptionDetailsFragment_to_addSubscriptionFragment));

        binding.cardHowToCancel.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_subscriptionDetailsFragment_to_cancellationGuideFragment));

        binding.cardWastedMoney.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_subscriptionDetailsFragment_to_simulatorFragment));

        binding.btnDeleteSubscription.setOnClickListener(v -> {
            String name = binding.tvServiceName.getText().toString();
            Toast.makeText(requireContext(), name + " removed", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigateUp();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
