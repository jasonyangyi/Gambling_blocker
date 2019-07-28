package com.example.gambling_blocker;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ViewpagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> fragmentlist = new ArrayList<>();
    private final List<String> fragmenttitle = new ArrayList<>();

    public ViewpagerAdapter(FragmentManager manager)
    {
        super(manager);
    }

    @Override
    public Fragment getItem(int i) {
        return fragmentlist.get(i);
    }

    @Override
    public int getCount() {
        return fragmentlist.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return fragmenttitle.get(position);
    }

    public void addFragment(Fragment fra, String title)
    {
        fragmentlist.add(fra);
        fragmenttitle.add(title);
    }
}
