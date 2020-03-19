package com.amgregoire.mangafeed.UI.Fragments;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        View lView = inflater.inflate(R.layout.item_fragment_home_view_pager_refresh, container, false);
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

    @Override
    public void startRefresh()
    {
        mSwipeLayout.setRefreshing(true);
    }

    @Override
    public void stopRefresh()
    {
        mSwipeLayout.setRefreshing(false);
    }

    /***
     * This function ests up the views swipe refresh layout.
     *
     */
    private void setupSwipeLayout()
    {
        mSwipeLayout.setEnabled(true);
        mSwipeLayout.setOnRefreshListener(() -> ((HomePresRecent) mPresenter).onSwipeRefresh());
    }

    /***
     * This function is a message relay to let the Recent fragment know the device has an internet
     * connection.
     *
     */
    public void hasInternetMessage()
    {
        if (mPresenter != null)
        {
            ((HomePresRecent) mPresenter).hasInternetMessage();
        }
    }
}
