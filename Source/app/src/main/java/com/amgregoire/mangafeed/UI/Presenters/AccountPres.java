package com.amgregoire.mangafeed.UI.Presenters;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.UI.Adapters.AccountPagerAdapter;
import com.amgregoire.mangafeed.UI.Mappers.IAccount;
import com.amgregoire.mangafeed.Utils.BusEvents.GoogleLoginSuccessEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.GoogleLogoutEvent;
import com.amgregoire.mangafeed.Utils.LoginManager;
import com.amgregoire.mangafeed.Utils.MangaDB;
import com.amgregoire.mangafeed.Utils.MangaFeedRest;
import com.amgregoire.mangafeed.Utils.MangaLogger;
import com.amgregoire.mangafeed.Utils.SharedPrefs;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import io.reactivex.disposables.Disposable;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public class AccountPres implements IAccount.AccountPres
{
    public final static String TAG = AccountPres.class.getSimpleName();

    private IAccount.AccountMap mMap;
    private AccountPagerAdapter mAdapter;
    private FragmentManager mManager;
    private Disposable mRxBus;

    public AccountPres(IAccount.AccountMap map, FragmentManager manager)
    {
        mMap = map;
        mManager = manager;
    }

    @Override
    public void init(Bundle bundle)
    {
        try
        {
            mMap.initViews();

            if (mAdapter == null)
            {
                mAdapter = new AccountPagerAdapter(mManager, 2);
            }

            mMap.registerAdapter(mAdapter);
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
            mRxBus = MangaFeed.Companion.getApp().rxBus().toObservable().subscribe(o ->
            {
                if (o instanceof GoogleLoginSuccessEvent)
                {
                    mMap.setHeaderUserName();

                    if (SharedPrefs.getUserEmail() == null || SharedPrefs.getUserName() == null)
                    {
                        MangaFeed.Companion.getApp().makeToastShort("Login failed");
                        LoginManager.logout();
                    }
                }
                else if (o instanceof GoogleLogoutEvent)
                {
                    mMap.setHeaderUserName();
                }
            }, throwable -> MangaLogger.logError(TAG, throwable.getMessage()));
        }
        catch (Exception ex)
        {
            MangaFeed.Companion.getApp().makeToastShort("fucking errors : " + ex.getMessage());
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }
}
