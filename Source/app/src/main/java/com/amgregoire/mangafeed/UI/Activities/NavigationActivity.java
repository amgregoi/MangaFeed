package com.amgregoire.mangafeed.UI.Activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.amgregoire.mangafeed.Common.WifiBroadcastReceiver;
import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.UI.BackHandledFragment;
import com.amgregoire.mangafeed.UI.Fragments.AccountFragment;
import com.amgregoire.mangafeed.UI.Fragments.AccountFragmentFiltered;
import com.amgregoire.mangafeed.UI.Fragments.AccountFragmentSettings;
import com.amgregoire.mangafeed.UI.Fragments.DownloadsFragment;
import com.amgregoire.mangafeed.UI.Fragments.HomeFragment;
import com.amgregoire.mangafeed.UI.Fragments.MangaInfoFragment;
import com.amgregoire.mangafeed.UI.Fragments.OfflineFragment;
import com.amgregoire.mangafeed.UI.Fragments.ReaderFragment;
import com.amgregoire.mangafeed.Utils.BusEvents.ChapterSelectedEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.GoogleLoginAttemptEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.GoogleLogoutEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.MangaSelectedEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.SearchQueryChangeEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.StatusFilterEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.UpdateMangaInfoEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.UpdateSourceEvent;
import com.amgregoire.mangafeed.Utils.DownloadScheduler;
import com.amgregoire.mangafeed.Utils.LoginManager;
import com.amgregoire.mangafeed.Utils.MangaLogger;
import com.bumptech.glide.Glide;


import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;

