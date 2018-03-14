package com.amgregoire.mangafeed.UI.Presenters;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.UI.Adapters.MangaInfoChaptersAdapter;
import com.amgregoire.mangafeed.Common.RequestWrapper;
import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.Chapter;
import com.amgregoire.mangafeed.Models.Manga;
import com.amgregoire.mangafeed.UI.Fragments.MangaInfoFragment;
import com.amgregoire.mangafeed.UI.Mappers.IManga;
import com.amgregoire.mangafeed.Utils.BusEvents.UpdateFollowStatusEvent;
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
    private List<Chapter> mChapterList;

    private int mInfoUpdateFlag = ViewState.START;
    private int mChaptersFlag = ViewState.START;

    public MangaInfoPres(IManga.MangaMap map)
    {
        mMap = map;
    }

    @Override
    public void init(Bundle bundle)
    {
        mManga = bundle.getParcelable(MangaInfoFragment.MANGA_KEY);
        String lReadText = mManga.recentChapter == null ? "Start" : mManga.recentChapter.isEmpty() ? "Start" : "Continue";

        mMap.initViews();
        if (mManga.isFollowing())
        {
            mMap.setInitialFollowIcon(R.drawable.ic_heart_white_24dp, lReadText);
        }
        else
        {
            mMap.setInitialFollowIcon(R.drawable.ic_heart_outline_white_24dp, lReadText);
        }

        mMap.startRefresh();

        fetchMangaInfo();
        fetchChapterList();
    }

    @Override
    public void onSelectAllOrNone(boolean isAll)
    {
        mAdapter.onSelectAllOrNone(isAll);
    }

    @Override
    public void onDownloadCancel()
    {
        mAdapter.onDownloadCancel();
    }

    @Override
    public void onDownloadDownload()
    {
        mAdapter.onDownloadDownload();
    }

    @Override
    public void onDownloadViewEnabled()
    {
        mAdapter.onDownloadViewEnabled();
    }

    @Override
    public void onUpdateFollowStatus(int status)
    {
        mManga.setFollowing(status);
        MangaFeed.getInstance().rxBus().send(new UpdateFollowStatusEvent(mManga));
    }

    @Override
    public void onRefreshInfo()
    {
        mChaptersFlag = ViewState.START;
        mInfoUpdateFlag = ViewState.START;

        // refresh manga view / chapters
        mAdapter = new MangaInfoChaptersAdapter();
        mMap.registerAdapter(mAdapter, mLayoutManager);

        mMap.startRefresh();

        fetchMangaInfo();
        fetchChapterList();
    }

    /***
     * This function retrieves the manga information from its source.
     *
     */
    private void fetchMangaInfo()
    {
        MangaFeed.getInstance()
                 .getCurrentSource()
                 .updateMangaObservable(new RequestWrapper(mManga))
                 .subscribe
                         (
                                 manga ->
                                 {
                                     mInfoUpdateFlag = ViewState.FINISH;
                                     mManga = manga;
                                     initView();
                                 },
                                 throwable ->
                                 {
                                     mInfoUpdateFlag = ViewState.FAIL; // re-use old info
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

    /***
     * This function retrieves the chapter list from its source.
     *
     */
    private void fetchChapterList()
    {
        MangaFeed.getInstance()
                 .getCurrentSource()
                 .getChapterListObservable(new RequestWrapper(mManga))
                 .subscribe
                         (
                                 chapters ->
                                 {
                                     mChaptersFlag = ViewState.FINISH;
                                     mChapterList = new ArrayList<>(chapters);
                                     initView();
                                 },
                                 throwable ->
                                 {
                                     mChaptersFlag = ViewState.FAIL; // show info without chapters
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


    /***
     * This function checks the states of the two asynchronous calls that fetch a mangas
     * information, and its chapter list.
     *
     */
    private void initView()
    {
        if (mInfoUpdateFlag == ViewState.FINISH && mChaptersFlag == ViewState.FINISH)
        {
            // Both info and chapters retrieved successfully.
            initAdapter();
        }

        if (mInfoUpdateFlag == ViewState.FAIL && mChaptersFlag == ViewState.FINISH)
        {
            // Failed to retrieve info, but got chapters
            // show old manga info we already have (if any)
            initAdapter();
        }
        else if (mInfoUpdateFlag == ViewState.FAIL && mChaptersFlag == ViewState.FAIL)
        {
            // Failed to retrieve info, and chapters
            // Show bad view, (empty at the moment)
            mMap.stopRefresh();
        }
        else if (mInfoUpdateFlag == ViewState.FINISH && mChaptersFlag == ViewState.FAIL)
        {
            // Retrieved info, but failed to retrieve chapters, or not chapters present
            initAdapter();
        }
    }

    /***
     * This function initializes the Manga information adapter, and registers it to the view.
     *
     */
    private void initAdapter()
    {
        mLayoutManager = new LinearLayoutManager(mMap.getContext());
        mAdapter = new MangaInfoChaptersAdapter(mChapterList, mManga);
        mMap.registerAdapter(mAdapter, mLayoutManager);
        mMap.stopRefresh();
    }

    /***
     * This class holds the states for fetching the information and chapter list asynchronously.
     *
     */
    private class ViewState
    {
        public final static int START = 0;
        public final static int FAIL = 1;
        public final static int FINISH = 2;

    }

}
