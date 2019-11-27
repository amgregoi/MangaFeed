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
import com.amgregoire.mangafeed.UI.Mappers.IDownloads;
import com.amgregoire.mangafeed.UI.Presenters.DownloadsPres;
import com.amgregoire.mangafeed.v2.ui.map.ToolbarMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public class DownloadsFragment extends Fragment implements IDownloads.DownloadsMap
{
    public final static String TAG = DownloadsFragment.class.getSimpleName();
    public final static int MENU_RESOURCE = R.menu.menu_toolbar_downloads;

    @BindView(R.id.tabLayoutHome) TabLayout mTabLayout;
    @BindView(R.id.viewPagerHome) ViewPager mViewPager;

    private IDownloads.DownloadsPres mPresenter;

    public static DownloadsFragment newInstance()
    {
        return new DownloadsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View lView = inflater.inflate(R.layout.fragment_home, null);
        ButterKnife.bind(this, lView);

        mPresenter = new DownloadsPres(this, getChildFragmentManager());
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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser)
    {
        if(isVisibleToUser)
        {
            ((ToolbarMap)getActivity()).setOptionsMenu(R.menu.menu_empty);
            ((ToolbarMap)getActivity()).setTitle(getString(R.string.nav_bottom_title_download));
        }
    }

    /***
     * This function sets up the tab layout.
     *
     */
    private void setupTabLayout()
    {
        mTabLayout.addTab(mTabLayout.newTab().setText("Saved"));
        mTabLayout.addTab(mTabLayout.newTab().setText("Downloading"));

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


    public void startDownload()
    {
//        mChapter.downloadStatus = Chapter.DOWNLOAD_STATUS_DOWNLOADING;
//        MangaDB.getInstance().putChapter(mChapter);
    }
    public void finishDownload()
    {
//        mChapter.downloadStatus = Chapter.DOWNLOAD_STATUS_FINISHED;
//        MangaDB.getInstance().putChapter(mChapter);
    }
}
