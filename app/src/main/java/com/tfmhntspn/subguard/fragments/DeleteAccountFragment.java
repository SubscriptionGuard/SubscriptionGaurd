package com.tfmhntspn.subguard.fragments;

import android.content.Context;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tfmhntspn.subguard.R;
import com.tfmhntspn.subguard.databinding.FragmentDeleteAccountBinding;

public class DeleteAccountFragment extends Fragment {

    private FragmentDeleteAccountBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDeleteAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadSubscriptionCount();

        binding.btnConfirmDelete.setOnClickListener(v -> deleteAccountAndData());

        binding.btnCancelDelete.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());
    }

    private void loadSubscriptionCount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (binding == null) return;
                        Long countVal = snapshot.child(HomeFragment.FIELD_ACTIVE_COUNT).getValue(Long.class);
                        long subCount = countVal != null ? Math.max(0L, countVal) : snapshot.child("subscriptions").getChildrenCount();
                        String subLabel = subCount == 1 ? "1 subscription" : subCount + " subscriptions";
                        binding.tvDeleteSubscriptionsCount.setText("✕   " + subLabel);

                        long splitsCount = snapshot.child("splits").getChildrenCount();
                        String splitLabel = splitsCount == 1 ? "1 roommate split" : splitsCount + " roommate splits";
                        binding.tvDeleteSplitsCount.setText("✕   " + splitLabel);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void deleteAccountAndData() {
        // 1. Clear local SharedPreferences
        requireContext()
                .getSharedPreferences(HomeFragment.PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(requireContext(), "Local data deleted", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_deleteAccountFragment_to_welcomeFragment);
            return;
        }

        binding.btnConfirmDelete.setEnabled(false);
        String uid = user.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid);

        // 2. Remove user's data from Firebase Realtime Database, then delete Firebase Auth user
        userRef.removeValue().addOnCompleteListener(dbTask ->
                user.delete().addOnCompleteListener(authTask -> {
                    if (!isAdded()) return;
                    auth.signOut();
                    Toast.makeText(
                            requireContext(),
                            "Account and data permanently deleted",
                            Toast.LENGTH_SHORT
                    ).show();
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_deleteAccountFragment_to_welcomeFragment);
                })
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
