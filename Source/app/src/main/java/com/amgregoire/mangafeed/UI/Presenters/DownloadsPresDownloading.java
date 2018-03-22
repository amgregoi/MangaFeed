package com.amgregoire.mangafeed.UI.Presenters;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.UI.Adapters.DownloadScheduleAdapter;
import com.amgregoire.mangafeed.UI.Mappers.IDownloads;
import com.amgregoire.mangafeed.Utils.BusEvents.DownloadEventUpdateComplete;
import com.amgregoire.mangafeed.Utils.MangaLogger;

/**
 * Created by Andy Gregoire on 3/18/2018.
 */

public class DownloadsPresDownloading implements IDownloads.DownloadsDownloadingPres
{
    public final static String TAG = DownloadsPresDownloading.class.getSimpleName();

    private IDownloads.DownloadsDownloadingMap mMap;

    public DownloadsPresDownloading(IDownloads.DownloadsDownloadingMap map)
    {
        mMap = map;
    }

    DownloadScheduleAdapter lAdapter;
    RecyclerView.LayoutManager lManager;

    @Override
    public void init(Bundle bundle)
    {
        try
        {
            mMap.initViews();

            lManager = new LinearLayoutManager(mMap.getContext());
            lAdapter = new DownloadScheduleAdapter();

            mMap.registerAdapter(lAdapter, lManager);
            test();
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }

    private void test()
    {
        MangaFeed.getInstance()
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


}
