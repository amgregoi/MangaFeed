package com.amgregoire.mangafeed.UI.Presenters;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.amgregoire.mangafeed.UI.Adapters.MangaInfoChaptersAdapter;
import com.amgregoire.mangafeed.Common.RequestWrapper;
import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.Chapter;
import com.amgregoire.mangafeed.Models.Manga;
import com.amgregoire.mangafeed.UI.Fragments.MangaInfoFragment;
import com.amgregoire.mangafeed.UI.Mappers.IManga;
import com.amgregoire.mangafeed.Utils.MangaLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andy Gregoire on 3/12/2018.
 */

public class MangaInfoPres implements IManga.MangaPres
{
    public final static String TAG = MangaInfoPres.class.getSimpleName();

    private IManga.MangaMap mMap;
    private Manga mManga;

    private RecyclerView.LayoutManager mLayoutManager;
    private MangaInfoChaptersAdapter mAdapter;

    private boolean mInfoUpdateFlag = false;
    private boolean mChaptersFlag = false;

    public MangaInfoPres(IManga.MangaMap map)
    {
        mMap = map;
    }

    @Override
    public void init(Bundle bundle)
    {
        mManga = bundle.getParcelable(MangaInfoFragment.MANGA_KEY);

        fetchMangaInfo();
        fetchChapterList();
    }

    private void fetchMangaInfo()
    {
        MangaFeed.getInstance()
                 .getCurrentSource()
                 .updateMangaObservable(new RequestWrapper(mManga))
                 .subscribe
                         (
                                 manga ->
                                 {
                                     mInfoUpdateFlag = true;
                                     mManga = manga;
                                     initView();
                                 },
                                 throwable ->
                                 {
                                     mInfoUpdateFlag = true; // re-use old info
                                     String lMessage = "Failed to update (" + mManga.getTitle() + ")";
                                     MangaLogger.logError(TAG, lMessage, throwable.getMessage());
                                 },
                                 () ->
                                 {
                                     String lMessage = "Finished updating (" + mManga.getTitle() + ")";
                                     MangaLogger.logInfo(TAG, lMessage);
                                 }
                         );
    }

    List<Chapter> mChapterList;

    private void fetchChapterList()
    {
        MangaFeed.getInstance()
                 .getCurrentSource()
                 .getChapterListObservable(new RequestWrapper(mManga))
                 .subscribe
                         (
                                 chapters ->
                                 {
                                     mChaptersFlag = true;
                                     mChapterList = new ArrayList<>(chapters);
                                     initView();
                                 },
                                 throwable ->
                                 {
                                     mChaptersFlag = true; // show info without chapters
                                     String lMessage = "Failed to retrieve chapters (" + mManga.getTitle() + ")";
                                     MangaLogger.logError(TAG, lMessage, throwable.getMessage());

                                 },
                                 () ->
                                 {
                                     String lMessage = "Finished retrieving chapters (" + mManga.getTitle() + ")";
                                     MangaLogger.logInfo(TAG, lMessage);
                                 }
                         );

    }


    private void initView()
    {
        if (mInfoUpdateFlag && mChaptersFlag)
        {

            mLayoutManager = new LinearLayoutManager(mMap.getContext());
            mAdapter = new MangaInfoChaptersAdapter(mChapterList, mManga);
            mMap.registerAdapter(mAdapter, mLayoutManager);
        }

        if (!mInfoUpdateFlag && mChaptersFlag)
        {
            // show failed to retrieve manga view
        }
    }

    @Override
    public void onSelectAllOrNone(boolean isAll)
    {
        mAdapter.onSelectAllOrNone(isAll);
    }

    @Override
    public void onRefreshInfo()
    {
        // refresh manga view / chapters
    }

    @Override
    public void onDownloadCancel()
    {
        mAdapter.onDownloadCancel();
    }

    @Override
    public void onDownloadDownload()
    {
        mAdapter.onDownloadCancel();
    }

    @Override
    public void onDownloadViewEnabled()
    {
        mAdapter.onDownloadViewEnabled();
    }
}
