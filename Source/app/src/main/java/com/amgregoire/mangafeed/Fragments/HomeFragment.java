package com.amgregoire.mangafeed.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amgregoire.mangafeed.Adapters.HomeViewPagerAdapter;
import com.amgregoire.mangafeed.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public class HomeFragment extends Fragment
{
    public final static String TAG = HomeFragment.class.getSimpleName();

    @BindView(R.id.tabLayoutHome) TabLayout mTabLayout;
    @BindView(R.id.viewPagerHome) ViewPager mViewPager;

    private HomeViewPagerAdapter mPagerAdapter;

    public static HomeFragment newInstance()
    {
        return new HomeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View lView = inflater.inflate(R.layout.fragment_home, null);
        ButterKnife.bind(this, lView);


        if (savedInstanceState != null)
        {
            // restore state
        }

        init();

        return lView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putBoolean("test", true);
        super.onSaveInstanceState(outState);
    }

    /***
     * This function sets up the activities views.
     *
     */
    private void init()
    {

        setupTabLayout();

        if (mPagerAdapter == null)
        {
            mPagerAdapter = new HomeViewPagerAdapter(getChildFragmentManager(), mTabLayout.getTabCount());
        }

        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mViewPager.setOffscreenPageLimit(2);
    }

    /***
     * This function sets up the tab layout.
     *
     */
    private void setupTabLayout()
    {
        mTabLayout.addTab(mTabLayout.newTab().setText("Recent"));
        mTabLayout.addTab(mTabLayout.newTab().setText("Library"));
        mTabLayout.addTab(mTabLayout.newTab().setText("Catalog"));

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                mViewPager.setCurrentItem(tab.getPosition(), false);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {
                // do nothing
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {
                // do nothing
            }
        });
    }
}
