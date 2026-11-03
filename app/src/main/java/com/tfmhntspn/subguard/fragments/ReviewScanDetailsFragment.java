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
import com.tfmhntspn.subguard.databinding.FragmentReviewScanDetailsBinding;

public class ReviewScanDetailsFragment extends Fragment {

    private FragmentReviewScanDetailsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentReviewScanDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        binding.btnScanAgain.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        binding.btnSaveScanned.setOnClickListener(v -> {
            String serviceName = binding.etScannedService.getText() != null
                    ? binding.etScannedService.getText().toString().trim() : "";
            Toast.makeText(
                    requireContext(),
                    serviceName + " saved from receipt!",
                    Toast.LENGTH_SHORT
            ).show();
            NavHostFragment.findNavController(this).navigate(R.id.action_reviewScanDetailsFragment_to_subscriptionsFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
