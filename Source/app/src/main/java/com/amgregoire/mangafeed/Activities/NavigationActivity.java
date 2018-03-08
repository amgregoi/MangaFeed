package com.amgregoire.mangafeed.Activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.amgregoire.mangafeed.Fragments.AccountFragment;
import com.amgregoire.mangafeed.Fragments.DownloadsFragment;
import com.amgregoire.mangafeed.Fragments.HomeFragment;
import com.amgregoire.mangafeed.Fragments.OfflineFragment;
import com.amgregoire.mangafeed.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NavigationActivity extends AppCompatActivity
{

    @BindView(R.id.navigationHomeToolbar) Toolbar mToolbar;
    @BindView(R.id.navigation) BottomNavigationView mBottomNav;
    @BindView(R.id.frameLayoutNavContainer) FrameLayout mFragmentContainer;

    private boolean mHomeFlag = true;

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
        if (mHomeFlag)
        {
            MenuInflater lInflater = getMenuInflater();
            lInflater.inflate(R.menu.menu_toolbar_home, menu);
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
        mBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                mHomeFlag = false;
                switch (item.getItemId())
                {
                    case R.id.navigation_home:
                        mHomeFlag = true;
                        setTitle(R.string.nav_bottom_title_catalog);
                        setFragment(HomeFragment.TAG);
                        break;
                    case R.id.navigation_downloads:
                        setTitle(R.string.nav_bottom_title_download);
                        setFragment(DownloadsFragment.TAG);
                        break;
                    case R.id.navigation_account:
                        setTitle(R.string.nav_bottom_title_account);
                        setFragment(AccountFragment.TAG);
                        break;
                    default:
                        return false;
                }

                invalidateOptionsMenu();
                return true;
            }
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
        getSupportFragmentManager().beginTransaction().detach(lOld).attach(lNew).commit();
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
                                   .detach(lOffline)
                                   .detach(lAccount)
                                   .detach(lDownload)
                                   .commit();
    }
}
