package com.tfmhntspn.subguard.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.tfmhntspn.subguard.R;
import com.tfmhntspn.subguard.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

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

        binding.cardRenewalNetflix.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("service_name", "Netflix");
            args.putString("service_badge", "N");
            args.putString("service_price", "$16.99");
            args.putString("service_plan", "/ month · Standard");
            args.putString("next_renewal", "Oct 24, 2026");
            args.putString("category", "Streaming");
            args.putString("yearly_cost", "$203.88");
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_homeFragment_to_subscriptionDetailsFragment, args);
        });

        binding.cardRenewalSpotify.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("service_name", "Spotify");
            args.putString("service_badge", "S");
            args.putString("service_price", "$11.99");
            args.putString("service_plan", "/ month · Individual");
            args.putString("next_renewal", "Oct 27, 2026");
            args.putString("category", "Streaming");
            args.putString("yearly_cost", "$143.88");
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_homeFragment_to_subscriptionDetailsFragment, args);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
