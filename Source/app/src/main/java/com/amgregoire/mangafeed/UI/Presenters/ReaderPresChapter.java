package com.amgregoire.mangafeed.UI.Presenters;

import android.os.Bundle;

import com.amgregoire.mangafeed.Common.RequestWrapper;
import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.Chapter;
import com.amgregoire.mangafeed.Models.Manga;
import com.amgregoire.mangafeed.UI.Adapters.ImagePagerAdapter;
import com.amgregoire.mangafeed.UI.Fragments.ReaderFragmentChapter;
import com.amgregoire.mangafeed.UI.Mappers.IReader;
import com.amgregoire.mangafeed.Utils.MangaLogger;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by Andy Gregoire on 3/22/2018.
 */

public class ReaderPresChapter implements IReader.ReaderPresChapter
{
    public final static String TAG = ReaderPresChapter.class.getSimpleName();

    private IReader.ReaderMapChapter mMap;
    private ImagePagerAdapter mAdapter;
    private ArrayList<String> mImageUrls;

    private Manga mManga;
    private List<Chapter> mChapterList;
    private int mPosition;
    public ReaderPresChapter(IReader.ReaderMapChapter map)
    {
        mMap = map;
        mImageUrls = new ArrayList<>();
    }

    @Override
    public void init(Bundle bundle)
    {
        mManga = bundle.getParcelable(ReaderFragmentChapter.MANGA_KEY);
        mPosition = bundle.getInt(ReaderFragmentChapter.POSITION_KEY);
        mChapterList = MangaFeed.getInstance().getCurrentChapters();

        if(mChapterList == null)
        {
            getChapters();
        }
        else
        {
            MangaLogger.logError(TAG, "init adapter view");
            init();
        }
    }

    private void init()
    {
        mMap.initViews();
        Chapter lChapter = mChapterList.get(mPosition);

        MangaFeed.getInstance()
                 .getCurrentSource()
                 .getChapterImageListObservable(new RequestWrapper(lChapter))
                 .observeOn(AndroidSchedulers.mainThread())
                 .subscribe(
                         url -> mImageUrls.add(url),
                         throwable -> MangaLogger.logError(TAG, throwable.getMessage()),
                         () ->
                         {
                             mAdapter = new ImagePagerAdapter(mMap.getContext(), mImageUrls);
                             mMap.registerAdapter(mAdapter);
                         });
    }

    private void getChapters()
    {
        MangaFeed.getInstance()
                 .getCurrentSource()
                 .getChapterListObservable(new RequestWrapper(mManga))
                 .subscribe(
                         chapters ->
                         {
                             mChapterList = new ArrayList<>(chapters);
                         },
                         throwable ->
                         {
                             String lMessage = "Failed to retrieve chapters (" + mManga.getTitle() + ")";
                             MangaLogger.logError(TAG, lMessage, throwable.getMessage());
                         },
                         () ->
                         {
                             init();
                         }
                 );
    }

}
