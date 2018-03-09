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

import rx.Subscription;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public abstract class HomePresBase implements IHome.HomeBasePres, SearchRecyclerAdapter.ItemSelectedListener
{
    public final static String TAG = HomePresBase.class.getSimpleName();

    protected IHome.HomeBaseMap mMap;
    protected Subscription mMangaListSubscription;
    protected SearchRecyclerAdapter mAdapter;

    private RecyclerView.LayoutManager mLayoutManager;
    private boolean mNeedsItemSpacing = true;

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
    }

    @Override
    public void onItemSelected(int position)
    {
        try
        {
            Manga lManga = mAdapter.getItem(position);
            // launch new activity
            // look into way of updating other instances of this object when returning

//            if (mMap.setRecentSelection(manga.getMangaURL()))
//            {
//                Intent intent = MangaActivity.getNewInstance(mViewMapper.getContext(), manga.getMangaURL());
//                mViewMapper.getContext().startActivity(intent);
//            }
        }
        catch (Exception lException)
        {
            MangaLogger.logError(TAG, lException.getMessage());
        }    }


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
                    //Sorts manga list of Library or Catalog fragment
                    if (!(this instanceof HomePresRecent))
                        Collections.sort(mangaList, (emp1, emp2) -> emp1.getTitle().compareToIgnoreCase(emp2.getTitle()));
                }
                else
                {
                    // failed to update list, show refresh view,
                    mangaList = new ArrayList<>(); //empty list
                }

                if (mAdapter == null)
                {
                    mLayoutManager = new GridLayoutManager(mMap.getContext(), 3);
                    mAdapter = new SearchRecyclerAdapter(mangaList, (pos) -> onItemSelected(pos));
                    mAdapter.setHasStableIds(true);
                    mMap.registerAdapter(mAdapter, mLayoutManager, mNeedsItemSpacing);
                }
                else
                {
                    mAdapter.updateOriginalData(mangaList);
                }

                mMap.stopRefresh();
                mNeedsItemSpacing = false;

                mMangaListSubscription.unsubscribe();
                mMangaListSubscription = null;
            }
        }
        catch (Exception lException)
        {
            MangaLogger.logError(TAG, lException.getMessage());
        }
    }

}
