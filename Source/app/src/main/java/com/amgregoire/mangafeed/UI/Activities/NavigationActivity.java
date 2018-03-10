package com.amgregoire.mangafeed.UI.Activities;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.FrameLayout;

import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.UI.Fragments.AccountFragment;
import com.amgregoire.mangafeed.UI.Fragments.DownloadsFragment;
import com.amgregoire.mangafeed.UI.Fragments.HomeFragment;
import com.amgregoire.mangafeed.UI.Fragments.OfflineFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NavigationActivity extends AppCompatActivity
{

    @BindView(R.id.navigationHomeToolbar) Toolbar mToolbar;
    @BindView(R.id.navigation) BottomNavigationView mBottomNav;
    @BindView(R.id.frameLayoutNavContainer) FrameLayout mFragmentContainer;

    private int mMenuFlag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        ButterKnife.bind(this);

        mToolbar = findViewById(R.id.navigationHomeToolbar);
        setSupportActionBar(mToolbar);
        setupNavigation();
        setupFragmentBackStack();
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
        else if(mMenuFlag == 2)
        {
            MenuInflater lInflater = getMenuInflater();
            lInflater.inflate(R.menu.menu_toolbar_account, menu);
            return true;
        }

        return super.onCreateOptionsMenu(menu);
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
                    setTitle(R.string.nav_bottom_title_catalog);
                    setFragment(HomeFragment.TAG);
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

    private String mCurrentTag;

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
        mCurrentTag = HomeFragment.TAG;
        Fragment lHome = HomeFragment.newInstance();
        Fragment lAccount = AccountFragment.newInstance();
        Fragment lDownload = DownloadsFragment.newInstance();
        Fragment lOffline = OfflineFragment.newInstance();

        getSupportFragmentManager().beginTransaction()
                                   .add(R.id.frameLayoutNavContainer, lHome, HomeFragment.TAG)
                                   .add(R.id.frameLayoutNavContainer, lAccount, AccountFragment.TAG)
                                   .add(R.id.frameLayoutNavContainer, lDownload, DownloadsFragment.TAG)
                                   .add(R.id.frameLayoutNavContainer, lOffline, OfflineFragment.TAG)
                                   .hide(lOffline)
                                   .hide(lAccount)
                                   .hide(lDownload)
                                   .commit();
    }
}
