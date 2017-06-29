package com.vp.mplayerl.activities;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;

/**
 * Created by Ville on 28.6.2017.
 */

public class MainActivityTabHandlingListeners implements TabLayout.OnTabSelectedListener, ViewPager.OnPageChangeListener {


    private TabLayout tabLayout;
    private ViewPager viewPager;

    public MainActivityTabHandlingListeners(TabLayout tabLayout, ViewPager viewPager) {
        this.tabLayout = tabLayout;
        this.viewPager = viewPager;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
