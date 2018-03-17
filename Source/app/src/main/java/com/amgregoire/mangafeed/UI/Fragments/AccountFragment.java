package com.amgregoire.mangafeed.UI.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.UI.Mappers.IAccount;
import com.amgregoire.mangafeed.UI.Presenters.AccountPres;
import com.amgregoire.mangafeed.Utils.BusEvents.GoogleLoginSuccessEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.GoogleLogoutEvent;
import com.amgregoire.mangafeed.Utils.MangaFeedRest;
import com.amgregoire.mangafeed.Utils.MangaLogger;
import com.amgregoire.mangafeed.Utils.SharedPrefs;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public class AccountFragment extends Fragment implements IAccount.AccountMap
{
    public final static String TAG = AccountFragment.class.getSimpleName();

    @BindView(R.id.scrollViewAccountRoot) NestedScrollView mScroll;
    @BindView(R.id.tabLayoutAccount) TabLayout mTabLayout;
    @BindView(R.id.viewPagerAccount) ViewPager mViewPager;

    @BindView(R.id.textViewUserName) TextView mUserName;

    private IAccount.AccountPres mPresenter;

    public static AccountFragment newInstance()
    {
        return new AccountFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View lView = inflater.inflate(R.layout.fragment_account, null);
        ButterKnife.bind(this, lView);

        mPresenter = new AccountPres(this, getChildFragmentManager());
        mPresenter.init(getArguments());

        setupRxBus();

        return lView;
    }

    @Override
    public void initViews()
    {
        setupTabLayout();
        mScroll.setFillViewport(true);
        setHeaderUserName();
    }

    @Override
    public void registerAdapter(PagerAdapter adapter)
    {
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mViewPager.setOffscreenPageLimit(1);
    }

    @OnClick({R.id.linearLayoutUserContainer, R.id.imageViewAccountProfile})
    public void onAttemptLogin()
    {
        if (SharedPrefs.getGoogleEmail() == null)
        {
//            MangaFeed.getInstance().rxBus().send(new GoogleLoginAttemptEvent());

            // TODO
            // Remove this, and uncomment above line when done testing login features
            // Login only succeeds with signed apks
            MangaFeed.getInstance().rxBus().send(new GoogleLoginSuccessEvent(null));
        }
        else
        {
            AlertDialog lDialog = new AlertDialog.Builder(getContext()).create();
            lDialog.setTitle("Logout");
            lDialog.setMessage("Are you sure you want to logout?  \nThis will reset your library, until you log back in.");
            lDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", (dialogInterface, i) -> lDialog.dismiss());
            lDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok", (dialogInterface, i) ->
            {
                MangaFeed.getInstance().rxBus().send(new GoogleLogoutEvent());
                SharedPrefs.setGoogleEmail(null);
                SharedPrefs.setGoogleName(null);
                setHeaderUserName();
                // Clear library
            });

            lDialog.show();
        }
    }

    private void setHeaderUserName()
    {
        String lUserName = SharedPrefs.getGoogleName();
        mUserName.setText(lUserName == null ? "Guest" : lUserName);
    }

    /***
     * This function sets up the tab layout.
     *
     */
    private void setupTabLayout()
    {
        mTabLayout.addTab(mTabLayout.newTab().setText("Sources"));
        mTabLayout.addTab(mTabLayout.newTab().setText("Stats"));

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                mViewPager.setCurrentItem(tab.getPosition(), false);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {
                // do nothing
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {
                // do nothing
            }
        });
    }

    private void setupRxBus()
    {
        MangaFeed.getInstance().rxBus().toObservable().subscribe(o ->
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

                            SharedPrefs.setGoogleEmail(lUser.getString("email"));
                            SharedPrefs.setGoogleName(lUser.getString("name"));
                            MangaFeed.getInstance().makeToastShort("Successfully signed in");
                        }
                        catch (JSONException e)
                        {
                            MangaLogger.logError(TAG, e.getMessage());
                        }

                        setHeaderUserName();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse)
                    {
                        super.onFailure(statusCode, headers, throwable, errorResponse);
                        MangaLogger.logError(TAG, errorResponse.toString());
                    }
                });
            }
        }, throwable -> MangaLogger.logError(TAG, throwable.getMessage()));
    }


}
