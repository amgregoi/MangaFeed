package com.amgregoire.mangafeed.UI.Fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.amgregoire.mangafeed.Common.MangaEnums;
import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.Manga;
import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.UI.Activities.NavigationActivity;
import com.amgregoire.mangafeed.UI.Adapters.MangaInfoChaptersAdapter;
import com.amgregoire.mangafeed.UI.BackHandledFragment;
import com.amgregoire.mangafeed.UI.Mappers.IManga;
import com.amgregoire.mangafeed.UI.Presenters.MangaInfoPres;
import com.amgregoire.mangafeed.Utils.DownloadManager;
import com.amgregoire.mangafeed.Utils.MangaLogger;
import com.l4digital.fastscroll.FastScrollRecyclerView;

import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Andy Gregoire on 3/12/2018.
 */

public class MangaInfoFragment extends BackHandledFragment implements IManga.MangaMap
{
    public final static String TAG = MangaInfoFragment.class.getSimpleName();
    public final static String MANGA_KEY = TAG + "MANGA";
    public final static String OFFLINE_KEY = TAG + "OFFLINE";

    @BindView(R.id.recyclerViewMangaInfo) FastScrollRecyclerView mRecyclerView;
    @BindView(R.id.bottomNavigationMangaInfo) BottomNavigationView mBottomNav;
    @BindView(R.id.swipeRefreshLayoutMangaInfo) SwipeRefreshLayout mSwipeLayout;
    @BindView(R.id.toolbar) Toolbar mToolbar;

    @BindColor(R.color.manga_blue) int mColorBlue;
    @BindColor(R.color.manga_red) int mColorRed;
    @BindColor(R.color.manga_green) int mColorGreen;
    @BindColor(R.color.manga_gray) int mColorGray;
    @BindColor(R.color.manga_white) int mColorWhite;

    @BindDrawable(R.drawable.ic_check_circle_outline_white_24dp) Drawable mSelectFilled;
    @BindDrawable(R.drawable.ic_checkbox_blank_circle_outline_white_24dp) Drawable mSelectOutline;

    private IManga.MangaPres mPresenter;

    public static Fragment newInstance(Manga manga, boolean offline)
    {
        Bundle lBundle = new Bundle();
        lBundle.putParcelable(MANGA_KEY, manga);
        lBundle.putBoolean(OFFLINE_KEY, offline);

        Fragment lFragment = new MangaInfoFragment();
        lFragment.setArguments(lBundle);

        return lFragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        setHasOptionsMenu(true);

        View lView = inflater.inflate(R.layout.fragment_manga_info, null);
        ButterKnife.bind(this, lView);

        mPresenter = new MangaInfoPres(this);
        mPresenter.init(getArguments());

        return lView;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mPresenter.subEventBus();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mPresenter.unSubEventBus();
    }

