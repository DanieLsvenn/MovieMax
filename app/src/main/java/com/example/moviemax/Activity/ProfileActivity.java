package com.example.moviemax.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.moviemax.Adapter.ProfilePagerAdapter;
import com.example.moviemax.R;
import com.example.moviemax.Utils.SessionManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ProfileActivity extends AppCompatActivity {
    private static final int EDIT_PROFILE_REQUEST_CODE = 100;
    
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ProfilePagerAdapter pagerAdapter;
    private ImageButton btnBack, btnEditProfile, btnLogout;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupViewPager();
        setupClickListeners();
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        btnBack = findViewById(R.id.btnBack);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);
        
        sessionManager = new SessionManager(this);
    }

    private void setupViewPager() {
        pagerAdapter = new ProfilePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setText("Profile");
                        tab.setIcon(R.drawable.ic_person);
                        break;
                    case 1:
                        tab.setText("My Tickets");
                        tab.setIcon(R.drawable.ic_ticket);
                        break;
                }
            }
        }).attach();
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivityForResult(intent, EDIT_PROFILE_REQUEST_CODE);
        });
        
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    // Method to refresh current tab content
    public void refreshCurrentTab() {
        int currentPosition = viewPager.getCurrentItem();
        switch (currentPosition) {
            case 0:
                if (pagerAdapter.getUserInfoFragment() != null) {
                    pagerAdapter.getUserInfoFragment().refreshUserInfo();
                }
                break;
            case 1:
                if (pagerAdapter.getTicketsFragment() != null) {
                    pagerAdapter.getTicketsFragment().refreshTickets();
                }
                break;
        }
    }

    // Method to switch to tickets tab
    public void switchToTicketsTab() {
        viewPager.setCurrentItem(1, true);
    }

    // Method to switch to profile tab
    public void switchToProfileTab() {
        viewPager.setCurrentItem(0, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to the activity
        refreshCurrentTab();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == EDIT_PROFILE_REQUEST_CODE && resultCode == RESULT_OK) {
            // Profile was updated successfully, refresh the profile tab
            refreshCurrentTab();
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        performLogout();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void performLogout() {
        // Clear session data
        sessionManager.clearSession();
        
        // Show logout message
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        
        // Navigate to login screen and clear task stack
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}