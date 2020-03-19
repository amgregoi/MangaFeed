package com.amgregoire.mangafeed.UI.Presenters;

import android.os.Bundle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;

import com.amgregoire.mangafeed.UI.Adapters.SearchRecyclerAdapter;
import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.Manga;
import com.amgregoire.mangafeed.UI.Mappers.IHome;
import com.amgregoire.mangafeed.Utils.BusEvents.SearchQueryChangeEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.UpdateMangaItemViewEvent;
import com.amgregoire.mangafeed.Utils.MangaLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.disposables.Disposable;


/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public abstract class HomePresBase implements IHome.HomeBasePres
{
    public final static String TAG = HomePresBase.class.getSimpleName();

    protected IHome.HomeBaseMap mMap;
    protected Disposable mDisposableMangaList;
    protected SearchRecyclerAdapter mAdapter;
    protected Disposable mRxBus;

    private RecyclerView.LayoutManager mLayoutManager;

    public abstract void updateMangaList();

    public HomePresBase(IHome.HomeBaseMap map)
    {
        mMap = map;
    }

    @Override
    public void init(Bundle bundle)
    {
        try
        {
            mMap.initViews();
            updateMangaList();
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }

    public void setupRxBus()
    {
        try
        {
            mRxBus = MangaFeed.Companion.getApp().rxBus().toObservable().subscribe(o ->
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
                        Manga manga = lEvent.manga;
                        mAdapter.updateItem(manga);
                        MangaLogger.logInfo(TAG, "updated view", manga.getTitle());
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

    @Override
    public void onPause()
    {
        try
        {
            if(mRxBus != null)
            {
                mRxBus.dispose();
                mRxBus = null;
            }

            if(mDisposableMangaList != null)
            {
                mDisposableMangaList.dispose();
                mDisposableMangaList = null;
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
        setupRxBus();
    }

    /***
     * This function updates the Recycler views with new/updated content.
     *
     * @param mangaList
     */
    public void updateMangaGridView(List<Manga> mangaList)
    {
        try
        {
            if (mMap.getContext() != null)
            {
                if (mangaList != null)
                {
                    mangaList = new ArrayList<>(mangaList);
                    if (!(this instanceof HomePresRecent))
                    {
                        Collections.sort(mangaList, (emp1, emp2) -> emp1.getTitle()
                                                                        .compareToIgnoreCase(emp2.getTitle()));
                    }
                }
                else
                {
                    // failed to update list, show refresh view,
                    mangaList = new ArrayList<>();
                }

                if (mAdapter == null)
                {
                    mLayoutManager = new GridLayoutManager(mMap.getContext(), 3);
                    mAdapter = new SearchRecyclerAdapter(mangaList);
                    mAdapter.setHasStableIds(true);
                    mMap.registerAdapter(mAdapter, mLayoutManager);
                }
                else
                {
                    mAdapter.updateOriginalData(mangaList);
                }

                mMap.stopRefresh();

                mDisposableMangaList.dispose();
                mDisposableMangaList = null;
            }
        }
        catch (Exception lException)
        {
            MangaLogger.logError(TAG, lException.getMessage());
        }
    }

}
