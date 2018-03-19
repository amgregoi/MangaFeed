package com.amgregoire.mangafeed.UI.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amgregoire.mangafeed.Common.RecyclerViewSpaceDecoration;
import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.UI.Mappers.IDownloads;
import com.amgregoire.mangafeed.UI.Presenters.DownloadsPresSaved;
import com.l4digital.fastscroll.FastScrollRecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DownloadsFragmentSaved extends Fragment implements IDownloads.DownloadsSavedMap
{
    public final static String TAG = DownloadsFragmentSaved.class.getSimpleName();

    @BindView(R.id.recyclerViewDownloadSaved) FastScrollRecyclerView mRecyclerView;

    private IDownloads.DownloadsSavedPres mPresenter;
    private RecyclerViewSpaceDecoration mSpaceDecor;

    /***
     * This function creates and returns a new instance of the RecentFragment.
     *
     * @return
     */
    public static DownloadsFragmentSaved newInstance()
    {
        return new DownloadsFragmentSaved();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View lView = inflater.inflate(R.layout.fragment_downloads_saved, container, false);
        ButterKnife.bind(this, lView);

        mPresenter = new DownloadsPresSaved(this);
        mPresenter.init(getArguments());

        return lView;
    }


    @Override
    public void onResume()
    {
        super.onResume();
        mPresenter.subEventBus();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mPresenter.unSubEventBus();
    }

    @Override
    public void initViews()
    {
        // init some views
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

        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        mRecyclerView.addItemDecoration(mSpaceDecor);
        mRecyclerView.getItemAnimator().setChangeDuration(0);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(adapter);
    }
}
