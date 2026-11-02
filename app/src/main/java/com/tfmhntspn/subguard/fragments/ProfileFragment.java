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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tfmhntspn.subguard.R;
import com.tfmhntspn.subguard.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

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

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.trim().isEmpty()) {
                binding.tvProfileName.setText(displayName);
                binding.etProfileName.setText(displayName);
            }
            String email = currentUser.getEmail();
            if (email != null && !email.trim().isEmpty()) {
                binding.tvProfileEmail.setText(email);
                binding.etProfileEmail.setText(email);
            }
        }

        binding.btnSaveChanges.setOnClickListener(v -> {
            String updatedName = binding.etProfileName.getText() != null
                    ? binding.etProfileName.getText().toString().trim() : "";
            String updatedEmail = binding.etProfileEmail.getText() != null
                    ? binding.etProfileEmail.getText().toString().trim() : "";
            if (!updatedName.isEmpty()) {
                binding.tvProfileName.setText(updatedName);
            }
            if (!updatedEmail.isEmpty()) {
                binding.tvProfileEmail.setText(updatedEmail);
            }
            Toast.makeText(requireContext(), "Profile settings saved", Toast.LENGTH_SHORT).show();
        });

        binding.btnExitDemo.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            NavHostFragment.findNavController(this).navigate(R.id.action_profileFragment_to_welcomeFragment);
        });

        binding.btnDeleteAccount.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_profileFragment_to_deleteAccountFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
