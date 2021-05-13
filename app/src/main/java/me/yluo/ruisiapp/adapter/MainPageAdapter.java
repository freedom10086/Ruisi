package me.yluo.ruisiapp.adapter;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;

import me.yluo.ruisiapp.fragment.BaseLazyFragment;

public class MainPageAdapter extends FragmentStatePagerAdapter {

    private final List<BaseLazyFragment> fragments;

    public MainPageAdapter(FragmentManager fm, List<BaseLazyFragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
