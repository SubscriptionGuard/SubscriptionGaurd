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
import com.tfmhntspn.subguard.databinding.FragmentForgotPasswordBinding;

public class ForgotPasswordFragment extends Fragment {

    private FragmentForgotPasswordBinding binding;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        auth = FirebaseAuth.getInstance();

        binding.btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        binding.btnBackLogin.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());

        binding.btnSendReset.setOnClickListener(v -> {
            String email = binding.etResetEmail.getText() != null
                    ? binding.etResetEmail.getText().toString().trim() : "";
            if (email.isEmpty()) {
                Toast.makeText(
                        requireContext(),
                        "Please enter your email address",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (!isAdded()) return;
                        if (task.isSuccessful()) {
                            Toast.makeText(
                                    requireContext(),
                                    "Password reset email sent. Check your inbox!",
                                    Toast.LENGTH_LONG
                            ).show();
                            NavHostFragment.findNavController(this).navigateUp();
                        } else {
                            String errorMsg = task.getException() != null && task.getException().getMessage() != null
                                    ? task.getException().getMessage() : "Unknown error";
                            Toast.makeText(
                                    requireContext(),
                                    "Failed to send reset email: " + errorMsg,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
