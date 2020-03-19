package com.amgregoire.mangafeed.UI.Presenters;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amgregoire.mangafeed.Common.RequestWrapper;
import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.Chapter;
import com.amgregoire.mangafeed.Models.Manga;
import com.amgregoire.mangafeed.UI.Adapters.MangaInfoChaptersAdapter;
import com.amgregoire.mangafeed.UI.Fragments.MangaInfoFragment;
import com.amgregoire.mangafeed.UI.Mappers.IManga;
import com.amgregoire.mangafeed.Utils.BusEvents.ChapterSelectedEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.ToggleDownloadViewEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.UpdateMangaInfoEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.UpdateMangaItemViewEvent;
import com.amgregoire.mangafeed.Utils.MangaDB;
import com.amgregoire.mangafeed.Utils.MangaLogger;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;

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

    private Disposable mRxBus;
    private Disposable mDisposableMangaInfo;
    private Disposable mDisposableChapterList;

    private int mInfoUpdateFlag = ViewState.START;
    private int mChaptersFlag = ViewState.START;

    public boolean mOfflineFlag;
    public boolean mDownloadFlag;

    public MangaInfoPres(IManga.MangaMap map)
    {
        mMap = map;
    }

    @Override
    public void init(Bundle bundle)
    {
        try
        {
            mManga = bundle.getParcelable(MangaInfoFragment.MANGA_KEY);
            mOfflineFlag = bundle.getBoolean(MangaInfoFragment.OFFLINE_KEY);
            mDownloadFlag = false;

            MangaLogger.logError(TAG, "MangaId -> " + mManga.get_id());
            if (mManga != null)
            {
                mManga = MangaDB.getInstance().getManga(mManga.getLink());
            }

            mMap.initViews();
            mMap.startRefresh();
            setStartContinueReading();
            mMap.setBottomNavFollowTitle(mManga.getFollowing());

            if (mOfflineFlag)
            {
                mInfoUpdateFlag = ViewState.FINISH;
                fetchChapterListOffline();
            }
            else
            {
                fetchMangaInfoOnline();
                fetchChapterListOnline();
            }
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }

    @Override
    public void onPause()
    {
        try
        {
            if (mRxBus != null)
            {
                mRxBus.dispose();
                mRxBus = null;
            }

            if (mDisposableChapterList != null)
            {
                mDisposableChapterList.dispose();
                mDisposableChapterList = null;
            }

            if (mDisposableMangaInfo != null)
            {
                mDisposableMangaInfo.dispose();
                mDisposableMangaInfo = null;
            }
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }

    @Override
    public void onResume()
    {
        try
        {
            if(mAdapter != null)
            {
                mAdapter.notifyDataSetChanged();
            }

            mRxBus = MangaFeed.Companion.getApp().rxBus().toObservable().subscribe(o ->
            {
                if (o instanceof ToggleDownloadViewEvent)
                {
                    mMap.toggleDownloadingFlag();
                }
                else if (o instanceof UpdateMangaInfoEvent)
                {
                    mManga = MangaDB.getInstance().getManga(mManga.getLink());
                    setStartContinueReading();
                }
            }, throwable -> MangaLogger.logError(TAG, throwable.getMessage()));
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }

    @Override
    public void onSelectAllOrNone(boolean isAll)
    {
        try
        {
            mAdapter.onSelectAllOrNone(isAll);
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }

    @Override
    public void onDownloadCancel()
    {
        try
        {
            mAdapter.onDownloadCancel();
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }

    @Override
    public void onDownloadDownload()
    {
        try
        {
            mAdapter.onDownloadDownload();
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }

    public void onDownloadRemove()
    {
        try
        {
            mAdapter.onDownloadRemove();
        }
        catch (Exception ex)
        {
            // Tried to enable download view before adapter was initialized
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }

    @Override
    public void onContinueReading()
    {
        try
        {
            Chapter lChapter = null;
            ArrayList<Chapter> lNewChapterList = new ArrayList<>(MangaFeed.Companion.getApp().getCurrentChapters());

            for (Chapter iChapter : lNewChapterList)
            {
                if (iChapter.getUrl().equals(mManga.getRecentChapter()))
                {
                    lChapter = iChapter;
                    mManga.setRecentChapter(lChapter.getUrl());
                }
            }

            if (lChapter == null)
            {
                lChapter = lNewChapterList.get(0);
            }

            int lPosition = lNewChapterList.indexOf(lChapter);
            MangaFeed.Companion.getApp().rxBus().send(new ChapterSelectedEvent(mManga, lPosition));
        }
        catch (Exception aException)
        {
            MangaLogger.logError(TAG, aException.getMessage());
        }

    }

    @Override
    public void onDownloadViewEnabled()
    {
        try
        {
            mAdapter.onDownloadViewEnabled();
        }
        catch (Exception ex)
        {
            // Tried to enable download view before adapter was initialized
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }

    @Override
    public void onUpdateFollowStatus(int status)
    {
        try
        {
            mManga.updateFollowing(status);
            if (status == Manga.UNFOLLOW)
            {
                mManga.setRecentChapter("");
            }
            MangaDB.getInstance().putManga(mManga);

            MangaFeed.Companion.getApp().rxBus().send(new UpdateMangaItemViewEvent(mManga));
            mMap.setBottomNavFollowTitle(status);
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }

    @Override
    public String getTitle()
    {
        return mManga.getTitle();
    }

    @Override
    public boolean isOffline()
    {
        return mOfflineFlag;
    }

    @Override
    public boolean isDownload()
    {
        return mDownloadFlag;
    }

    @Override
    public void toggleDownload()
    {
        mDownloadFlag = !mDownloadFlag;
    }

    @Override
    public void onRefreshInfo()
    {
        try
        {
            mChaptersFlag = ViewState.START;
            mInfoUpdateFlag = ViewState.START;

            // refresh manga view / chapters
            mAdapter = new MangaInfoChaptersAdapter();
            mMap.registerAdapter(mAdapter, mLayoutManager);

            mMap.startRefresh();

            if (mOfflineFlag)
            {
                mInfoUpdateFlag = ViewState.FINISH;
                fetchChapterListOffline();
            }
            else
            {
                fetchMangaInfoOnline();
                fetchChapterListOnline();
            }
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }

    /***
     * This function retrieves the manga information from its source.
     *
     */
    private void fetchMangaInfoOnline()
    {
        try
        {
            mDisposableMangaInfo = MangaFeed.Companion.getApp()
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
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }

    /***
     * This function retrieves the chapter list from its source.
     *
     */
    private void fetchChapterListOnline()
    {
        try
        {
            mDisposableChapterList = MangaFeed.Companion.getApp()
                                              .getCurrentSource()
                                              .getChapterListObservable(new RequestWrapper(mManga))
                                              .subscribe(
                                                      chapters ->
                                                      {
                                                          mChaptersFlag = ViewState.FINISH;
                                                          MangaFeed.Companion.getApp().setCurrentChapters(chapters);
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
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }

    private void fetchChapterListOffline()
    {
        try
        {
            mDisposableChapterList = MangaDB.getInstance()
                                            .getDownloadedChapters(mManga)
                                            .subscribe(
                                                    chapters ->
                                                    {
                                                        mChaptersFlag = ViewState.FINISH;
                                                        mChapterList = chapters;
                                                        initView();
                                                    }, throwable ->
                                                    {
                                                        mChaptersFlag = ViewState.FAIL; // show info without chapters
                                                        String lMessage = "Failed to retrieve chapters (" + mManga.getTitle() + ")";
                                                        MangaLogger.logError(TAG, lMessage, throwable.getMessage());
                                                    }, () ->
                                                    {
                                                        String lMessage = "Finished retrieving chapters (" + mManga.getTitle() + ")";
                                                        MangaLogger.logInfo(TAG, lMessage);
                                                    });
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }


    /***
     * This function checks the states of the two asynchronous calls that fetch a mangas
     * information, and its chapter list.
     *
     */
    private void initView()
    {
        try
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
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }

    /***
     * This function initializes the Manga information adapter, and registers it to the view.
     *
     */
    private void initAdapter()
    {
        try
        {
            mLayoutManager = new LinearLayoutManager(mMap.getContext());
            mAdapter = new MangaInfoChaptersAdapter(mChapterList, mManga);
            mMap.registerAdapter(mAdapter, mLayoutManager);
            mMap.stopRefresh();
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }

    private void setStartContinueReading()
    {
        String lReadText = mManga.getRecentChapter() == null ? "Start" : mManga.getRecentChapter().isEmpty() ? "Start" : "Continue";
        mMap.setBottomNavStartContinue(lReadText);
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
