package com.example.qsee;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

public class ProfilePagerAdapter extends FragmentStateAdapter {
    private String userId;

    public ProfilePagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, String userId) {
        super(fragmentManager, lifecycle);
        this.userId = userId;
    }
    public ProfilePagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        Bundle args = new Bundle();
        args.putString("userId", userId);

        switch (position) {
            case 0:
                fragment = new GlimpseFragment();
                break;
            case 1:
                fragment = new GroupsFragment();
                break;
            case 2:
                fragment = new LocationFragment();
                break;
            default:
                return null;
        }

        // Set the arguments containing the username to the fragment
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
