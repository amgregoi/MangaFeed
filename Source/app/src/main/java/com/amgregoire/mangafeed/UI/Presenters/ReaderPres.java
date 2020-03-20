package com.amgregoire.mangafeed.UI.Presenters;

import android.os.Bundle;
import androidx.fragment.app.FragmentManager;

import com.amgregoire.mangafeed.Common.RequestWrapper;
import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.DbChapter;
import com.amgregoire.mangafeed.Models.DbManga;
import com.amgregoire.mangafeed.UI.Adapters.ChapterPagerAdapter;
import com.amgregoire.mangafeed.UI.Fragments.ReaderFragment;
import com.amgregoire.mangafeed.UI.Fragments.ReaderFragmentChapter;
import com.amgregoire.mangafeed.UI.Mappers.IReader;
import com.amgregoire.mangafeed.Utils.BusEvents.ReaderChapterChangeEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.ReaderSingleTapEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.ReaderToolbarUpdateEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.ToolbarTimerEvent;
import com.amgregoire.mangafeed.Utils.MangaDB;
import com.amgregoire.mangafeed.Utils.MangaLogger;

import java.util.List;

import io.reactivex.disposables.Disposable;

/**
 * Created by Andy Gregoire on 3/21/2018.
 */

public class ReaderPres implements IReader.ReaderPres
{
    public final static String TAG = ReaderPres.class.getSimpleName();

    private IReader.ReaderMap mMap;

    private DbManga mDbManga;
    private List<DbChapter> mDbChapterList;
    private int mCurrentPosition;
    private ChapterPagerAdapter mAdapter;
    private FragmentManager mManager;
    private Disposable mRxBus;

    public ReaderPres(IReader.ReaderMap map)
    {
        mMap = map;
    }

    @Override
    public void init(Bundle bundle)
    {
        try
        {
            mDbManga = bundle.getParcelable(ReaderFragment.MANGA_KEY);

            mManager = ((ReaderFragment) mMap).getFragmentManager();
            mCurrentPosition = bundle.getInt(ReaderFragment.POSITION_KEY);
            mDbChapterList = MangaFeed.Companion.getApp().getCurrentDbChapters();

            mMap.initViews();

            if (mDbChapterList == null)
            {
                MangaFeed.Companion.getApp()
                         .getCurrentSource()
                         .getChapterListObservable(new RequestWrapper(mDbManga))
                         .subscribe(
                                 chapters -> MangaFeed.Companion.getApp().setCurrentDbChapters(chapters),
                                 throwable -> MangaLogger.logError(TAG, "Failed to parse chapters", throwable
                                         .getMessage()),
                                 () ->
                                 {
                                     mDbChapterList = MangaFeed.Companion.getApp().getCurrentDbChapters();
                                     finishInit();
                                 }
                         );
            }
            else
            {
                finishInit();
            }
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }

    @Override
    public String getMangaTitle()
    {
        return mDbManga.getTitle();
    }

    @Override
    public String getChapterTitle()
    {
        return mDbChapterList.get(mCurrentPosition).getChapterTitle();
    }

    @Override
    public void onSaveState(Bundle save)
    {
        save.putParcelable(ReaderFragment.MANGA_KEY, mDbManga);
        save.putInt(ReaderFragment.POSITION_KEY, mCurrentPosition);
    }

    @Override
    public void onRestoreState(Bundle restore)
    {
        if (restore != null)
        {
            mDbManga = restore.getParcelable(ReaderFragment.MANGA_KEY);
            mCurrentPosition = restore.getInt(ReaderFragment.POSITION_KEY);
        }
    }

    @Override
    public void updateCurrentPosition(int position)
    {
        mCurrentPosition = position;
        updateRecentChapter();
        ((ReaderFragmentChapter) mAdapter.getItem(position)).update();

        MangaDB.getInstance().putChapter(mDbChapterList.get(mCurrentPosition));
    }


    @Override
    public void onPause()
    {
        mRxBus.dispose();
        mRxBus = null;
    }


    @Override
    public void onResume()
    {
        mRxBus = MangaFeed.Companion.getApp().rxBus().toObservable().subscribe(o ->
        {
            if (o instanceof ReaderSingleTapEvent)
            {
                mMap.onSingleTap();
            }
            else if (o instanceof ReaderChapterChangeEvent)
            {
                ReaderChapterChangeEvent lEvent = (ReaderChapterChangeEvent) o;
                if (lEvent.isNext)
                {
                    mMap.onNextChapter();
                }
                else
                {
                    mMap.onPrevChapter();
                }
            }
            else if (o instanceof ReaderToolbarUpdateEvent)
            {
                ReaderToolbarUpdateEvent lEvent = (ReaderToolbarUpdateEvent) o;
                mMap.updateToolbars(lEvent.message, lEvent.currentPage, lEvent.totalPages, lEvent.chapterPosition);
            }
            else if (o instanceof ToolbarTimerEvent)
            {
                ToolbarTimerEvent lEvent = (ToolbarTimerEvent) o;
                if (lEvent.isStart)
                {
                    mMap.startToolbarTimer(lEvent.chapterPosition);
                }
                else
                {
                    mMap.stopToolbarTimer(lEvent.chapterPosition);
                }
            }
        }, throwable -> MangaLogger.logError(TAG, throwable.getMessage()));
    }

    /***
     * This function completes the common initializations for both creating, and restoring the fragment.
     *
     */
    private void finishInit()
    {
        mAdapter = new ChapterPagerAdapter(mManager, mDbChapterList, mDbManga.isFollowing(), mDbManga);
        mMap.registerAdapter(mAdapter);
        mMap.setPagerPosition(mCurrentPosition);
    }

    /***
     * Sets the mangas recent chapter to the current displayed chapter, and updates the local db.
     *
     */
    private void updateRecentChapter()
    {
        if (mDbManga.isFollowing())
        {
            mDbManga.setRecentChapter(mDbChapterList.get(mCurrentPosition).getUrl());
            MangaDB.getInstance().putManga(mDbManga);
        }
    }
}
