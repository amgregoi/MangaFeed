package com.amgregoire.mangafeed.UI.Presenters;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.Manga;
import com.amgregoire.mangafeed.UI.Adapters.ChapterPagerAdapter;
import com.amgregoire.mangafeed.UI.Fragments.ReaderFragment;
import com.amgregoire.mangafeed.UI.Mappers.IReader;
import com.amgregoire.mangafeed.Utils.MangaLogger;

/**
 * Created by Andy Gregoire on 3/21/2018.
 */

public class ReaderPres implements IReader.ReaderPres
{
    public final static String TAG = ReaderPres.class.getSimpleName();

    private IReader.ReaderMap mMap;

    private Manga mManga;
    private ChapterPagerAdapter mAdapter;

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

            FragmentManager lManager = ((ReaderFragment) mMap).getChildFragmentManager();
            mAdapter = new ChapterPagerAdapter(lManager, MangaFeed.getInstance().getCurrentChapters(), mManga.isFollowing(), mManga);
            mMap.registerAdapter(mAdapter);
            mMap.setPagerPosition(bundle.getInt(ReaderFragment.POSITION_KEY));
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }
}
