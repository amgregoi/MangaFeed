package com.amgregoire.mangafeed.UI.Presenters;

import android.os.Bundle;
import android.widget.Toast;

import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.UI.Mappers.IHome;
import com.amgregoire.mangafeed.Utils.MangaDB;
import com.amgregoire.mangafeed.Utils.MangaLogger;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public class HomePresCatalog extends HomePresBase
{

    public HomePresCatalog(IHome.HomeBaseMap map)
    {
        super(map);
    }

    @Override
    public void updateMangaList()
    {
        if (mMangaListSubscription != null)
        {
            mMangaListSubscription.unsubscribe();
            mMangaListSubscription = null;
        }

        try
        {
            mMangaListSubscription = MangaDB.getInstance()
                                            .getCatalogList().cache()
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
}