public class NavigationActivity extends AppCompatActivity implements WifiBroadcastReceiver.WifiResponseListener, BackHandledFragment.BackHandlerInterface
{
    public final static String TAG = NavigationActivity.class.getSimpleName();

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.navigation) BottomNavigationView mBottomNav;
    @BindView(R.id.frameLayoutNavContainer) FrameLayout mFragmentContainer;

    @BindDrawable(R.drawable.ic_checkbox_blank_circle_outline_white_24dp) Drawable mDrawWhiteOutline;
    @BindDrawable(R.drawable.ic_check_circle_outline_white_24dp) Drawable mDrawWhiteChecked;
    @BindDrawable(R.drawable.nav_back_white) Drawable mDrawBack;

    private BackHandledFragment selectedFragment;
    private WifiBroadcastReceiver mReceiver;

    private boolean mInternetFlag;
    private String mCurrentTag;
    private int mCurrentMenu;
    private Disposable mRxBus;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        ButterKnife.bind(this);

        initViews();
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
//        Glide.with(this).onLowMemory();
    }

    @Override
    public void onTrimMemory(int level)
    {
        super.onTrimMemory(level);
//        Glide.with(this).onTrimMemory(Glide.TRIM_MEMORY_RUNNING_LOW);
    }

    private void initViews()
    {
        mInternetFlag = WifiBroadcastReceiver.hasInternet(this);
        mCurrentMenu = R.menu.menu_toolbar_filter;
        setupToolbar();
        setupNavigation();
        setupFragmentBackStack();

        LoginManager.init(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        mReceiver = new WifiBroadcastReceiver(this);
        registerReceiver(mReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        setupRxBus();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        try
        {
            mRxBus.dispose();
            unregisterReceiver(mReceiver);
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, "Unregister receiver error", ex.getMessage());
        }

    }

    private void setupSearchView(Menu menu)
    {
        SearchView lSearch = (SearchView) menu.findItem(R.id.menuHomeSearch).getActionView();
        SearchView.SearchAutoComplete lTextArea = lSearch.findViewById(R.id.search_src_text);
        lTextArea.setTextColor(Color.WHITE);//or any color that you want
        lSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String s)
            {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s)
            {
                MangaFeed.Companion.getApp().rxBus().send(new SearchQueryChangeEvent(s));
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
        {
            return super.onCreateOptionsMenu(menu);
        }

        mToolbar.setNavigationIcon(null);
        MenuInflater lInflater = getMenuInflater();
        lInflater.inflate(mCurrentMenu, menu);

        switch (mCurrentMenu)
        {
            case R.menu.menu_toolbar_filter:
                setTitle(MangaFeed.Companion.getApp().getCurrentSource().getSourceName());
                setupSearchView(menu);
                break;
            case R.menu.menu_toolbar_downloads:
                setTitle(R.string.nav_bottom_title_download);
                setupSearchView(menu);
                break;
            case R.menu.menu_toolbar_account:
                setTitle(R.string.empty);
            default:
                return super.onCreateOptionsMenu(menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
        {
            return super.onOptionsItemSelected(item);
        }

        switch (item.getItemId())
        {
            case R.id.menuAccountSettings:
                // start settings fragment
                getSupportFragmentManager().beginTransaction()
                                           .add(R.id.frameLayoutMasterContainer, AccountFragmentSettings.newInstance(), AccountFragmentSettings.TAG)
                                           .addToBackStack(AccountFragmentSettings.TAG)
                                           .commit();

                break;
            case R.id.menuDownloadsCancelAll:
                DownloadScheduler.clearDownloads();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            MangaFeed.Companion.getApp().makeToastShort("You may now download chapters!");
        }
    }

    @Override
    public void hasInternet()
    {
        mInternetFlag = true;

        if (mCurrentMenu == R.menu.menu_toolbar_filter)
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

        if (mCurrentMenu == R.menu.menu_toolbar_filter)
        {
            setFragment(OfflineFragment.TAG);
        }
    }


    @Override
    public void onBackPressed()
    {
        Glide.get(this).clearMemory();

        if (selectedFragment == null || selectedFragment.onBackPressed())
        {
            FragmentManager lManager = getSupportFragmentManager();

            if (lManager.getBackStackEntryCount() > 0)
            {
                lManager.popBackStack();
                if (lManager.getBackStackEntryCount() == 1)
                {
                    setSupportActionBar(mToolbar);
                }
                else
                {
                    MangaFeed.Companion.getApp().rxBus().send(new UpdateMangaInfoEvent());
                }
            }
            else
            {
                super.onBackPressed();
            }
        }
    }

    /***
     * this function subscribes to the relevant Rx event bus events.
     *
     * TODO: move subscribe/unsubscribe to rxbus to onresume/onpause when refactoring to presenters
     */
    private void setupRxBus()
    {
        mRxBus = MangaFeed.Companion.getApp().rxBus().toObservable().subscribe(o ->
        {
            if (o instanceof UpdateSourceEvent)
            {
                // Refresh fragment views
                Fragment lHome = getSupportFragmentManager().findFragmentByTag(HomeFragment.TAG);
                Fragment lAccount = getSupportFragmentManager().findFragmentByTag(AccountFragment.TAG);

                getSupportFragmentManager().beginTransaction()
                                           .detach(lHome)
                                           .attach(lHome)
                                           .detach(lAccount)
                                           .attach(lAccount)
                                           .commit();

                MangaFeed.Companion.getApp().makeToastLong("Source changed to " + MangaFeed
                        .Companion.getApp()
                        .getCurrentSource()
                        .getSourceName());
            }
            else if (o instanceof MangaSelectedEvent)
            {
                MangaSelectedEvent lEvent = (MangaSelectedEvent) o;
                Fragment lMangaFragment = MangaInfoFragment.newInstance(lEvent.manga, lEvent.isOffline);

                getSupportFragmentManager().beginTransaction()
                                           .add(R.id.frameLayoutMasterContainer, lMangaFragment, MangaInfoFragment.TAG)
                                           .addToBackStack(MangaInfoFragment.TAG)
                                           .commit();
            }
            else if (o instanceof StatusFilterEvent)
            {
                StatusFilterEvent lEvent = (StatusFilterEvent) o;
                Fragment lFragment = AccountFragmentFiltered.newInstance(lEvent.filter, lEvent.title);
                getSupportFragmentManager().beginTransaction()
                                           .add(R.id.frameLayoutMasterContainer, lFragment)
                                           .addToBackStack(null)
                                           .commit();
            }
            else if (o instanceof ChapterSelectedEvent)
            {
                ChapterSelectedEvent lEvent = (ChapterSelectedEvent) o;
                Fragment lReaderFragment = ReaderFragment.newInstance(lEvent.manga, lEvent.position);
                getSupportFragmentManager().beginTransaction()
                                           .add(R.id.frameLayoutMasterContainer, lReaderFragment, ReaderFragment.TAG)
                                           .addToBackStack(MangaInfoFragment.TAG)
                                           .commit();
            }
            else if (o instanceof GoogleLoginAttemptEvent)
            {
                LoginManager.login(this);
            }
            else if (o instanceof GoogleLogoutEvent)
            {
                LoginManager.logout();
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

        if (mCurrentTag.equals(DownloadsFragment.TAG))
        {
            getSupportFragmentManager().beginTransaction()
                                       .hide(lOld)
                                       .show(lNew)
                                       .detach(lNew)
                                       .attach(lNew)
                                       .commit();
        }
        else
        {
            getSupportFragmentManager().beginTransaction().hide(lOld).show(lNew).commit();
        }
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
     * This function sets up the bottom navigation.
     *
     */
    private void setupNavigation()
    {
        mBottomNav.setOnNavigationItemSelectedListener(item ->
        {
            switch (item.getItemId())
            {
                case R.id.menuBottomNavCatalog:
                    mCurrentMenu = HomeFragment.MENU_RESOURCE;
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
                    mCurrentMenu = DownloadsFragment.MENU_RESOURCE;
                    setFragment(DownloadsFragment.TAG);
                    break;
                case R.id.menuBottomNavAccount:
                    mCurrentMenu = AccountFragment.MENU_RESOURCE;
                    setFragment(AccountFragment.TAG);
                    break;
                default:
                    return false;
            }

            supportInvalidateOptionsMenu();
            return true;
        });
    }

    /***
     * This function sets up the activities toolbar.
     *
     */
    private void setupToolbar()
    {
        setSupportActionBar(mToolbar);
        setTitle(MangaFeed.Companion.getApp().getCurrentSource().getSourceName());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 8008)
        {
            LoginManager.loginResult(data);
        }
    }

    @Override
    public void setSelectedFragment(BackHandledFragment backHandledFragment)
    {
        selectedFragment = backHandledFragment;
    }
}
