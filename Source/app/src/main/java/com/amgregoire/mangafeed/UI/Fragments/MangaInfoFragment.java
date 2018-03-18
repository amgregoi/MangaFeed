package com.amgregoire.mangafeed.UI.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.amgregoire.mangafeed.Common.MangaEnums;
import com.amgregoire.mangafeed.Models.Manga;
import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.UI.Adapters.MangaInfoChaptersAdapter;
import com.amgregoire.mangafeed.UI.Mappers.IManga;
import com.amgregoire.mangafeed.UI.Presenters.MangaInfoPres;
import com.amgregoire.mangafeed.Utils.MangaLogger;
import com.l4digital.fastscroll.FastScrollRecyclerView;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Andy Gregoire on 3/12/2018.
 */

public class MangaInfoFragment extends Fragment implements IManga.MangaMap
{
    public final static String TAG = MangaInfoFragment.class.getSimpleName();
    public final static String MANGA_KEY = TAG + "MANGA";

    @BindView(R.id.recyclerViewMangaInfo) FastScrollRecyclerView mRecyclerView;
    @BindView(R.id.bottomNavigationMangaInfo) BottomNavigationView mBottomNav;
    @BindView(R.id.swipeRefreshLayoutMangaInfo) SwipeRefreshLayout mSwipeLayout;

    @BindColor(R.color.manga_blue) int mColorBlue;
    @BindColor(R.color.manga_red) int mColorRed;
    @BindColor(R.color.manga_green) int mColorGreen;
    @BindColor(R.color.manga_gray) int mColorGray;
    @BindColor(R.color.manga_white) int mColorWhite;

    private IManga.MangaPres mPresenter;

    public static Fragment newInstance(Manga manga)
    {
        Bundle lBundle = new Bundle();
        lBundle.putParcelable(MANGA_KEY, manga);

        Fragment lFragment = new MangaInfoFragment();
        lFragment.setArguments(lBundle);

        return lFragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View lView = inflater.inflate(R.layout.fragment_manga_info, null);
        ButterKnife.bind(this, lView);

        mPresenter = new MangaInfoPres(this);
        mPresenter.init(getArguments());

        return lView;
    }

    @Override
    public void initViews()
    {
        setupBottomNav();
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
