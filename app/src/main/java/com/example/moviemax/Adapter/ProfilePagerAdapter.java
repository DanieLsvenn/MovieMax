package com.example.moviemax.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.moviemax.Fragment.TicketsFragment;
import com.example.moviemax.Fragment.UserInfoFragment;

public class ProfilePagerAdapter extends FragmentStateAdapter {
    private UserInfoFragment userInfoFragment;
    private TicketsFragment ticketsFragment;

    public ProfilePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                if (userInfoFragment == null) {
                    userInfoFragment = new UserInfoFragment();
                }
                return userInfoFragment;
            case 1:
                if (ticketsFragment == null) {
                    ticketsFragment = new TicketsFragment();
                }
                return ticketsFragment;
            default:
                return new UserInfoFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Two tabs: User Info and Tickets
    }

    public UserInfoFragment getUserInfoFragment() {
        return userInfoFragment;
    }

    public TicketsFragment getTicketsFragment() {
        return ticketsFragment;
    }
}