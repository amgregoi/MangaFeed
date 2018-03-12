package com.amgregoire.mangafeed.UI.Presenters;

import android.util.Log;

import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.Manga;
import com.amgregoire.mangafeed.UI.Mappers.IHome;
import com.amgregoire.mangafeed.Utils.BusEvents.UpdateItemEvent;
import com.amgregoire.mangafeed.Utils.MangaDB;
import com.amgregoire.mangafeed.Utils.MangaLogger;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public class HomePresLibrary extends HomePresBase
{
    public final static String TAG = HomePresLibrary.class.getSimpleName();

    public HomePresLibrary(IHome.HomeBaseMap map)
    {
        super(map);
    }

    @Override
    public void updateMangaList()
    {
        if (mMangaListSubscription != null)
        {
            mMangaListSubscription.dispose();
            mMangaListSubscription = null;
        }

        try
        {
            mMangaListSubscription = MangaDB.getInstance()
                                            .getLibraryList().cache()
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .doOnError(throwable -> MangaFeed.getInstance().makeToastShort(throwable.getMessage()))
                                            .subscribe(aManga -> updateMangaGridView(aManga));
        }
        catch (Exception aException)
        {
            MangaLogger.logError(TAG, aException.getMessage());
        }
    }

    @Override
    public void setupRxBus()
    {
        if (mRxBus == null || mRxBus.isDisposed())
        {
            mRxBus = MangaFeed.getInstance().rxBus().toObservable().subscribe(o ->
            {
                if (o instanceof UpdateItemEvent)
                {
                    Manga manga = ((UpdateItemEvent) o).manga;
                    mAdapter.updateItem(manga, manga.isFollowing());
                    Log.e(TAG, manga.title);
                }
            }, throwable -> Log.e(TAG, throwable.getMessage()));
        }
    }
}
