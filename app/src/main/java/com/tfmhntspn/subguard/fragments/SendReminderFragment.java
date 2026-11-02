package com.tfmhntspn.subguard.fragments;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.tfmhntspn.subguard.R;
import com.tfmhntspn.subguard.databinding.FragmentSendReminderBinding;

import java.util.Arrays;
import java.util.List;

public class SendReminderFragment extends Fragment {

    private FragmentSendReminderBinding binding;
    private boolean isEmailChannel = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSendReminderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            String roommateName = args.getString("roommate_name");
            if (roommateName != null) {
                binding.tvRecipientName.setText(roommateName);
            }
        }

        binding.btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        binding.btnChannelSms.setOnClickListener(v -> {
            isEmailChannel = false;
            selectSegment(binding.btnChannelSms, binding.btnChannelEmail);
            binding.layoutEmailHeaders.setVisibility(View.GONE);
            binding.tvChannelHelper.setText("Opens your messaging app with this text filled in.");
            binding.btnSendReminder.setText("Send via SMS");
        });

        binding.btnChannelEmail.setOnClickListener(v -> {
            isEmailChannel = true;
            selectSegment(binding.btnChannelEmail, binding.btnChannelSms);
            binding.layoutEmailHeaders.setVisibility(View.VISIBLE);
            binding.tvChannelHelper.setText("Opens your email app with this filled in.");
            binding.btnSendReminder.setText("Send email");
        });

        List<TextView> toneChips = Arrays.asList(
                binding.chipToneFriendly,
                binding.chipToneShort,
                binding.chipToneFirm
        );
        binding.chipToneFriendly.setOnClickListener(v -> {
            selectTone(toneChips, binding.chipToneFriendly);
            binding.etMessagePreview.setText(
                    "Hey Maya! Quick reminder: your share of Telus WiFi ($10.00) and October hydro ($16.50) comes to $26.50. E-transfer whenever you get a chance. Thanks! – Sophia"
            );
        });
        binding.chipToneShort.setOnClickListener(v -> {
            selectTone(toneChips, binding.chipToneShort);
            binding.etMessagePreview.setText(
                    "Hi Maya, your October share for Telus WiFi ($10.00) + BC Hydro ($16.50) is $26.50. Please e-transfer when you can!"
            );
        });
        binding.chipToneFirm.setOnClickListener(v -> {
            selectTone(toneChips, binding.chipToneFirm);
            binding.etMessagePreview.setText(
                    "Hi Maya, following up on the $26.50 balance for Telus WiFi ($10.00) and October hydro ($16.50). Please send the e-transfer today so we can settle this month's bills."
            );
        });

        binding.btnEditMessage.setOnClickListener(v -> binding.etMessagePreview.requestFocus());

        binding.btnSendReminder.setOnClickListener(v -> {
            String message = binding.etMessagePreview.getText() != null
                    ? binding.etMessagePreview.getText().toString() : "";
            if (isEmailChannel) {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:maya.chen@gmail.com"));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Maple St. House · October");
                intent.putExtra(Intent.EXTRA_TEXT, message);
                startActivity(Intent.createChooser(intent, "Send email reminder"));
            } else {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:6045550142"));
                intent.putExtra("sms_body", message);
                startActivity(intent);
            }
        });

        binding.btnMarkSettled.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Marked balance as settled!", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigateUp();
        });
    }

    private void selectSegment(TextView selected, TextView unselected) {
        int greenColor = ContextCompat.getColor(requireContext(), R.color.primary_green);
        int onGreenColor = ContextCompat.getColor(requireContext(), R.color.button_on_green);
        int secondaryText = ContextCompat.getColor(requireContext(), R.color.text_secondary);

        selected.setBackgroundResource(R.drawable.bg_pill_green);
        selected.setBackgroundTintList(ColorStateList.valueOf(greenColor));
        selected.setTextColor(onGreenColor);

        unselected.setBackground(null);
        unselected.setTextColor(secondaryText);
    }

    private void selectTone(List<TextView> all, TextView active) {
        int greenColor = ContextCompat.getColor(requireContext(), R.color.primary_green);
        int onGreenColor = ContextCompat.getColor(requireContext(), R.color.button_on_green);
        int secondaryText = ContextCompat.getColor(requireContext(), R.color.text_secondary);

        for (TextView chip : all) {
            if (chip == active) {
                chip.setBackgroundResource(R.drawable.bg_pill_green);
                chip.setBackgroundTintList(ColorStateList.valueOf(greenColor));
                chip.setTextColor(onGreenColor);
            } else {
                chip.setBackgroundResource(R.drawable.bg_pill_elevated);
                chip.setBackgroundTintList(null);
                chip.setTextColor(secondaryText);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
