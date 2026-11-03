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

import com.tfmhntspn.subguard.databinding.FragmentReviewChangeBinding;

public class ReviewChangeFragment extends Fragment {

    private FragmentReviewChangeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentReviewChangeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        binding.btnAcceptChange.setOnClickListener(v -> {
            Toast.makeText(
                    requireContext(),
                    "Telus WiFi split change accepted!",
                    Toast.LENGTH_SHORT
            ).show();
            NavHostFragment.findNavController(this).navigateUp();
        });

        binding.btnDeclineChange.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Split change declined", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigateUp();
        });

        binding.btnSuggestSplit.setOnClickListener(v -> {
            Toast.makeText(
                    requireContext(),
                    "Sent counter-proposal to Sophia",
                    Toast.LENGTH_SHORT
            ).show();
            NavHostFragment.findNavController(this).navigateUp();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
