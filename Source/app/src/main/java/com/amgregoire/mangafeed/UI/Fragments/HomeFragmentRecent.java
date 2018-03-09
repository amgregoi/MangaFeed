package com.amgregoire.mangafeed.UI.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amgregoire.mangafeed.UI.Presenters.HomePresBase;
import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.UI.Presenters.HomePresRecent;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeFragmentRecent extends HomeFragmentsBase
{
    public final static String TAG = HomeFragmentRecent.class.getSimpleName();


    @BindView(R.id.swipeRefreshHomeManga) SwipeRefreshLayout mSwipeLayout;

    /***
     * This function creates and returns a new instance of the RecentFragment.
     *
     * @return
     */
    public static HomeFragmentRecent newInstance()
    {
        return new HomeFragmentRecent();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View lView = inflater.inflate(R.layout.fragment_home_pager_item_refresh, container, false);
        ButterKnife.bind(this, lView);

        mPresenter = new HomePresRecent(this);
        mPresenter.init(getArguments());
        return lView;
    }

    @Override
    public void initViews()
    {
        super.initViews();
        setupSwipeLayout();
    }

    private void setupSwipeLayout()
    {
        mSwipeLayout.setEnabled(true);
        mSwipeLayout.setOnRefreshListener(() -> mPresenter.updateMangaList());
    }
}
