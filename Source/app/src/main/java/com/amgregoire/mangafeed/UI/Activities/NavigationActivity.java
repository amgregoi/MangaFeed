package com.amgregoire.mangafeed.UI.Activities;

import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
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
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.amgregoire.mangafeed.Common.WifiBroadcastReceiver;
import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.UI.Fragments.AccountFragment;
import com.amgregoire.mangafeed.UI.Fragments.DownloadsFragment;
import com.amgregoire.mangafeed.UI.Fragments.HomeFragment;
import com.amgregoire.mangafeed.UI.Fragments.MangaInfoFragment;
import com.amgregoire.mangafeed.UI.Fragments.OfflineFragment;
import com.amgregoire.mangafeed.Utils.BusEvents.DownloadSelectAllEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.MangaSelectedEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.UpdateSourceEvent;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;

public class NavigationActivity extends AppCompatActivity implements WifiBroadcastReceiver.WifiResponseListener
{
    public final static String TAG = NavigationActivity.class.getSimpleName();

    @BindView(R.id.navigationHomeToolbar) Toolbar mToolbar;
    @BindView(R.id.navigation) BottomNavigationView mBottomNav;
    @BindView(R.id.frameLayoutNavContainer) FrameLayout mFragmentContainer;

    @BindDrawable(R.drawable.ic_checkbox_blank_circle_outline_white_24dp) Drawable mDrawWhiteOutline;
    @BindDrawable(R.drawable.ic_check_circle_outline_white_24dp) Drawable mDrawWhiteChecked;
    @BindDrawable(R.drawable.navigation_back) Drawable mDrawBack;

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
        mToolbar.setNavigationIcon(null);

        if (mMenuFlag == Menus.MENU_HOME)
        {
            MenuInflater lInflater = getMenuInflater();
            lInflater.inflate(R.menu.menu_toolbar_home, menu);
            setTitle(MangaFeed.getInstance().getCurrentSource().getSourceName());
            return true;
        }
        else if (mMenuFlag == Menus.MENU_DOWNLOADS)
        {
            MenuInflater lInflater = getMenuInflater();
            lInflater.inflate(R.menu.menu_toolbar_home, menu);
            setTitle(R.string.nav_bottom_title_download);
            return true;
        }
        else if (mMenuFlag == Menus.MENU_ACCOUNT)
        {
            MenuInflater lInflater = getMenuInflater();
            lInflater.inflate(R.menu.menu_toolbar_account, menu);
            setTitle(R.string.nav_bottom_title_account);
            return true;
        }
        else if (mMenuFlag == Menus.MENU_MANGA_INFO)
        {
            MenuInflater lInflater = getMenuInflater();
            lInflater.inflate(R.menu.menu_toolbar_manga_info, menu);
            mToolbar.setNavigationIcon(mDrawBack);
            return true;
        }
        else if (mMenuFlag == Menus.MENU_MANGA_DOWNLOAD)
        {
            MenuInflater lInflater = getMenuInflater();
            lInflater.inflate(R.menu.menu_toolbar_manga_info_download, menu);
            mToolbar.setNavigationIcon(mDrawWhiteOutline);
            setTitle("Select Items");
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        MangaInfoFragment lMangaFragment = (MangaInfoFragment) getSupportFragmentManager().findFragmentByTag(MangaInfoFragment.TAG);

        switch (item.getItemId())
        {
            case R.id.menuAccountSettings:
                // start settings fragment
                break;
            case R.id.menuHomeSearch:
                // send query listener update
                break;
            case R.id.menuMangaInfoRefresh:
                lMangaFragment.onRefreshInfo();
                break;
            case R.id.menuMangaInfoDownload:
                mMenuFlag = Menus.MENU_MANGA_DOWNLOAD;
                invalidateOptionsMenu();
                lMangaFragment.onDownloadViewEnabled();
                break;
            case R.id.menuMangaInfoDownloadCancel:
                lMangaFragment.onDownloadCancel();
                break;
            case R.id.menuMangaInfoDownloadDownload:
                lMangaFragment.onDownloadDownload();
                mMenuFlag = Menus.MENU_MANGA_INFO;
                invalidateOptionsMenu();
                MangaFeed.getInstance().makeToastShort("Starting downloads now");
                break;
            case android.R.id.home:
                if (mMenuFlag == Menus.MENU_MANGA_INFO)
                {
                    mMenuFlag = Menus.MENU_HOME;
                    invalidateOptionsMenu();
                    onBackPressed();
                }
                else
                {
                    Drawable lCurrent = mToolbar.getNavigationIcon();

                    if (lCurrent == mDrawWhiteOutline)
                    {
                        mToolbar.setNavigationIcon(mDrawWhiteChecked);
                        lMangaFragment.onSelectAllOrNone(true);
                    }
                    else
                    {
                        mToolbar.setNavigationIcon(mDrawWhiteOutline);
                        lMangaFragment.onSelectAllOrNone(false);
                    }
                }
        }

        return true;
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

        if (mMenuFlag == Menus.MENU_HOME)
        {
            setFragment(OfflineFragment.TAG);
        }
    }


