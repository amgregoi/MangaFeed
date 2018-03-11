package com.amgregoire.mangafeed.UI.Activities;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.FrameLayout;

import com.amgregoire.mangafeed.Common.WifiBroadcastReceiver;
import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.UI.Fragments.AccountFragment;
import com.amgregoire.mangafeed.UI.Fragments.DownloadsFragment;
import com.amgregoire.mangafeed.UI.Fragments.HomeFragment;
import com.amgregoire.mangafeed.UI.Fragments.OfflineFragment;
import com.amgregoire.mangafeed.Utils.BusEvents.UpdateSourceEvent;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NavigationActivity extends AppCompatActivity implements WifiBroadcastReceiver.WifiResponseListener
{
    public final static String TAG = NavigationActivity.class.getSimpleName();

    @BindView(R.id.navigationHomeToolbar) Toolbar mToolbar;
    @BindView(R.id.navigation) BottomNavigationView mBottomNav;
    @BindView(R.id.frameLayoutNavContainer) FrameLayout mFragmentContainer;

    private WifiBroadcastReceiver mReceiver;
    private int mMenuFlag = 0;
    private boolean mInternetFlag;
    private String mCurrentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        ButterKnife.bind(this);

        // TODO: implement presenter..
        initViews();
    }

    private void initViews()
    {
        mInternetFlag = WifiBroadcastReceiver.hasInternet(this);
        setupToolbar();
        setupNavigation();
        setupFragmentBackStack();
        setupRxBus();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mReceiver = new WifiBroadcastReceiver(this);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        try
        {
            unregisterReceiver(mReceiver);
        }
        catch (Exception ex)
        {
            Log.e(TAG, "Unregister receiver error: " + ex.getMessage());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        if (mMenuFlag == 0)
        {
            MenuInflater lInflater = getMenuInflater();
            lInflater.inflate(R.menu.menu_toolbar_home, menu);
            return true;
        }
        else if (mMenuFlag == 2)
        {
            MenuInflater lInflater = getMenuInflater();
            lInflater.inflate(R.menu.menu_toolbar_account, menu);
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void hasInternet()
    {
        mInternetFlag = true;

        if (mMenuFlag == 0)
        {
            setFragment(HomeFragment.TAG);

            HomeFragment lHome = (HomeFragment) getSupportFragmentManager().findFragmentByTag(HomeFragment.TAG);
            lHome.onInternetConnection();
        }
    }

    @Override
    public void hasNoInternet()
    {
        mInternetFlag = false;

        if (mMenuFlag == 0)
        {
            setFragment(OfflineFragment.TAG);
        }
    }

    /***
     * This function sets up the bottom navigation.
     *
     */
    private void setupNavigation()
    {
        mBottomNav.setOnNavigationItemSelectedListener(item ->
        {
            mMenuFlag = 0;
            switch (item.getItemId())
            {
                case R.id.navigation_home:
                    mMenuFlag = 0;
                    setTitle(MangaFeed.getInstance().getCurrentSource().getSourceName());
                    if (mInternetFlag)
                    {
                        setFragment(HomeFragment.TAG);
                    }
                    else
                    {
                        setFragment(OfflineFragment.TAG);
                    }
                    break;
                case R.id.navigation_downloads:
                    mMenuFlag = -1;
                    setTitle(R.string.nav_bottom_title_download);
                    setFragment(DownloadsFragment.TAG);
                    break;
                case R.id.navigation_account:
                    mMenuFlag = 2;
                    setTitle(R.string.nav_bottom_title_account);
                    setFragment(AccountFragment.TAG);
                    break;
                default:
                    return false;
            }

            invalidateOptionsMenu();
            return true;
        });
    }

    private void setupRxBus()
    {
        MangaFeed.getInstance().rxBus().toObservable().subscribe(o ->
        {
            if (o instanceof UpdateSourceEvent)
            {
                // Refresh fragment views
                Fragment home = getSupportFragmentManager().findFragmentByTag(HomeFragment.TAG);
                Fragment account = getSupportFragmentManager().findFragmentByTag(AccountFragment.TAG);

                getSupportFragmentManager().beginTransaction()
                                           .detach(home)
                                           .attach(home)
                                           .detach(account)
                                           .attach(account)
                                           .commit();
                MangaFeed.getInstance()
                         .makeSnackBarShort(findViewById(R.id.coordinatorLayoutSnack), "Source changed to " + MangaFeed
                                 .getInstance()
                                 .getCurrentSource()
                                 .getSourceName());
            }
        }, throwable -> Log.e(TAG, throwable.getMessage()));
    }


    /***
     * This fragment switches the active fragment to the newly specified fragment by its tag, and detaches the old.
     *
     * @param tag
     */
    private void setFragment(String tag)
    {
        Fragment lOld = getSupportFragmentManager().findFragmentByTag(mCurrentTag);
        Fragment lNew = getSupportFragmentManager().findFragmentByTag(tag);
        mCurrentTag = tag;
        getSupportFragmentManager().beginTransaction().hide(lOld).show(lNew).commit();
    }

    /***
     * This function initializes the backstack by adding the 4 primary fragments for the Navigation Activity.
     *
     */
    private void setupFragmentBackStack()
    {
        FragmentTransaction lTransaction = getSupportFragmentManager().beginTransaction();

        mCurrentTag = HomeFragment.TAG;
        Fragment lHome = HomeFragment.newInstance();
        Fragment lAccount = AccountFragment.newInstance();
        Fragment lDownload = DownloadsFragment.newInstance();
        Fragment lOffline = OfflineFragment.newInstance();

        lTransaction.add(R.id.frameLayoutNavContainer, lHome, HomeFragment.TAG)
                    .add(R.id.frameLayoutNavContainer, lAccount, AccountFragment.TAG)
                    .add(R.id.frameLayoutNavContainer, lDownload, DownloadsFragment.TAG)
                    .add(R.id.frameLayoutNavContainer, lOffline, OfflineFragment.TAG)
                    .hide(lAccount)
                    .hide(lDownload);

        if (!mInternetFlag)
        {
            lTransaction.hide(lHome);
        }
        else
        {
            lTransaction.hide(lOffline);
        }

        lTransaction.commit();
    }

    /***
     * This function sets up the activities toolbar.
     *
     */
    private void setupToolbar()
    {
        setSupportActionBar(mToolbar);
        setTitle(MangaFeed.getInstance().getCurrentSource().getSourceName());
    }
}
