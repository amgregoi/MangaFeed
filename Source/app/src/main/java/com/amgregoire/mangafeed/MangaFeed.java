package com.amgregoire.mangafeed;

import android.app.Application;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import com.amgregoire.mangafeed.Common.MangaEnums;
import com.amgregoire.mangafeed.Common.WebSources.Base.SourceBase;
import com.amgregoire.mangafeed.Common.WebSources.FunManga;
import com.amgregoire.mangafeed.Common.WebSources.MangaEden;
import com.amgregoire.mangafeed.Common.WebSources.MangaHere;
import com.amgregoire.mangafeed.Common.WebSources.ReadLight;
import com.amgregoire.mangafeed.Models.Chapter;
import com.amgregoire.mangafeed.Utils.MangaDB;
import com.amgregoire.mangafeed.Utils.MangaLogger;
import com.amgregoire.mangafeed.Utils.RxBus;
import com.amgregoire.mangafeed.Utils.SharedPrefs;
import com.bumptech.glide.request.target.ViewTarget;
import com.squareup.picasso.Cache;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public class MangaFeed extends Application
{
    public final static String TAG = MangaFeed.class.getSimpleName();

    private static MangaFeed mInstance;
    private RxBus mBus;

    private ArrayList<Chapter> mCurrentChapters;
    private Picasso mPicasso;

    public MangaFeed()
    {
        mInstance = this;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        ViewTarget.setTagId(R.id.glide_tag);
        mBus = new RxBus();

        mPicasso = new Picasso.Builder(this).executor(Executors.newSingleThreadExecutor()).memoryCache(Cache.NONE).indicatorsEnabled(true).build();
        mPicasso.setIndicatorsEnabled(true);
        Picasso.setSingletonInstance(mPicasso);

        MangaDB.getInstance().createDB(); // Copy pre-loaded database if not already done.
        MangaDB.getInstance().initDao();
        updateCatalogs(false); // check if we should update local database on application open

        if(BuildConfig.VERSION_CODE < 2)
        {
            updateCatalogs(true);
        }
    }

    /***
     * This function returns the MangaFeed application instance
     *
     * @return
     */
    public static synchronized MangaFeed getInstance()
    {
        return mInstance;
    }

    /***
     * This function returns the application RxBus instance.
     *
     * @return
     */
    public RxBus rxBus()
    {
        return mBus;
    }

    /***
     * This function creates and shows a SHORT toast message.
     *
     * @param message
     */
    public void makeToastShort(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /***
     * This function creates and shows a LONG toast message.
     *
     * @param message
     */
    public void makeToastLong(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /***
     * This function creates and shows a short snackbar message.
     *
     * TODO: might remove and just use toasts.
     * @param v
     * @param message
     */
    public void makeSnackBarShort(View v, String message)
    {
        Snackbar.make(v, message, Snackbar.LENGTH_LONG).show();
    }

    /***
     * This function returns the current sources source type
     *
     * NOVEL, MANGA
     *
     * @return
     */
    public MangaEnums.SourceType getCurrentSourceType()
    {
        return MangaEnums.Source.valueOf(SharedPrefs.getSavedSource()).getSource().getSourceType();
    }

    /***
     * This function returns the current source.
     *
     * MangaEden, MangaHere. FunManga, ReadLight
     *
     * @return
     */
    public SourceBase getCurrentSource()
    {
        return MangaEnums.Source.valueOf(SharedPrefs.getSavedSource()).getSource();
    }

    /***
     * This function returns a new source specified by the source classes TAG.
     *
     * @param tag
     * @return
     */
    public SourceBase getSourceByTag(String tag)
    {
        if (tag.equals(FunManga.TAG))
        {
            return new FunManga();
        }
        else if (tag.equals(MangaEden.TAG))
        {
            return new MangaEden();
        }
        else if (tag.equals(MangaHere.TAG))
        {
            return new MangaHere();
        }
        else
        {
            return new ReadLight();
        }
    }

    /***
     * This function returns a new source specified by the source classes TAG.
     *
     * @param url
     * @return
     */
    public SourceBase getSourceByUrl(String url)
    {
        if (url.contains(FunManga.URL))
        {
            return new FunManga();
        }
        else if (url.contains(MangaEden.URL))
        {
            return new MangaEden();
        }
        else if (url.contains(MangaHere.URL))
        {
            return new MangaHere();
        }
        else
        {
            return new ReadLight();
        }
    }

    public void setCurrentChapters(List<Chapter> chapters)
    {
        mCurrentChapters = new ArrayList<>(chapters);
        Collections.reverse(mCurrentChapters);
    }

    public List<Chapter> getCurrentChapters()
    {
        return mCurrentChapters;
    }

    /***
     * This function updates source catalogs items on the local database, adding any missing items.
     * Currently it is set to update no more than once a week.
     *
     */
    public void updateCatalogs(boolean isForceUpdate)
    {
        int lWeekSeconds = 604800;
        int lWeekMs = lWeekSeconds * 1000;

        // Check if we updated in the last week, if we have we'll skip.
        Date lLowerLimit = new Date(SharedPrefs.getLastCatalogUpdate().getTime() + lWeekMs);
        if (lLowerLimit.before(new Date()) || isForceUpdate)
        {
            Observable.create((ObservableEmitter<SourceBase> subscriber) ->
            {
                try
                {
                    SharedPrefs.setLastCatalogUpdate();
                    MangaEnums.Source[] lSources = MangaEnums.Source.values();

                    for (MangaEnums.Source source : lSources)
                    {
                        source.getSource().updateLocalCatalog();
                        subscriber.onNext(source.getSource());
                    }
                    subscriber.onComplete();
                }
                catch (Exception ex)
                {
                    subscriber.onError(ex);
                }
            }).subscribeOn(Schedulers.computation()).subscribe(
                    source -> source.updateLocalCatalog(), // onNext
                    throwable -> MangaLogger.logError(TAG, throwable.getMessage()) // onError
            );
        }
    }
}