    @Override
    public void onBackPressed()
    {
        if (mMenuFlag == Menus.MENU_MANGA_DOWNLOAD)
        {
            MangaInfoFragment lMangaFragment = (MangaInfoFragment) getSupportFragmentManager().findFragmentByTag(MangaInfoFragment.TAG);
            lMangaFragment.onDownloadCancel();
        }
        else if (getSupportFragmentManager().getBackStackEntryCount() > 0)
        {
            getSupportFragmentManager().popBackStack();
            mMenuFlag = Menus.MENU_HOME;
            invalidateOptionsMenu();
        }
        else
        {
            // implement double back to exit
            super.onBackPressed();
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
            mMenuFlag = Menus.MENU_HOME;

            switch (item.getItemId())
            {
                case R.id.menuBottomNavCatalog:
                    mMenuFlag = Menus.MENU_HOME;
                    if (mInternetFlag)
                    {
                        setFragment(HomeFragment.TAG);
                    }
                    else
                    {
                        setFragment(OfflineFragment.TAG);
                    }
                    break;
                case R.id.menuBottomNavDownloads:
                    mMenuFlag = Menus.MENU_DOWNLOADS;
                    setFragment(DownloadsFragment.TAG);
                    break;
                case R.id.menuBottomNavAccount:
                    mMenuFlag = Menus.MENU_ACCOUNT;
                    setFragment(AccountFragment.TAG);
                    break;
                default:
                    return false;
            }

            invalidateOptionsMenu();
            return true;
        });
    }

    /***
     * this function subscribes to the relevant Rx event bus events.
     *
     */
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
            else if (o instanceof MangaSelectedEvent)
            {
                MangaSelectedEvent lEvent = (MangaSelectedEvent) o;
                Fragment lMangaFragment = MangaInfoFragment.newInstance(lEvent.manga);

                getSupportFragmentManager().beginTransaction()
                                           .add(R.id.frameLayoutMasterContainer, lMangaFragment, MangaInfoFragment.TAG)
                                           .addToBackStack(MangaInfoFragment.TAG)
                                           .commit();

                setTitle(lEvent.manga.title);
                mMenuFlag = Menus.MENU_MANGA_INFO;
                invalidateOptionsMenu();
            }
            else if (o instanceof DownloadSelectAllEvent)
            {
                MangaInfoFragment lMangaFragment = (MangaInfoFragment) getSupportFragmentManager().findFragmentByTag(MangaInfoFragment.TAG);

                if (mMenuFlag == Menus.MENU_MANGA_INFO)
                {
                    mMenuFlag = Menus.MENU_MANGA_DOWNLOAD;
                    invalidateOptionsMenu();
                    lMangaFragment.onDownloadViewEnabled();
                }
                else
                {
                    setTitle(((DownloadSelectAllEvent) o).manga.title);
                    mMenuFlag = Menus.MENU_MANGA_INFO;
                    invalidateOptionsMenu();
                }
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

    /***
     * This class holds the various menu identifiers used to toggle between the different toolbar menus.
     *
     */
    public class Menus
    {
        public final static int MENU_HOME = 0;
        public final static int MENU_ACCOUNT = 1;
        public final static int MENU_DOWNLOADS = 2;
        public final static int MENU_MANGA_INFO = 3;
        public final static int MENU_MANGA_DOWNLOAD = 4;
    }
}
