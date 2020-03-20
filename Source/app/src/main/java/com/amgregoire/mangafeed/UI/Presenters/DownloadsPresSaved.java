package com.amgregoire.mangafeed.UI.Presenters;

import android.os.Bundle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.DbManga;
import com.amgregoire.mangafeed.UI.Adapters.SearchRecyclerAdapter;
import com.amgregoire.mangafeed.UI.Mappers.IDownloads;
import com.amgregoire.mangafeed.Utils.BusEvents.SearchQueryChangeEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.StartDownloadEvent;
import com.amgregoire.mangafeed.Utils.MangaDB;
import com.amgregoire.mangafeed.Utils.MangaLogger;

import java.util.List;

import io.reactivex.disposables.Disposable;

/**
 * Created by Andy Gregoire on 3/18/2018.
 */

public class DownloadsPresSaved implements IDownloads.DownloadsSavedPres
{
    public final static String TAG = DownloadsPresSaved.class.getSimpleName();

    private SearchRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private IDownloads.DownloadsSavedMap mMap;
    private Disposable mRxBus;

    public DownloadsPresSaved(IDownloads.DownloadsSavedMap map)
    {
        mMap = map;
    }

    @Override
    public void init(Bundle bundle)
    {
        try
        {
            getMangaWithSavedChapters();
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
            mRxBus.dispose();
            mRxBus = null;
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
            mRxBus = MangaFeed.Companion.getApp().rxBus().toObservable().subscribe(o ->
            {
                // onNext
                if (o instanceof StartDownloadEvent)
                {
                    getMangaWithSavedChapters();
                }
                else if (o instanceof SearchQueryChangeEvent)
                {
                    SearchQueryChangeEvent lEvent = (SearchQueryChangeEvent) o;
                    mAdapter.performTextFilter(lEvent.query);
                }
            }, throwable ->
            {
                // onError
                MangaLogger.logError(TAG, throwable.getMessage());
            });
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }


    public void getMangaWithSavedChapters()
    {
        try
        {
            MangaDB.getInstance()
                   .getMangaWithDownloadedChapters()
                   .subscribe(
                           mangas -> registerAdapter(mangas),
                           throwable -> MangaLogger.logError(TAG, throwable.toString()));
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }

    public void registerAdapter(List<DbManga> dbManga)
    {
        try
        {
            mLayoutManager = new GridLayoutManager(mMap.getContext(), 3);
            mAdapter = new SearchRecyclerAdapter(dbManga);
            mAdapter.setOffline();

            mMap.registerAdapter(mAdapter, mLayoutManager);
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }
}
