package com.amgregoire.mangafeed.UI.Fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amgregoire.mangafeed.Common.RecyclerViewSpaceDecoration;
import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.UI.Mappers.IDownloads;
import com.amgregoire.mangafeed.UI.Presenters.DownloadsPresDownloading;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DownloadsFragmentDownloading extends Fragment implements IDownloads.DownloadsDownloadingMap
{
    public final static String TAG = DownloadsFragmentDownloading.class.getSimpleName();

    @BindView(R.id.recyclerViewDownloadDownloading) RecyclerView mRecyclerView;

    private IDownloads.DownloadsDownloadingPres mPresenter;
    private RecyclerViewSpaceDecoration mSpaceDecor;

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
    public void onResume()
    {
        super.onResume();
        mPresenter.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mPresenter.onPause();
    }

    @Override
    public void initViews()
    {

    }

    @Override
    public void registerAdapter(RecyclerView.Adapter adapter, RecyclerView.LayoutManager manager)
    {
        if (mSpaceDecor == null)
        {
            mSpaceDecor = new RecyclerViewSpaceDecoration(20);
        }
        else
        {
            mRecyclerView.removeItemDecoration(mSpaceDecor);
        }

        mRecyclerView.addItemDecoration(mSpaceDecor);
        mRecyclerView.getItemAnimator().setChangeDuration(0);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void scrollToUpdateViews()
    {
        // recyclerview wasn't updating correctly, and needed an 'invisible' scroll to update views.
        mRecyclerView.scrollBy(0, 0);
    }
}
