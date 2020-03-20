package com.amgregoire.mangafeed.UI.Presenters;

import android.os.Bundle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.DbManga;
import com.amgregoire.mangafeed.UI.Adapters.SearchRecyclerAdapter;
import com.amgregoire.mangafeed.UI.Fragments.AccountFragmentFiltered;
import com.amgregoire.mangafeed.UI.Mappers.IAccount;
import com.amgregoire.mangafeed.Utils.BusEvents.SearchQueryChangeEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.UpdateMangaItemViewEvent;
import com.amgregoire.mangafeed.Utils.MangaDB;
import com.amgregoire.mangafeed.Utils.MangaLogger;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Andy Gregoire on 3/11/2018.
 */

public class AccountPresFiltered implements IAccount.AccountFilteredPres
{
    public final static String TAG = AccountPresFiltered.class.getSimpleName();
    private Disposable mDisposable;

    private IAccount.AccountFilteredMap mMap;
    private SearchRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Disposable mRxBus;

    private int mFilterValue;

    public AccountPresFiltered(IAccount.AccountFilteredMap map)
    {
        mMap = map;
    }

    @Override
    public void init(Bundle bundle)
    {
        try
        {
            mFilterValue = bundle.getInt(AccountFragmentFiltered.FILTER_KEY);
            mMap.initViews();
            getMangaList();
        }
        catch (Exception aException)
        {
            MangaLogger.logError(TAG, aException.getMessage());
        }
    }

    @Override
    public void onPause()
    {
        try
        {
            mRxBus.dispose();
            mRxBus = null;
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }

    @Override
    public void onResume()
    {
        try
        {
            mRxBus = MangaFeed.Companion.getApp().rxBus().toObservable().subscribe(
                    o ->
                    {
                        if (o instanceof UpdateMangaItemViewEvent)
                        {
                            UpdateMangaItemViewEvent lEvent = ((UpdateMangaItemViewEvent) o);
                            mAdapter.updateItem(lEvent.dbManga);
                        }
                        else if (o instanceof SearchQueryChangeEvent)
                        {
                            SearchQueryChangeEvent lEvent = (SearchQueryChangeEvent) o;
                            mAdapter.performTextFilter(lEvent.query);
                        }
                    },
                    throwable -> MangaLogger.logError(TAG, throwable.getMessage()));
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }

    /***
     * This function retrieves the list of manga with a specified filter.
     */
    private void getMangaList()
    {
        try
        {
            if (mDisposable != null)
            {
                mDisposable.dispose();
                mDisposable = null;
            }


            mDisposable = MangaDB.getInstance()
                                 .getLibraryList(mFilterValue).cache()
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

    /***
     * This function updates the adapter and views when the list is retrieved.
     *
     * @param dbManga
     */
    private void updateMangaGridView(List<DbManga> dbManga)
    {
        try
        {
            if (dbManga != null)
            {
                if (mAdapter == null)
                {
                    mLayoutManager = new GridLayoutManager(mMap.getContext(), 3);
                    mAdapter = new SearchRecyclerAdapter(dbManga);
                    mMap.registerAdapter(mAdapter, mLayoutManager);
                }
                else
                {
                    mAdapter.updateOriginalData(dbManga);
                }
            }
        }
        catch (Exception aException)
        {
            MangaLogger.logError(TAG, aException.getMessage());
        }
    }
}
