package com.amgregoire.mangafeed.UI.Presenters;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.UI.Adapters.DownloadScheduleAdapter;
import com.amgregoire.mangafeed.UI.Mappers.IDownloads;
import com.amgregoire.mangafeed.Utils.BusEvents.DownloadEventUpdateComplete;
import com.amgregoire.mangafeed.Utils.MangaLogger;

import io.reactivex.disposables.Disposable;

/**
 * Created by Andy Gregoire on 3/18/2018.
 */

public class DownloadsPresDownloading implements IDownloads.DownloadsDownloadingPres
{
    public final static String TAG = DownloadsPresDownloading.class.getSimpleName();

    private IDownloads.DownloadsDownloadingMap mMap;
    private DownloadScheduleAdapter lAdapter;
    private RecyclerView.LayoutManager lManager;

    private Disposable mDisposableDownloadListener;

    public DownloadsPresDownloading(IDownloads.DownloadsDownloadingMap map)
    {
        mMap = map;
    }


    @Override
    public void init(Bundle bundle)
    {
        try
        {
            mMap.initViews();

            lManager = new LinearLayoutManager(mMap.getContext());
            lAdapter = new DownloadScheduleAdapter();

            mMap.registerAdapter(lAdapter, lManager);
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
            if (mDisposableDownloadListener != null)
            {
                mDisposableDownloadListener.dispose();
                mDisposableDownloadListener = null;
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
            mDisposableDownloadListener = MangaFeed.Companion.getApp()
                                                   .rxBus()
                                                   .toObservable()
                                                   .subscribe(o ->
                                                   {
                                                       if (o instanceof DownloadEventUpdateComplete)
                                                       {
                                                           mMap.scrollToUpdateViews();
                                                       }
                                                   }, throwable -> MangaLogger.logError(TAG, throwable.getMessage()));
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }
}
