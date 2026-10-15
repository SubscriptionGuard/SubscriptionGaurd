
package com.tfmhntspn.subguard;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.tfmhntspn.subguard.fragments.HomeFragment;
import com.tfmhntspn.subguard.fragments.SubscriptionsFragment;
import com.tfmhntspn.subguard.fragments.ProfileFragment;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dashboard);

        // Bottom navigation buttons
        Button homeButton = findViewById(R.id.nav_home);
        Button subscriptionsButton =
                findViewById(R.id.nav_subscriptions);
        Button profileButton = findViewById(R.id.nav_profile);

        // Show Home when clicked
        homeButton.setOnClickListener(v ->
                loadFragment(new HomeFragment()));

        // Show Subscriptions when clicked
        subscriptionsButton.setOnClickListener(v ->
                loadFragment(new SubscriptionsFragment()));

        // Show Profile when clicked
        profileButton.setOnClickListener(v ->
                loadFragment(new ProfileFragment()));

        // Display Home by default
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
