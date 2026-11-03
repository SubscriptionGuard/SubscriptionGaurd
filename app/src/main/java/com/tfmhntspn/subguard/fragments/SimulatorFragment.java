package com.tfmhntspn.subguard.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
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
import com.tfmhntspn.subguard.databinding.FragmentSimulatorBinding;

public class SimulatorFragment extends Fragment {

    private FragmentSimulatorBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSimulatorBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        binding.btnSimAll.setOnClickListener(v -> {
            selectMode(binding.btnSimAll, binding.btnSimUnused);
            binding.tvSimAnnualSpend.setText("$1,529.76");
            binding.tvEquivGroceries.setText("15 weeks");
            binding.tvEquivCoffees.setText("306");
            binding.tvEquivFlights.setText("2 flights");
            binding.tvEquivTextbooks.setText("3 terms");
            binding.tvSimFiveYearTotal.setText("$8,560");
        });

        binding.btnSimUnused.setOnClickListener(v -> {
            selectMode(binding.btnSimUnused, binding.btnSimAll);
            binding.tvSimAnnualSpend.setText("$239.88");
            binding.tvEquivGroceries.setText("2.5 weeks");
            binding.tvEquivCoffees.setText("48");
            binding.tvEquivFlights.setText("1 weekend");
            binding.tvEquivTextbooks.setText("2 books");
            binding.tvSimFiveYearTotal.setText("$1,342");
        });

        binding.btnReviewUnused.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_simulatorFragment_to_subscriptionsFragment));
    }

    private void selectMode(TextView selected, TextView unselected) {
        int greenColor = ContextCompat.getColor(requireContext(), R.color.primary_green);
        int onGreenColor = ContextCompat.getColor(requireContext(), R.color.button_on_green);
        int secondaryText = ContextCompat.getColor(requireContext(), R.color.text_secondary);

        selected.setBackgroundResource(R.drawable.bg_pill_green);
        selected.setBackgroundTintList(ColorStateList.valueOf(greenColor));
        selected.setTextColor(onGreenColor);

        unselected.setBackground(null);
        unselected.setTextColor(secondaryText);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
