package com.amgregoire.mangafeed.UI.Presenters;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.Manga;
import com.amgregoire.mangafeed.UI.Adapters.SearchRecyclerAdapter;
import com.amgregoire.mangafeed.UI.Fragments.AccountFragmentFiltered;
import com.amgregoire.mangafeed.UI.Mappers.IAccount;
import com.amgregoire.mangafeed.Utils.BusEvents.UpdateFollowStatusEvent;
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
    private Disposable mDisposableRxBus;

    private int mFilterValue;

    public AccountPresFiltered(IAccount.AccountFilteredMap map)
    {
        mMap = map;
    }

    public void init(Bundle bundle)
    {
        mFilterValue = bundle.getInt(AccountFragmentFiltered.FILTER_KEY);

        mMap.initViews();
        getMangaList();
    }

    private void getMangaList()
    {
        if (mDisposable != null)
        {
            mDisposable.dispose();
            mDisposable = null;
        }

        try
        {
            mDisposable = MangaDB.getInstance()
                                 .getLibraryList(mFilterValue).cache()
                                 .subscribeOn(Schedulers.io())
                                 .observeOn(AndroidSchedulers.mainThread())
                                 .subscribe
                                         (
                                                 mangas -> updateMangaGridView(mangas),
                                                 throwable -> MangaLogger.logError(TAG, "Failed to retrieve library list", throwable.getMessage())
                                         );
        }
        catch (Exception aException)
        {
            MangaLogger.logError(TAG, aException.getMessage());
        }
    }

    private void updateMangaGridView(List<Manga> manga)
    {
        if (manga != null)
        {
            if (mAdapter == null)
            {
                mLayoutManager = new GridLayoutManager(mMap.getContext(), 3);
                mAdapter = new SearchRecyclerAdapter(manga);
                mMap.registerAdapter(mAdapter, mLayoutManager);
            }
            else
            {
                mAdapter.updateOriginalData(manga);
            }
        }
    }


    @Override
    public void unSubEventBus()
    {
        mDisposableRxBus.dispose();
    }

    @Override
    public void subEventBus()
    {
        mDisposableRxBus = MangaFeed.getInstance().rxBus().toObservable().subscribe(
                o ->
                {
                    if (o instanceof UpdateFollowStatusEvent)
                    {
                        UpdateFollowStatusEvent lEvent = ((UpdateFollowStatusEvent) o);
                        mAdapter.updateItem(lEvent.manga);
                    }
                },
                throwable -> MangaLogger.logError(TAG, throwable.getMessage()));
    }
}
