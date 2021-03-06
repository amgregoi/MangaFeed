package com.amgregoire.mangafeed.UI.Presenters;

import android.os.Bundle;

import com.amgregoire.mangafeed.Common.RequestWrapper;
import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.Chapter;
import com.amgregoire.mangafeed.Models.Manga;
import com.amgregoire.mangafeed.UI.Adapters.ImagePagerAdapter;
import com.amgregoire.mangafeed.UI.Fragments.ReaderFragmentChapter;
import com.amgregoire.mangafeed.UI.Mappers.IReader;
import com.amgregoire.mangafeed.Utils.BusEvents.ReaderPageChangeEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.ReaderToolbarUpdateEvent;
import com.amgregoire.mangafeed.Utils.MangaLogger;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

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
    private Chapter mChapter;
    private int mChapterPosition, mPagePosition;
    private boolean mFinishedImageUrls;

    public ReaderPresChapter(IReader.ReaderMapChapter map)
    {
        mMap = map;
        mImageUrls = new ArrayList<>();
    }

    @Override
    public void init(Bundle bundle)
    {
        mManga = bundle.getParcelable(ReaderFragmentChapter.MANGA_KEY);
        mChapterPosition = bundle.getInt(ReaderFragmentChapter.POSITION_KEY);
        List<Chapter> lChapterList = MangaFeed.getInstance().getCurrentChapters();

        if (lChapterList == null)
        {
            getChapters();
        }
        else
        {
            MangaLogger.logError(TAG, "init adapter view");
            init(lChapterList);
        }
    }

    private void init(List<Chapter> list)
    {
        mMap.initViews();
        mChapter = list.get(mChapterPosition);

        updateToolbar("Loading pages..", 0, 0);

        MangaFeed.getInstance()
                 .getCurrentSource()
                 .getChapterImageListObservable(new RequestWrapper(mChapter))
                 .observeOn(AndroidSchedulers.mainThread())
                 .subscribe(
                         url ->
                         {
                             mImageUrls.add(url);
                             updateToolbar("Pages loaded: " + mImageUrls.size(), 0, 0);
                         },
                         throwable ->
                         {
                             MangaLogger.logError(TAG, throwable.getMessage());
                             updateToolbar("Problem retrieving pages, try refreshing", 0, 0);
                         },
                         () ->
                         {
                             updateToolbar(mChapter.chapterTitle, 1, mImageUrls.size());
                             mChapter.setTotalPages(mImageUrls.size());
                             mAdapter = new ImagePagerAdapter(mMap.getContext(), mImageUrls);
                             mMap.registerAdapter(mAdapter);
                             mFinishedImageUrls = true;
                         });
    }

    private void getChapters()
    {
        MangaFeed.getInstance()
                 .getCurrentSource()
                 .getChapterListObservable(new RequestWrapper(mManga))
                 .subscribe(
                         chapters -> MangaFeed.getInstance().setCurrentChapters(chapters),
                         throwable ->
                         {
                             String lMessage = "Failed to retrieve chapters (" + mManga.getTitle() + ")";
                             MangaLogger.logError(TAG, lMessage, throwable.getMessage());
                         },
                         () -> init(MangaFeed.getInstance().getCurrentChapters())
                 );
    }

    @Override
    public void updateCurrentPosition(int position)
    {
        mPagePosition = position;
        updateToolbar(mChapter.getChapterTitle(), position + 1, mImageUrls.size());
    }

    @Override
    public void setNewChapterToolbar()
    {
        int lTotal = mImageUrls.size() >= 0 ? mImageUrls.size() : 0;
        int lPage = lTotal == 0 ? 0 : mPagePosition + 1;
        updateToolbar(mChapter.getChapterTitle(), lPage, lTotal);
    }

    private void updateToolbar(String message, int page, int total)
    {
        MangaFeed.getInstance()
                 .rxBus()
                 .send(new ReaderToolbarUpdateEvent(message, page, total, mChapterPosition));
    }

    private Disposable mRxBus;

    @Override
    public void unSubEventBus()
    {
        mRxBus.dispose();
        mRxBus = null;
    }

    @Override
    public void subEventBus()
    {
        mRxBus = MangaFeed.getInstance().rxBus().toObservable().subscribe(o ->
        {
            if (o instanceof ReaderPageChangeEvent)
            {
                ReaderPageChangeEvent lEvent = (ReaderPageChangeEvent) o;
                if (mFinishedImageUrls)
                {
                    if (lEvent.isNext)
                    {
                        mMap.onNextPage();
                    }
                    else
                    {
                        mMap.onPrevPage();
                    }
                }
            }
        }, throwable -> MangaLogger.logError(TAG, throwable.getMessage()));
    }
}
