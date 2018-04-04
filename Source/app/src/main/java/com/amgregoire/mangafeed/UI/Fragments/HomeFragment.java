package com.amgregoire.mangafeed.UI.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.UI.Mappers.IHome;
import com.amgregoire.mangafeed.UI.Presenters.HomePres;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public class HomeFragment extends Fragment implements IHome.HomeMap
{
    public final static String TAG = HomeFragment.class.getSimpleName();
    public final static int MENU_RESOURCE = R.menu.menu_toolbar_home;

    @BindView(R.id.tabLayoutHome) TabLayout mTabLayout;
    @BindView(R.id.viewPagerHome) ViewPager mViewPager;

    private IHome.HomePres mPresenter;

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

        mPresenter = new HomePres(this, getChildFragmentManager());
        mPresenter.init(getArguments());

        return lView;
    }


    @Override
    public void initViews()
    {
        setupTabLayout();
    }

    @Override
    public void registerAdapter(PagerAdapter adapter)
    {
        mViewPager.setAdapter(adapter);
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

    public void onInternetConnection()
    {
        HomeFragmentRecent lRecent = (HomeFragmentRecent) mPresenter.getAdapterFragment(0);
        lRecent.hasInternetMessage();
    }
}