    @Override
    public void initViews()
    {
        setupBottomNav();
        ((NavigationActivity) getActivity()).setSupportActionBar(mToolbar);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        if (!mPresenter.isDownload())
        {
            if (!mPresenter.isOffline())
            {
                inflater.inflate(R.menu.menu_toolbar_home_manga_info, menu);
            }
            else
            {
                inflater.inflate(R.menu.menu_toolbar_downloads_manga_info, menu);
            }

            mToolbar.setNavigationIcon(R.drawable.navigation_back);
            mToolbar.setTitle(mPresenter.getTitle());
        }
        else
        {
            if (!mPresenter.isOffline())
            {
                inflater.inflate(R.menu.menu_toolbar_home_manga_info_download, menu);
            }
            else
            {
                inflater.inflate(R.menu.menu_toolbar_downloads_manga_info_remove, menu);
            }

            mToolbar.setNavigationIcon(R.drawable.ic_checkbox_blank_circle_outline_white_24dp);
            mToolbar.setTitle("Select Items");
            onDownloadViewEnabled();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menuMangaInfoRefresh:
                onRefreshInfo();
                break;
            case R.id.menuMangaInfoDownload:
                toggleDownloadingFlag();
                onDownloadViewEnabled();
                break;
            case R.id.menuDownloadsMangaInfoDownload:
                toggleDownloadingFlag();
                getActivity().invalidateOptionsMenu();
                onDownloadViewEnabled();
                break;
            case R.id.menuDownloadMangaInfoCancel:
                onDownloadCancel();
                break;
            case R.id.menuMangaInfoDownloadDownload:
                if (DownloadManager.isStoragePermissionGranted(getActivity()))
                {
                    onDownloadDownload(); // start download
                    onDownloadCancel(); // exit download view
                    MangaFeed.getInstance().makeToastShort("Starting downloads now");
                }
                else
                {
                    MangaFeed.getInstance()
                             .makeToastShort("Need storage permissions to download chapters.");
                }
                break;
            case R.id.menuDownloadsMangaInfoRemove:
                onDownloadRemove();
                break;
            case android.R.id.home:
                if (mPresenter.isDownload())
                {
                    if (mToolbar.getNavigationIcon() == mSelectFilled)
                    {
                        mToolbar.setNavigationIcon(mSelectOutline);
                        onSelectAllOrNone(false);
                    }
                    else
                    {
                        mToolbar.setNavigationIcon(mSelectFilled);
                        onSelectAllOrNone(true);
                    }
                }
                else
                {
                    getActivity().onBackPressed();
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void setBottomNavStartContinue(String readText)
    {
        mBottomNav.getMenu().findItem(R.id.menuMangaInfoBottomContinueReading).setTitle(readText);
    }

    @Override
    public void setBottomNavFollowTitle(int followType)
    {
        MenuItem item = mBottomNav.getMenu().findItem(R.id.menuMangaInfoBottomNavFollow);
        if (followType == 0)
        {
            item.setIcon(R.drawable.ic_heart_outline_white_24dp);
        }
        else
        {
            item.setIcon(R.drawable.ic_heart_white_24dp);
        }

        item.setTitle(MangaEnums.FollowType.values()[followType].toString());
    }

    @Override
    public void toggleDownloadingFlag()
    {
        mPresenter.toggleDownload();
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void registerAdapter(RecyclerView.Adapter adapter, RecyclerView.LayoutManager manager)
    {
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void startRefresh()
    {
        mSwipeLayout.setEnabled(true);
        mSwipeLayout.setRefreshing(true);
    }

    @Override
    public void stopRefresh()
    {
        mSwipeLayout.setRefreshing(false);
        mSwipeLayout.setEnabled(false);
    }

    @Override
    public String getTagText()
    {
        return TAG;
    }

    @Override
    public boolean onBackPressed()
    {
        if (mPresenter.isDownload())
        {
            onDownloadCancel();
            return false;
        }

        return true;
    }

    /***
     * This function tells the presenter the (un)select all button has been selected.
     *
     * @param isAll
     */
    public void onSelectAllOrNone(boolean isAll)
    {
        mPresenter.onSelectAllOrNone(isAll);
    }

    /***
     * This function makes the recyclerview scroll to the selected position when the download view is enabled.
     *
     */
    public void onDownloadViewStart()
    {
        int lPosition = ((MangaInfoChaptersAdapter) mRecyclerView.getAdapter()).getFirstDownloadScrollPosition();
        mRecyclerView.scrollToPosition(lPosition);
    }

    /***
     * This function tells the presenter the cancel button has been selected.
     *
     */
    public void onDownloadCancel()
    {
        mPresenter.onDownloadCancel();
        mBottomNav.setVisibility(View.VISIBLE);
    }

    /***
     * This function tells the presenter the download button has been selected.
     *
     */
    public void onDownloadDownload()
    {
        mPresenter.onDownloadDownload();
    }

    /***
     * This function tells the presenter the remove menu button has been selected.
     *
     */
    public void onDownloadRemove()
    {
        mPresenter.onDownloadRemove();
    }

    /***
     * This function tells the presenter to refresh the information view.
     *
     */
    public void onRefreshInfo()
    {
        mPresenter.onRefreshInfo();
    }

    /***
     * This function tells the presenter to enable Download view.
     *
     */
    public void onDownloadViewEnabled()
    {
        mPresenter.onDownloadViewEnabled();
        mBottomNav.setVisibility(View.GONE);
        onDownloadViewStart();
    }

    /***
     * This function sets up the bottom navigation view interactions.
     *
     */
    private void setupBottomNav()
    {
        mBottomNav.setOnNavigationItemSelectedListener((MenuItem item) ->
        {
            switch (item.getItemId())
            {
                case R.id.menuMangaInfoBottomNavFollow:
                    // pop up menu
                    try
                    {
                        PopupMenu lPopupMenu = new PopupMenu(getContext(), mBottomNav);
                        lPopupMenu.getMenuInflater()
                                  .inflate(R.menu.menu_follow_status, lPopupMenu.getMenu());

                        lPopupMenu.setOnMenuItemClickListener(popupItem ->
                        {
                            int lStatus;

                            switch (popupItem.getItemId())
                            {
                                case R.id.menuFollowStatusReading:
                                    lStatus = Manga.FOLLOW_READING;
                                    break;
                                case R.id.menuFollowStatusCompleted:
                                    lStatus = Manga.FOLLOW_COMPLETE;
                                    break;
                                case R.id.menuFollowStatusOnHold:
                                    lStatus = Manga.FOLLOW_ON_HOLD;
                                    break;
                                case R.id.menuFollowStatusPlanToRead:
                                    lStatus = Manga.FOLLOW_PLAN_TO_READ;
                                    break;
                                default:
                                    lStatus = Manga.UNFOLLOW;
                                    break;
                            }

                            mPresenter.onUpdateFollowStatus(lStatus);

                            return true;
                        });

                        lPopupMenu.show(); //showing lPopupMenu menu
                    }
                    catch (NullPointerException npe)
                    {
                        MangaLogger.logError(TAG, "Context was null", npe.getMessage());
                    }
                    return true;
                case R.id.menuMangaInfoBottomContinueReading:
                    // Start Reading from latest chapter
                    return true;
            }
            return false;
        });
    }
}
