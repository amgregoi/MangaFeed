package com.amgregoire.mangafeed.UI.Presenters;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.UI.Adapters.AccountPagerAdapter;
import com.amgregoire.mangafeed.UI.Mappers.IAccount;
import com.amgregoire.mangafeed.Utils.BusEvents.GoogleLoginSuccessEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.GoogleLogoutEvent;
import com.amgregoire.mangafeed.Utils.MangaDB;
import com.amgregoire.mangafeed.Utils.MangaFeedRest;
import com.amgregoire.mangafeed.Utils.MangaLogger;
import com.amgregoire.mangafeed.Utils.SharedPrefs;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

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
            mRxBus = MangaFeed.getInstance().rxBus().toObservable().subscribe(o ->
            {
                if (o instanceof GoogleLoginSuccessEvent)
                {

                    final GoogleLoginSuccessEvent lEvent = (GoogleLoginSuccessEvent) o;
                    final String lEmail, lName;

                    // TODO
                    // remove branching after done implementing login/out sequence and cleaning up
                    if (lEvent.userAccount == null)
                    {
                        lEmail = "jsmith@gmail.com";
                        lName = "John Smith";
                    }
                    else
                    {
                        lEmail = lEvent.userAccount.getEmail();
                        lName = lEvent.userAccount.getDisplayName();
                    }

                    RequestParams lParams = new RequestParams();
                    lParams.put("email", lEmail);
                    lParams.put("name", lName);

                    MangaFeedRest.postUser(lParams, new JsonHttpResponseHandler()
                    {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response)
                        {
                            super.onSuccess(statusCode, headers, response);

                            try
                            {
                                JSONObject lUser = response.getJSONObject("user");

                                SharedPrefs.setUserEmail(lUser.getString("email"));
                                SharedPrefs.setUserName(lUser.getString("name"));
                                SharedPrefs.setUserId(lUser.getInt("id"));

                                MangaFeed.getInstance().makeToastShort("Successfully signed in");
                                MangaDB.getInstance().updateNewUsersLibrary();
                            }
                            catch (JSONException e)
                            {
                                MangaLogger.logError(TAG, e.getMessage());
                            }

                            mMap.setHeaderUserName();
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse)
                        {
                            super.onFailure(statusCode, headers, throwable, errorResponse);
                            MangaLogger.logError(TAG, errorResponse.toString());
                        }
                    });
                }
                else if (o instanceof GoogleLogoutEvent)
                {
                    mMap.setHeaderUserName();
                }
            }, throwable -> MangaLogger.logError(TAG, throwable.getMessage()));
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }
}
