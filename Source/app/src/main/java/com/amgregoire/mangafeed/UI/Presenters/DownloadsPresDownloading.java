package com.amgregoire.mangafeed.UI.Presenters;

import android.os.Bundle;

import com.amgregoire.mangafeed.UI.Mappers.IDownloads;
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

    @Override
    public void init(Bundle bundle)
    {
        try
        {
            mMap.initViews();
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }

}
