package com.amgregoire.mangafeed.UI.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.UI.Mappers.IDownloads;
import com.amgregoire.mangafeed.UI.Presenters.DownloadsPresDownloading;

import butterknife.ButterKnife;

public class DownloadsFragmentDownloading extends Fragment implements IDownloads.DownloadsDownloadingMap
{
    public final static String TAG = DownloadsFragmentDownloading.class.getSimpleName();

    private IDownloads.DownloadsDownloadingPres mPresenter;

    /***
     * This function creates and returns a new instance of the RecentFragment.
     *
     * @return
     */
    public static DownloadsFragmentDownloading newInstance()
    {
        return new DownloadsFragmentDownloading();
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        mPresenter = new DownloadsPresDownloading(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View lView = inflater.inflate(R.layout.fragment_downloads_downloading, container, false);
        ButterKnife.bind(this, lView);

        mPresenter.init(getArguments());

        return lView;
    }

    @Override
    public void initViews()
    {

    }

    @Override
    public void registerAdapter(RecyclerView.Adapter adapter, RecyclerView.LayoutManager manager)
    {

    }
}
