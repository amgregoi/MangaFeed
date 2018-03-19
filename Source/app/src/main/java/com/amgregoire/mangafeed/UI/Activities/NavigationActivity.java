package com.amgregoire.mangafeed.UI.Activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
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
import com.amgregoire.mangafeed.Utils.BusEvents.GoogleLoginAttemptEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.GoogleLogoutEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.MangaSelectedEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.SearchQueryChangeEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.ToggleDownloadViewEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.UpdateSourceEvent;
import com.amgregoire.mangafeed.Utils.DownloadManager;
import com.amgregoire.mangafeed.Utils.LoginManager;
import com.amgregoire.mangafeed.Utils.MangaLogger;

import java.util.Stack;

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
    //    private int mMenuFlag = Menus.MENU_HOME;
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

        LoginManager.init(this);
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

    private void setupSearchView(Menu menu)
    {
        SearchView search = (SearchView) menu.findItem(R.id.menuHomeSearch).getActionView();
        SearchView.SearchAutoComplete theTextArea = search.findViewById(R.id.search_src_text);
        theTextArea.setTextColor(Color.WHITE);//or any color that you want
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String s)
            {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s)
            {
                MangaFeed.getInstance().rxBus().send(new SearchQueryChangeEvent(s));
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        mToolbar.setNavigationIcon(null);
        MenuInflater lInflater = getMenuInflater();
        lInflater.inflate(Menus.getMenu(), menu);

        switch (Menus.getMenu())
        {
            case Menus.MENU_HOME:
                setTitle(MangaFeed.getInstance().getCurrentSource().getSourceName());
                setupSearchView(menu);
                break;
            case Menus.MENU_HOME_MANGA_INFO:
                mToolbar.setNavigationIcon(mDrawBack);
                break;
            case Menus.MENU_HOME_MANGA_INFO_DOWNLOAD:
                mToolbar.setNavigationIcon(mDrawWhiteOutline);
                setTitle("Select Items");
                break;
            case Menus.MENU_DOWNLOADS:
                setTitle(R.string.nav_bottom_title_download);
                setupSearchView(menu);
                break;
            case Menus.MENU_DOWNLOADS_MANGA_INFO:
                mToolbar.setNavigationIcon(mDrawBack);
                break;
            case Menus.MENU_DOWNLOADS_MANGA_INFO_REMOVE:
                mToolbar.setNavigationIcon(mDrawWhiteOutline);
                setTitle("Select Items");
                break;
            case Menus.MENU_ACCOUNT:
                setTitle(R.string.empty);
                break;
            default:
                return super.onCreateOptionsMenu(menu);
        }

        return true;
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
            case R.id.menuMangaInfoRefresh:
                lMangaFragment.onRefreshInfo();
                break;
            case R.id.menuMangaInfoDownload:
                Menus.setMenu(Menus.MENU_HOME_MANGA_INFO_DOWNLOAD);
                invalidateOptionsMenu();
                lMangaFragment.onDownloadViewEnabled();
                break;
            case R.id.menuDownloadsMangaInfoDownload:
                Menus.setMenu(Menus.MENU_DOWNLOADS_MANGA_INFO_REMOVE);
                invalidateOptionsMenu();
                lMangaFragment.onDownloadViewEnabled();
                break;
            case R.id.menuDownloadMangaInfoCancel:
                lMangaFragment.onDownloadCancel();
                break;
            case R.id.menuMangaInfoDownloadDownload:
                if (DownloadManager.isStoragePermissionGranted(this))
                {
                    lMangaFragment.onDownloadDownload(); // start download
                    lMangaFragment.onDownloadCancel(); // exit download view
                    MangaFeed.getInstance().makeToastShort("Starting downloads now");
                }
                break;
            case R.id.menuDownloadsMangaInfoRemove:
                lMangaFragment.onDownloadRemove();
                lMangaFragment.onDownloadCancel();
                MangaLogger.logError(TAG, "test");
                break;
            case android.R.id.home:
                switch (Menus.getMenu())
                {
                    case Menus.MENU_HOME_MANGA_INFO:
                    case Menus.MENU_DOWNLOADS_MANGA_INFO:
                        onBackPressed();
                        break;
                    default:
                        if (mToolbar.getNavigationIcon() == mDrawWhiteOutline)
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            MangaFeed.getInstance().makeToastShort("You may now download chapters!");
        }
    }

    @Override
    public void hasInternet()
    {
        mInternetFlag = true;

        if (Menus.getMenu() == Menus.MENU_HOME)
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

        if (Menus.getMenu() == Menus.MENU_HOME)
        {
            setFragment(OfflineFragment.TAG);
        }
    }


    @Override
    public void onBackPressed()
    {
        if (Menus.getMenu() == Menus.MENU_HOME_MANGA_INFO_DOWNLOAD || Menus.getMenu() == Menus.MENU_DOWNLOADS_MANGA_INFO_REMOVE)
        {
            MangaInfoFragment lMangaFragment = (MangaInfoFragment) getSupportFragmentManager().findFragmentByTag(MangaInfoFragment.TAG);
            lMangaFragment.onDownloadCancel();
        }
        else if (getSupportFragmentManager().getBackStackEntryCount() > 0)
        {
            getSupportFragmentManager().popBackStack();

            Menus.setToPrevMenu();

//            if (mMenuFlag == Menus.MENU_HOME_MANGA_INFO)
//            {
//                mMenuFlag = Menus.MENU_HOME;
//            }
//            else if (mMenuFlag == Menus.MENU_DOWNLOADS_MANGA_INFO)
//            {
//                mMenuFlag = Menus.MENU_DOWNLOADS;
//            }
//            else
//            {
//                mMenuFlag = Menus.MENU_ACCOUNT;
//            }

            invalidateOptionsMenu();
        }
        else
        {
            if (!mCurrentTag.equals(HomeFragment.TAG) || !mExitAppFlag)
            {
                mBottomNav.setSelectedItemId(R.id.menuBottomNavCatalog);
                MangaFeed.getInstance().makeToastShort("Press back again to exit");
                mExitAppFlag = true;
            }
            else
            {
                // implement double back to exit
                super.onBackPressed();
            }
        }
    }

    private boolean mExitAppFlag = false;

    /***
     * this function subscribes to the relevant Rx event bus events.
     *
     * TODO: move subscribe/unsubscribe to rxbus to onresume/onpause when refactoring to presenters
     */
    private void setupRxBus()
    {
        MangaFeed.getInstance().rxBus().toObservable().subscribe(o ->
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

                MangaFeed.getInstance()
                         .makeSnackBarShort
                                 (findViewById(R.id.coordinatorLayoutSnack), "Source changed to " + MangaFeed
                                         .getInstance()
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

                setTitle(lEvent.manga.title);
                if (lEvent.isOffline)
                {
                    Menus.setMenu(Menus.MENU_DOWNLOADS_MANGA_INFO);
                }
                else
                {
                    Menus.setMenu(Menus.MENU_HOME_MANGA_INFO);
                }

                invalidateOptionsMenu();
            }
            else if (o instanceof ToggleDownloadViewEvent)
            {
                MangaInfoFragment lMangaFragment = (MangaInfoFragment) getSupportFragmentManager().findFragmentByTag(MangaInfoFragment.TAG);

                switch (Menus.getMenu())
                {
                    case Menus.MENU_HOME_MANGA_INFO:
                        Menus.setMenu(Menus.MENU_HOME_MANGA_INFO_DOWNLOAD);
                        lMangaFragment.onDownloadViewEnabled();
                        break;
                    case Menus.MENU_HOME_MANGA_INFO_DOWNLOAD:
                        Menus.setToPrevMenu();
                        setTitle(((ToggleDownloadViewEvent) o).manga.title);
                        break;
                    case Menus.MENU_DOWNLOADS_MANGA_INFO:
                        Menus.setMenu(Menus.MENU_DOWNLOADS_MANGA_INFO_REMOVE);
                        lMangaFragment.onDownloadViewEnabled();
                    default:
                        Menus.setToPrevMenu();
                        setTitle(((ToggleDownloadViewEvent) o).manga.title);
                }

                invalidateOptionsMenu();
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
                    Menus.setMenu(Menus.MENU_HOME);
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
                    Menus.setMenu(Menus.MENU_DOWNLOADS);
                    setFragment(DownloadsFragment.TAG);
                    break;
                case R.id.menuBottomNavAccount:
                    Menus.setMenu(Menus.MENU_ACCOUNT);
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
     * This function sets up the activities toolbar.
     *
     */
    private void setupToolbar()
    {
        setSupportActionBar(mToolbar);
        setTitle(MangaFeed.getInstance().getCurrentSource().getSourceName());
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


    /***
     * This class holds the various menu identifiers used to toggle between the different toolbar menus.
     *
     */
    public static class Menus
    {
        public final static int MENU_HOME = R.menu.menu_toolbar_home;
        public final static int MENU_ACCOUNT = R.menu.menu_toolbar_account;
        public final static int MENU_DOWNLOADS = R.menu.menu_toolbar_downloads;
        public final static int MENU_DOWNLOADS_MANGA_INFO = R.menu.menu_toolbar_downloads_manga_info;
        public final static int MENU_DOWNLOADS_MANGA_INFO_REMOVE = R.menu.menu_toolbar_downloads_manga_info_remove;
        public final static int MENU_HOME_MANGA_INFO = R.menu.menu_toolbar_home_manga_info;
        public final static int MENU_HOME_MANGA_INFO_DOWNLOAD = R.menu.menu_toolbar_home_manga_info_download;

        private static int sCURRENT_MENU = MENU_HOME;
        private static Stack<Integer> sPREV_MENUS = new Stack<>();

        public static int getMenu()
        {
            return sCURRENT_MENU;
        }

        public static void setToPrevMenu()
        {
            sCURRENT_MENU = sPREV_MENUS.pop();
        }

        public static void setMenu(int menu)
        {
            sPREV_MENUS.push(sCURRENT_MENU);
            sCURRENT_MENU = menu;
        }
    }
}
