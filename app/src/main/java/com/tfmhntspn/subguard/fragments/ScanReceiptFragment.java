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
import com.tfmhntspn.subguard.databinding.FragmentScanReceiptBinding;

public class ScanReceiptFragment extends Fragment {

    private FragmentScanReceiptBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentScanReceiptBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnCloseScanner.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        binding.btnFlash.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Flashlight toggled", Toast.LENGTH_SHORT).show());

        binding.btnCaptureReceipt.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_scanReceiptFragment_to_reviewScanDetailsFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
