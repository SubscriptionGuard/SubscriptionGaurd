package com.tfmhntspn.subguard.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.tfmhntspn.subguard.R;
import com.tfmhntspn.subguard.databinding.FragmentRoommateSplitsV1Binding;

public class RoommateSplitsV1Fragment extends Fragment {

    private FragmentRoommateSplitsV1Binding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRoommateSplitsV1Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        binding.rowMayaTelus.setOnClickListener(v ->
                navigateToReminder("Maya Chen", "$26.50"));

        binding.rowMayaHydro.setOnClickListener(v ->
                navigateToReminder("Maya Chen", "$26.50"));

        binding.rowJordanHydro.setOnClickListener(v ->
                navigateToReminder("Jordan Lee", "$16.50"));
    }

    private void navigateToReminder(String roommateName, String amountOwed) {
        Bundle args = new Bundle();
        args.putString("roommate_name", roommateName);
        args.putString("amount_owed", amountOwed);
        NavHostFragment.findNavController(this).navigate(
                R.id.action_roommateSplitsV1Fragment_to_sendReminderFragment,
                args
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
