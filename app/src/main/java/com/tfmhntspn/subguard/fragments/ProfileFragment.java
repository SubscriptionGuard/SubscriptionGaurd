package com.tfmhntspn.subguard.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tfmhntspn.subguard.R;
import com.tfmhntspn.subguard.databinding.FragmentProfileBinding;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileFragment extends Fragment {

    public static final String KEY_RENEWAL_ALERTS = "renewal_alerts_enabled";
    public static final String KEY_MONTHLY_BUDGET = "monthly_budget";
    public static final String FIELD_RENEWAL_ALERTS = "renewalAlerts";

    private FragmentProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadProfileDetails();

        binding.switchRenewalAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            requireContext()
                    .getSharedPreferences(HomeFragment.PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(KEY_RENEWAL_ALERTS, isChecked)
                    .apply();

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(user.getUid())
                        .child(FIELD_RENEWAL_ALERTS)
                        .setValue(isChecked);
            }
        });

        binding.btnSaveChanges.setOnClickListener(v -> saveProfileChanges());

        binding.btnExitDemo.setOnClickListener(v -> {
            requireContext()
                    .getSharedPreferences(HomeFragment.PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .remove(HomeFragment.KEY_CUSTOM_NAME)
                    .apply();
            FirebaseAuth.getInstance().signOut();
            NavHostFragment.findNavController(this).navigate(R.id.action_profileFragment_to_welcomeFragment);
        });

        binding.btnDeleteAccount.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_profileFragment_to_deleteAccountFragment));
    }

    private void loadProfileDetails() {
        SharedPreferences prefs = requireContext()
                .getSharedPreferences(HomeFragment.PREFS_NAME, Context.MODE_PRIVATE);

        String resolvedName = HomeFragment.resolveUserDisplayName(requireContext());
        binding.tvProfileName.setText(resolvedName);
        binding.etProfileName.setText(resolvedName);
        binding.tvProfileInitials.setText(HomeFragment.extractInitials(resolvedName));

        boolean alertsEnabled = prefs.getBoolean(KEY_RENEWAL_ALERTS, true);
        float savedBudget = prefs.getFloat(KEY_MONTHLY_BUDGET, 150.0f);
        binding.switchRenewalAlerts.setChecked(alertsEnabled);
        binding.etMonthlyBudget.setText(String.format(Locale.US, "$%.2f", savedBudget));

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            if (email != null && !email.trim().isEmpty()) {
                binding.tvProfileEmail.setText(email);
                binding.etProfileEmail.setText(email);
            }

            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(currentUser.getUid());

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (binding == null) return;

                    Double budgetVal = snapshot.child(HomeFragment.FIELD_MONTHLY_BUDGET).getValue(Double.class);
                    if (budgetVal != null && budgetVal > 0) {
                        binding.etMonthlyBudget.setText(String.format(Locale.US, "$%.2f", budgetVal));
                    }

                    Boolean alertsVal = snapshot.child(FIELD_RENEWAL_ALERTS).getValue(Boolean.class);
                    if (alertsVal != null) {
                        binding.switchRenewalAlerts.setChecked(alertsVal);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    private void saveProfileChanges() {
        String updatedName = binding.etProfileName.getText() != null
                ? binding.etProfileName.getText().toString().trim() : "";
        String updatedEmail = binding.etProfileEmail.getText() != null
                ? binding.etProfileEmail.getText().toString().trim() : "";
        String rawBudget = binding.etMonthlyBudget.getText() != null
                ? binding.etMonthlyBudget.getText().toString().replaceAll("[^0-9.]", "") : "";
        boolean alertsEnabled = binding.switchRenewalAlerts.isChecked();

        double parsedBudget = 150.0;
        if (!rawBudget.isEmpty()) {
            try {
                parsedBudget = Double.parseDouble(rawBudget);
            } catch (NumberFormatException ignored) {
            }
        }
        binding.etMonthlyBudget.setText(String.format(Locale.US, "$%.2f", parsedBudget));

        SharedPreferences.Editor editor = requireContext()
                .getSharedPreferences(HomeFragment.PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_RENEWAL_ALERTS, alertsEnabled)
                .putFloat(KEY_MONTHLY_BUDGET, (float) parsedBudget);

        if (!updatedName.isEmpty()) {
            binding.tvProfileName.setText(updatedName);
            binding.tvProfileInitials.setText(HomeFragment.extractInitials(updatedName));
            editor.putString(HomeFragment.KEY_CUSTOM_NAME, updatedName);
        }
        editor.apply();

        if (!updatedEmail.isEmpty()) {
            binding.tvProfileEmail.setText(updatedEmail);
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            if (!updatedName.isEmpty()) {
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(updatedName)
                        .build();
                user.updateProfile(profileUpdates);
            }

            Map<String, Object> updates = new HashMap<>();
            if (!updatedName.isEmpty()) {
                updates.put("name", updatedName);
            }
            updates.put(HomeFragment.FIELD_MONTHLY_BUDGET, parsedBudget);
            updates.put(FIELD_RENEWAL_ALERTS, alertsEnabled);

            FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid())
                    .updateChildren(updates);
        }

        Toast.makeText(requireContext(), "Profile settings saved", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
