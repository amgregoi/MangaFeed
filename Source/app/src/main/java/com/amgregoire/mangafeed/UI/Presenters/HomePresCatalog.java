package com.amgregoire.mangafeed.UI.Presenters;

import com.amgregoire.mangafeed.UI.Mappers.IHome;
import com.amgregoire.mangafeed.Utils.MangaDB;
import com.amgregoire.mangafeed.Utils.MangaLogger;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public class HomePresCatalog extends HomePresBase
{
    public final static String TAG = HomePresCatalog.class.getSimpleName();

    public HomePresCatalog(IHome.HomeBaseMap map)
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
                                            .getCatalogList()
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe
                                                    (
                                                            mangas -> updateMangaGridView(mangas),
                                                            throwable -> MangaLogger.logError(TAG, "Failed to retrieve catalog list", throwable.getMessage())
                                                    );
        }
        catch (Exception aException)
        {
            MangaLogger.logError(TAG, aException.getMessage());
        }
    }
}
