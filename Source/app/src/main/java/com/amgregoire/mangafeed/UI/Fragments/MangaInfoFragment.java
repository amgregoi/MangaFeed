package com.amgregoire.mangafeed.UI.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amgregoire.mangafeed.Models.Manga;
import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.UI.Mappers.IManga;
import com.amgregoire.mangafeed.UI.Presenters.MangaInfoPres;
import com.l4digital.fastscroll.FastScrollRecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Andy Gregoire on 3/12/2018.
 */

public class MangaInfoFragment extends Fragment implements IManga.MangaMap
{
    public final static String TAG = MangaInfoFragment.class.getSimpleName();
    public final static String MANGA_KEY = TAG + "MANGA";

    @BindView(R.id.recyclerViewMangaInfo) FastScrollRecyclerView mRecyclerView;

    private IManga.MangaPres mPresenter;

    public static Fragment newInstance(Manga manga)
    {
        Bundle lBundle = new Bundle();
        lBundle.putParcelable(MANGA_KEY, manga);

        Fragment lFragment = new MangaInfoFragment();
        lFragment.setArguments(lBundle);

        return lFragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View lView = inflater.inflate(R.layout.fragment_manga_info, null);
        ButterKnife.bind(this, lView);

        mPresenter = new MangaInfoPres(this);
        mPresenter.init(getArguments());

        return lView;
    }

    @Override
    public void initViews()
    {
        // do stuff
    }

    @Override
    public void registerAdapter(RecyclerView.Adapter adapter, RecyclerView.LayoutManager manager)
    {
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(adapter);
    }

    public void onSelectAllOrNone(boolean isAll)
    {
        mPresenter.onSelectAllOrNone(isAll);
        onDownloadViewStart();
    }

    public void onDownloadViewStart()
    {
        mRecyclerView.scrollToPosition(1);
    }

    public void onDownloadCancel()
    {
        mPresenter.onDownloadCancel();
    }

    public void onDownloadDownload()
    {
        mPresenter.onDownloadDownload();
    }

    public void onRefreshInfo()
    {
        mPresenter.onRefreshInfo();
    }

}
