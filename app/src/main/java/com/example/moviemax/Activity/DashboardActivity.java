package com.example.moviemax.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.moviemax.Fragment.MovieFragment;
import com.example.moviemax.Fragment.RoomFragment;
import com.example.moviemax.R;
import com.example.moviemax.Fragment.CinemaFragment;
import com.example.moviemax.Fragment.SidebarFragment;

import java.util.Objects;

public class DashboardActivity extends AppCompatActivity implements SidebarFragment.OnSidebarItemSelectedListener{

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        ImageButton menuBtn = toolbar.findViewById(R.id.btnMenu);

        menuBtn.setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });

        drawerLayout = findViewById(R.id.drawerLayout);
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(false);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.sidebarFragmentContainer, new SidebarFragment())
                    .commit();
        }
    }

    @Override
    public void onSidebarItemSelected(String item) {
        Fragment fragmentToLoad = null;

        switch (item) {
            case "Cinemas":
                fragmentToLoad = new CinemaFragment();
                break;
            case "Movies":
                fragmentToLoad = new MovieFragment();
                break;
            case "Rooms":
                fragmentToLoad = new RoomFragment();
                break;
            case "Profile":
                // Navigate to ProfileActivity
                Intent profileIntent = new Intent(DashboardActivity.this, ProfileActivity.class);
                startActivity(profileIntent);
                drawerLayout.closeDrawer(GravityCompat.START);
                return; // Don't load fragment, just start activity
        }

        if (fragmentToLoad != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contentFrame, fragmentToLoad)
                    .addToBackStack(null)
                    .commit();
        }
        
        // Close drawer after selection
        drawerLayout.closeDrawer(GravityCompat.START);
    }
}