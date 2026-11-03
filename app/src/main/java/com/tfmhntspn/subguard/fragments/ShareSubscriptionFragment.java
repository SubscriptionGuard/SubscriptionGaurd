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
import com.tfmhntspn.subguard.databinding.FragmentShareSubscriptionBinding;

public class ShareSubscriptionFragment extends Fragment {

    private FragmentShareSubscriptionBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentShareSubscriptionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        binding.pickNetflix.setOnClickListener(v -> {
            binding.pickNetflix.setBackgroundResource(R.drawable.bg_pill_green);
            binding.pickSpotify.setBackgroundResource(R.drawable.bg_card_rounded);
            binding.pickCrave.setBackgroundResource(R.drawable.bg_card_rounded);
            binding.tvSplitHeader.setText("2 · SPLIT $16.99 WITH");
            binding.tvShareYou.setText("$5.67");
            binding.tvShareMaya.setText("$5.66");
            binding.tvShareJordan.setText("$5.66");
        });

        binding.pickSpotify.setOnClickListener(v -> {
            binding.pickNetflix.setBackgroundResource(R.drawable.bg_card_rounded);
            binding.pickSpotify.setBackgroundResource(R.drawable.bg_pill_green);
            binding.pickCrave.setBackgroundResource(R.drawable.bg_card_rounded);
            binding.tvSplitHeader.setText("2 · SPLIT $11.99 WITH");
            binding.tvShareYou.setText("$4.00");
            binding.tvShareMaya.setText("$4.00");
            binding.tvShareJordan.setText("$3.99");
        });

        binding.pickCrave.setOnClickListener(v -> {
            binding.pickNetflix.setBackgroundResource(R.drawable.bg_card_rounded);
            binding.pickSpotify.setBackgroundResource(R.drawable.bg_card_rounded);
            binding.pickCrave.setBackgroundResource(R.drawable.bg_pill_green);
            binding.tvSplitHeader.setText("2 · SPLIT $19.99 WITH");
            binding.tvShareYou.setText("$6.67");
            binding.tvShareMaya.setText("$6.66");
            binding.tvShareJordan.setText("$6.66");
        });

        binding.btnSplitEqual.setOnClickListener(v -> {
            binding.btnSplitEqual.setBackgroundResource(R.drawable.bg_pill_green);
            binding.btnSplitCustom.setBackgroundResource(R.drawable.bg_pill_elevated);
        });

        binding.btnSplitCustom.setOnClickListener(v -> {
            binding.btnSplitCustom.setBackgroundResource(R.drawable.bg_pill_green);
            binding.btnSplitEqual.setBackgroundResource(R.drawable.bg_pill_elevated);
        });

        binding.btnSendForApproval.setOnClickListener(v -> {
            Toast.makeText(
                    requireContext(),
                    "Split sent to Maya and Jordan for approval!",
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
