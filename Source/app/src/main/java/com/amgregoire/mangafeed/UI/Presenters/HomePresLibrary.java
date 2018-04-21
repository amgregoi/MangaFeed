package com.amgregoire.mangafeed.UI.Presenters;

import android.util.Log;

import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.Manga;
import com.amgregoire.mangafeed.UI.Mappers.IHome;
import com.amgregoire.mangafeed.Utils.BusEvents.SearchQueryChangeEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.UpdateMangaItemViewEvent;
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
        try
        {
            if (mDisposableMangaList != null)
            {
                mDisposableMangaList.dispose();
                mDisposableMangaList = null;
            }


            mDisposableMangaList = MangaDB.getInstance()
                                          .getLibraryList().cache()
                                          .subscribeOn(Schedulers.io())
                                          .observeOn(AndroidSchedulers.mainThread())
                                          .subscribe
                                                    (
                                                            mangas -> updateMangaGridView(mangas),
                                                            throwable -> MangaLogger.logError(TAG, "Failed to retrieve library list", throwable
                                                                    .getMessage())
                                                    );
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }

    @Override
    public void setupRxBus()
    {
        try
        {
            mRxBus = MangaFeed.getInstance().rxBus().toObservable().subscribe(o ->
            {
                if (o instanceof UpdateMangaItemViewEvent)
                {

                    UpdateMangaItemViewEvent lEvent = (UpdateMangaItemViewEvent) o;

                    if (lEvent.isMulti)
                    {
                        updateMangaList();
                    }
                    else
                    {
                        Manga manga = ((UpdateMangaItemViewEvent) o).manga;
                        mAdapter.updateItem(manga, manga.isFollowing());
                        MangaLogger.logInfo(TAG, "updated view", manga.title);
                    }
                }
                else if (o instanceof SearchQueryChangeEvent)
                {
                    SearchQueryChangeEvent lEvent = (SearchQueryChangeEvent) o;
                    mAdapter.performTextFilter(lEvent.query);
                }
            }, throwable -> Log.e(TAG, throwable.getMessage()));
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }
}
