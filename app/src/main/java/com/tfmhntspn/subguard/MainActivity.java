package com.tfmhntspn.subguard;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.tfmhntspn.subguard.databinding.ActivityMainBinding;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNav, navController);

            Set<Integer> topLevelDestinations = new HashSet<>(Arrays.asList(
                    R.id.homeFragment,
                    R.id.subscriptionsFragment,
                    R.id.splitsFragment,
                    R.id.roommateSplitsV1Fragment,
                    R.id.profileFragment
            ));

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                boolean showBottomNav = topLevelDestinations.contains(destination.getId());
                binding.bottomNav.setVisibility(showBottomNav ? View.VISIBLE : View.GONE);
                binding.bottomNavDivider.setVisibility(showBottomNav ? View.VISIBLE : View.GONE);
            });
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
