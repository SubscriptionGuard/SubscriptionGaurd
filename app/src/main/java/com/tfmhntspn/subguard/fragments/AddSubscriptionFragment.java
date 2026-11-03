package com.tfmhntspn.subguard.fragments;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
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
import com.tfmhntspn.subguard.databinding.FragmentAddSubscriptionBinding;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class AddSubscriptionFragment extends Fragment {

    private FragmentAddSubscriptionBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddSubscriptionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        binding.cardScanReceipt.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_addSubscriptionFragment_to_scanReceiptFragment));

        binding.btnCycleMonthly.setOnClickListener(v ->
                selectToggle(binding.btnCycleMonthly, binding.btnCycleYearly));
        binding.btnCycleYearly.setOnClickListener(v ->
                selectToggle(binding.btnCycleYearly, binding.btnCycleMonthly));

        List<TextView> categoryViews = Arrays.asList(
                binding.catStreaming,
                binding.catFood,
                binding.catUtilities,
                binding.catOther
        );
        for (TextView chip : categoryViews) {
            chip.setOnClickListener(v -> updateCategorySelection(categoryViews, chip));
        }

        binding.btnPickDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(
                    requireContext(),
                    (datePicker, year, month, dayOfMonth) -> {
                        String[] months = new String[]{
                                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
                        };
                        binding.btnPickDate.setText(months[month] + " " + dayOfMonth + ", " + year);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        binding.btnSaveSubscription.setOnClickListener(v -> {
            String name = binding.etServiceName.getText() != null
                    ? binding.etServiceName.getText().toString().trim() : "";
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a service name", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(requireContext(), name + " added to subscriptions!", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigate(R.id.action_addSubscriptionFragment_to_subscriptionsFragment);
        });
    }

    private void selectToggle(TextView selected, TextView unselected) {
        int greenColor = ContextCompat.getColor(requireContext(), R.color.primary_green);
        int onGreenColor = ContextCompat.getColor(requireContext(), R.color.button_on_green);
        int secondaryText = ContextCompat.getColor(requireContext(), R.color.text_secondary);

        selected.setBackgroundResource(R.drawable.bg_pill_green);
        selected.setBackgroundTintList(ColorStateList.valueOf(greenColor));
        selected.setTextColor(onGreenColor);

        unselected.setBackground(null);
        unselected.setTextColor(secondaryText);
    }

    private void updateCategorySelection(List<TextView> all, TextView selected) {
        int greenColor = ContextCompat.getColor(requireContext(), R.color.primary_green);
        int onGreenColor = ContextCompat.getColor(requireContext(), R.color.button_on_green);
        int secondaryText = ContextCompat.getColor(requireContext(), R.color.text_secondary);

        for (TextView view : all) {
            if (view == selected) {
                view.setBackgroundResource(R.drawable.bg_pill_green);
                view.setBackgroundTintList(ColorStateList.valueOf(greenColor));
                view.setTextColor(onGreenColor);
            } else {
                view.setBackgroundResource(R.drawable.bg_pill_elevated);
                view.setBackgroundTintList(null);
                view.setTextColor(secondaryText);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
