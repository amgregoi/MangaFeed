package com.amgregoire.mangafeed.UI.Presenters;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.amgregoire.mangafeed.Adapters.SearchRecyclerAdapter;
import com.amgregoire.mangafeed.Models.Manga;
import com.amgregoire.mangafeed.UI.Mappers.IHome;
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
    protected Disposable mMangaListSubscription;
    protected SearchRecyclerAdapter mAdapter;

    private RecyclerView.LayoutManager mLayoutManager;

    public abstract void updateMangaList();

    public HomePresBase(IHome.HomeBaseMap map)
    {
        mMap = map;
    }

    @Override
    public void init(Bundle bundle)
    {
        mMap.initViews();
        updateMangaList();
        setupRxBus();
    }

    public void setupRxBus(){};



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

                mMangaListSubscription.dispose();
                mMangaListSubscription = null;
            }
        }
        catch (Exception lException)
        {
            MangaLogger.logError(TAG, lException.getMessage());
        }
    }

}
