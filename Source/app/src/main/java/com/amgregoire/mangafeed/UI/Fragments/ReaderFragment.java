package com.amgregoire.mangafeed.UI.Fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amgregoire.mangafeed.Common.MangaEnums;
import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.DbManga;
import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.UI.BackHandledFragment;
import com.amgregoire.mangafeed.UI.Mappers.IReader;
import com.amgregoire.mangafeed.UI.Presenters.ReaderPres;
import com.amgregoire.mangafeed.UI.Services.ToolbarTimerService;
import com.amgregoire.mangafeed.UI.Widgets.NoScrollViewPager;
import com.amgregoire.mangafeed.Utils.BusEvents.ReaderPageChangeEvent;
import com.amgregoire.mangafeed.Utils.MangaLogger;
import com.github.clans.fab.FloatingActionButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Andy Gregoire on 3/21/2018.
 */

public class ReaderFragment extends BackHandledFragment implements IReader.ReaderMap, ToolbarTimerService.ReaderTimerListener
{
    public final static String TAG = ReaderFragment.class.getSimpleName();
    public final static String MANGA_KEY = TAG + "MANGA";
    public final static String POSITION_KEY = TAG + "POSITION";

    @BindView(R.id.noScrollViewPagerReader) NoScrollViewPager mViewPager;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.textViewReaderChapterTitle) TextView mChapterTitle;
    @BindView(R.id.textViewReaderCurrentPage) TextView mCurrentPage;
    @BindView(R.id.textViewReaderTotalPages) TextView mTotalPages;

    @BindView(R.id.topContainer) LinearLayout mToolbarContainer;

    @BindView(R.id.relativeLayoutChapterHeader) RelativeLayout mReaderHeader;
    @BindView(R.id.relativeLayoutChapterFooter) RelativeLayout mReaderFooter;

    @BindView(R.id.fabReaderNextPage) FloatingActionButton mFABNextPage;
    @BindView(R.id.fabReaderPreviousPage) FloatingActionButton mFABPrevPage;

    private IReader.ReaderPres mPresenter;

    private ToolbarTimerService mToolBarService;
    private ServiceConnection mConnection;

    public static Fragment newInstance(DbManga dbManga, int position)
    {
        Bundle lBundle = new Bundle();
        lBundle.putParcelable(MANGA_KEY, dbManga);
        lBundle.putInt(POSITION_KEY, position);

        Fragment lFragment = new ReaderFragment();
        lFragment.setArguments(lBundle);

        return lFragment;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View lView = inflater.inflate(R.layout.fragment_reader, null);
        ButterKnife.bind(this, lView);

        mPresenter = new ReaderPres(this);
        mPresenter.init(getArguments());

        return lView;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mPresenter.onResume();

        Window w = getActivity().getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mPresenter.onPause();

        Window w = getActivity().getWindow();
        showToolbar();
        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        getActivity().unbindService(mConnection);
    }

    @Override
    public void initViews()
    {
        setupToolbar();

//        ViewGroup.MarginLayoutParams lToolbarParams = (ViewGroup.MarginLayoutParams) mToolbar.getLayoutParams();
//        lToolbarParams.topMargin = getStatusBarHeight();
//
//        ViewGroup.MarginLayoutParams lFooterParams = (ViewGroup.MarginLayoutParams) mReaderFooter.getLayoutParams();
//        lFooterParams.bottomMargin = getNavBarHeight();

        if (MangaFeed.Companion.getApp().getCurrentSourceType() == MangaEnums.SourceType.NOVEL)
        {
            mFABNextPage.setVisibility(View.GONE);
            mFABPrevPage.setVisibility(View.GONE);
        }

        setupViewPager();
        setupToolbarService();

        mToolbarContainer.setPadding(0, getStatusBarHeight(), 0, 0);
        mReaderFooter.setPadding(0, 0, 0, getNavBarHeight());
    }

    /***
     * This function hides the status bar, toolbar, and reader header/footers
     *
     */
    public void hideToolbar()
    {

        mToolbarContainer.animate()
                         .translationY(-mToolbarContainer.getHeight() - getStatusBarHeight())
                         .setInterpolator(new AccelerateInterpolator())
                         .start();

        mReaderFooter.animate()
                     .translationY(mReaderFooter.getHeight() + getNavBarHeight())
                     .setInterpolator(new AccelerateInterpolator())
                     .start();

        getActivity().getWindow().getDecorView()
                     .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

    }

    @Override
    public void hideSystemUi()
    {
        if (getActivity().getWindow().getDecorView().getSystemUiVisibility() == 0)
        {
            getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    /***
     * This function makes the status bar, toolbar, and reader header/footers visible
     *
     */
    public void showToolbar()
    {
        mToolbarContainer.animate()
                         .translationY(mToolbarContainer.getScrollY())
                         .setInterpolator(new AccelerateInterpolator())
                         .start();

        mReaderFooter.animate()
                     .translationY(-mReaderFooter.getScrollY())
                     .setInterpolator(new AccelerateInterpolator())
                     .start();

        getActivity().getWindow().getDecorView().setSystemUiVisibility(0);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.menu_toolbar_reader, menu);
    }

    private void setupToolbar()
    {
        if (getActivity() != null)
        {
            mToolbar.setTitle(mPresenter.getMangaTitle());
            mToolbar.setNavigationIcon(R.drawable.nav_back_white);
            mToolbar.setNavigationOnClickListener(view -> getActivity().onBackPressed());
        }
    }

    @Override
    public void registerAdapter(PagerAdapter adapter)
    {
        if(MangaFeed.Companion.getApp().getCurrentSourceType().equals(MangaEnums.SourceType.NOVEL))
        {
            mViewPager.setPagingEnabled(true);
        }
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(1);
    }

    @Override
    public void setPagerPosition(int position)
    {
        mViewPager.setCurrentItem(position);
    }

    @Override
    public void onSingleTap()
    {
        if (getActivity().getWindow().getDecorView().getSystemUiVisibility() == 0)
        {
            hideToolbar();
        }
        else
        {
            showToolbar();
        }

        startToolbarTimer();
    }

    @Override
    public void onNextChapter()
    {
        showToolbar();
        mViewPager.incrementCurrentItem();
        stopToolbarTimer();
    }

    @Override
    public void onPrevChapter()
    {
        showToolbar();
        mViewPager.decrementCurrentItem();
        stopToolbarTimer();
    }

    @Override
    public void updateToolbars(String message, String page, String total, int position)
    {
        if (position == mViewPager.getCurrentItem())
        {
            mChapterTitle.setText(message);
            mCurrentPage.setText(page);
            mTotalPages.setText(total);
        }
    }

    @Override
    public String getTagText()
    {
        return TAG;
    }

    @Override
    public boolean onBackPressed()
    {
        return true;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
        mPresenter.onSaveState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState)
    {
        super.onViewStateRestored(savedInstanceState);
        mPresenter.onRestoreState(savedInstanceState);
    }

    @Override
    public void stopToolbarTimer(int chPosition)
    {
        if (chPosition == mViewPager.getCurrentItem())
        {
            mToolBarService.stopTimer();
        }
    }

    @Override
    public void startToolbarTimer(int chPosition)
    {
        if (chPosition == mViewPager.getCurrentItem())
        {
            mToolBarService.startTimer();
        }
    }

    @OnClick(R.id.fabReaderPreviousPage)
    public void onFABPrevPage()
    {
        MangaFeed.Companion.getApp().rxBus().send(new ReaderPageChangeEvent(false));
        startToolbarTimer();
    }

    @OnClick(R.id.fabReaderNextPage)
    public void onFABNextPage()
    {
        MangaFeed.Companion.getApp().rxBus().send(new ReaderPageChangeEvent(true));
        startToolbarTimer();
    }

    @OnClick(R.id.fabReaderPreviousChapter)
    public void onFABPrevChapter()
    {
        onPrevChapter();
    }

    @OnClick(R.id.fabReaderNextChapter)
    public void onFABNextChapter()
    {
        onNextChapter();
    }

    @OnClick(R.id.imageViewReaderRefresh)
    public void onRefreshClicked()
    {
        MangaFeed.Companion.getApp().makeToastShort("NOT IMPLEMENTED");
//        mPresenter.refresh();
//        stopToolbarTimer();
    }

    @OnClick(R.id.imageViewReaderScreenOrientationToggle)
    public void onScreenRotateClicked()
    {
        MangaFeed.Companion.getApp().makeToastShort("NOT IMPLEMENTED");
        startToolbarTimer();
    }

    @OnClick(R.id.imageViewReaderVerticalScrollToggle)
    public void onVerticalScrollClicked()
    {
        MangaFeed.Companion.getApp().makeToastShort("NOT IMPLEMENTED");
        startToolbarTimer();
    }

    /***
     * This function retrieves the height of the android status bar.
     *
     * @return
     */
    private int getStatusBarHeight()
    {
        int lResult = 0;
        int lResourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (lResourceId > 0)
        {
            lResult = getResources().getDimensionPixelSize(lResourceId);
        }

        return lResult;
    }

    /***
     * This function retrieves the height of the android onscreen bottom navigation bar.
     *
     * @return
     */
    private int getNavBarHeight()
    {
        int lResult = 0;
        int lResourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (lResourceId > 0)
        {
            lResult = getResources().getDimensionPixelSize(lResourceId);
        }

        return lResult;
    }

    /***
     * This function sets up the activity viewpager.
     *
     */
    private void setupViewPager()
    {
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
                // Do nothing
            }

            @Override
            public void onPageSelected(int position)
            {
                mPresenter.updateCurrentPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {
                // Do nothing
            }
        });
    }

    /***
     * This function sets up the toolbar service, that hides the toolbar, header, footer, status bar, and nav bar
     * after a set period of time after it has been shown.
     *
     */
    private void setupToolbarService()
    {
        mConnection = new ServiceConnection()
        {

            @Override
            public void onServiceConnected(ComponentName className, IBinder service)
            {
                // We've bound to ToolbarTimerService, cast the IBinder and get ToolbarTimerService instance
                ToolbarTimerService.LocalBinder binder = (ToolbarTimerService.LocalBinder) service;
                mToolBarService = binder.getService();
                mToolBarService.setToolbarListener(ReaderFragment.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName aComponent)
            {
                MangaLogger.logInfo(TAG, aComponent.flattenToShortString() + " service disconnected.");
            }
        };

        mToolBarService = new ToolbarTimerService();
        mToolBarService.setToolbarListener(this);

        Intent intent = new Intent(getActivity(), ToolbarTimerService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void startToolbarTimer()
    {
        startToolbarTimer(mViewPager.getCurrentItem());
    }

    private void stopToolbarTimer()
    {
        stopToolbarTimer(mViewPager.getCurrentItem());
    }
}
