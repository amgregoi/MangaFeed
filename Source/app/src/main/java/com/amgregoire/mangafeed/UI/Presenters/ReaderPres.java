package com.amgregoire.mangafeed.UI.Presenters;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.amgregoire.mangafeed.Common.RequestWrapper;
import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.Chapter;
import com.amgregoire.mangafeed.Models.Manga;
import com.amgregoire.mangafeed.UI.Adapters.ChapterPagerAdapter;
import com.amgregoire.mangafeed.UI.Fragments.ReaderFragment;
import com.amgregoire.mangafeed.UI.Mappers.IReader;
import com.amgregoire.mangafeed.Utils.MangaLogger;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by Andy Gregoire on 3/21/2018.
 */

public class ReaderPres implements IReader.ReaderPres
{
    public final static String TAG = ReaderPres.class.getSimpleName();

    private IReader.ReaderMap mMap;

    private Manga mManga;
    private List<Chapter> mChapterList;
    private int mCurrentPosition;
    private ChapterPagerAdapter mAdapter;
    private FragmentManager mManager;

    public ReaderPres(IReader.ReaderMap map)
    {
        mMap = map;
    }

    @Override
    public void init(Bundle bundle)
    {
        try
        {
            mManga = bundle.getParcelable(ReaderFragment.MANGA_KEY);

            mManager = ((ReaderFragment) mMap).getFragmentManager();
            mCurrentPosition = bundle.getInt(ReaderFragment.POSITION_KEY);
            mChapterList = MangaFeed.getInstance().getCurrentChapters();

            mMap.initViews();

            if (mChapterList == null)
            {
                MangaFeed.getInstance()
                         .getCurrentSource()
                         .getChapterListObservable(new RequestWrapper(mManga))
                         .subscribe(
                                 chapters -> MangaFeed.getInstance().setCurrentChapters(chapters),
                                 throwable -> MangaLogger.logError(TAG, "Failed to parse chapters", throwable
                                         .getMessage()),
                                 () ->
                                 {
                                     mChapterList = MangaFeed.getInstance().getCurrentChapters();
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

    private void finishInit()
    {
        mAdapter = new ChapterPagerAdapter(mManager, mChapterList, mManga.isFollowing(), mManga);
        mMap.registerAdapter(mAdapter);
        mMap.setPagerPosition(mCurrentPosition);
    }

    @Override
    public String getMangaTitle()
    {
        return mManga.title;
    }

    @Override
    public String getChapterTitle()
    {
        return mChapterList.get(mCurrentPosition).chapterTitle;
    }

    @Override
    public void onSaveState(Bundle save)
    {
        save.putParcelable(ReaderFragment.MANGA_KEY, mManga);
        save.putInt(ReaderFragment.POSITION_KEY, mCurrentPosition);
    }

    @Override
    public void onRestoreState(Bundle restore)
    {
        if (restore != null)
        {
            mManga = restore.getParcelable(ReaderFragment.MANGA_KEY);
            mCurrentPosition = restore.getInt(ReaderFragment.POSITION_KEY);
        }
    }
}
