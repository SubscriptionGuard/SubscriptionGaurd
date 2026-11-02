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

import com.tfmhntspn.subguard.databinding.FragmentHouseholdSettingsBinding;

public class HouseholdSettingsFragment extends Fragment {

    private FragmentHouseholdSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHouseholdSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        binding.btnRemoveMaya.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Maya removed from household", Toast.LENGTH_SHORT).show());

        binding.btnRemoveJordan.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Jordan removed from household", Toast.LENGTH_SHORT).show());

        binding.btnCancelPriya.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Invite to Priya cancelled", Toast.LENGTH_SHORT).show());

        binding.btnDeleteHousehold.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Household deleted", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigateUp();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
