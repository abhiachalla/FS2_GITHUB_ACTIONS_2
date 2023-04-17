package edu.northeasternn.studycircle.util;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import edu.northeasternn.studycircle.HomeFragment;

public class HomePagerAdapter extends FragmentStateAdapter {

    private HomeFragment homeTabFragment;

    private HomeFragment homeTabFragment2;

    private HomeFragment homeTabFragment3;


    private final int numberOfTabs = 3;
    public HomePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        homeTabFragment = new HomeFragment();
        homeTabFragment2 = new HomeFragment();
        homeTabFragment3 = new HomeFragment();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return homeTabFragment;
            case 1:
                return homeTabFragment2;
            case 2:
                return homeTabFragment3;
        }
        return homeTabFragment;
    }

    @Override
    public int getItemCount() {
        return  numberOfTabs;
    }
}