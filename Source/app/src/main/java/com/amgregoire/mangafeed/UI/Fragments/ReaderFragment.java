package com.amgregoire.mangafeed.UI.Fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.Manga;
import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.UI.BackHandledFragment;
import com.amgregoire.mangafeed.UI.Mappers.IReader;
import com.amgregoire.mangafeed.UI.Presenters.ReaderPres;
import com.amgregoire.mangafeed.UI.Services.ToolbarTimerService;
import com.amgregoire.mangafeed.UI.Widgets.NoScrollViewPager;
import com.amgregoire.mangafeed.Utils.BusEvents.ReaderPageChangeEvent;
import com.amgregoire.mangafeed.Utils.MangaLogger;

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

    @BindView(R.id.relativeLayoutChapterHeader) RelativeLayout mReaderHeader;
    @BindView(R.id.relativeLayoutChapterFooter) RelativeLayout mReaderFooter;

    private IReader.ReaderPres mPresenter;

    private ToolbarTimerService mToolBarService;
    private ServiceConnection mConnection;

    public static Fragment newInstance(Manga manga, int position)
    {
        Bundle lBundle = new Bundle();
        lBundle.putParcelable(MANGA_KEY, manga);
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
        setRetainInstance(true);
        setupToolbar();

        ViewGroup.MarginLayoutParams lToolbarParams = (ViewGroup.MarginLayoutParams) mToolbar.getLayoutParams();
        lToolbarParams.topMargin = getStatusBarHeight();

        ViewGroup.MarginLayoutParams lFooterParams = (ViewGroup.MarginLayoutParams) mReaderFooter.getLayoutParams();
        lFooterParams.bottomMargin = getNavBarHeight();

        setupViewPager();
        setupToolbarService();
    }

    /***
     * This function hides the status bar, toolbar, and reader header/footers
     *
     */
    public void hideToolbar()
    {

        mToolbar.animate()
                .translationY(-mToolbar.getHeight() - getStatusBarHeight())
                .setInterpolator(new AccelerateInterpolator())
                .start();

        mReaderHeader.animate()
                     .translationY(-mReaderHeader.getHeight() - mToolbar.getHeight())
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
            getActivity().getWindow().getDecorView()
                         .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    /***
     * This function makes the status bar, toolbar, and reader header/footers visible
     *
     */
    public void showToolbar()
    {
        mToolbar.animate()
                .translationY(mToolbar.getScrollY())
                .setInterpolator(new AccelerateInterpolator())
                .start();

        mReaderHeader.animate()
                     .translationY(mReaderHeader.getScrollY() + mToolbar.getScrollY())
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
            mToolbar.setNavigationIcon(R.drawable.navigation_back);
            mToolbar.setNavigationOnClickListener(view -> getActivity().onBackPressed());
        }
    }

    @Override
    public void registerAdapter(PagerAdapter adapter)
    {
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(1);
        mToolBarService.startToolBarTimer();
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

        mToolBarService.startToolBarTimer();
    }

    @Override
    public void onNextChapter()
    {
        showToolbar();
        mViewPager.incrementCurrentItem();
        mToolBarService.startToolBarTimer();
    }

    @Override
    public void onPrevChapter()
    {
        showToolbar();
        mViewPager.decrememntCurrentItem();
        mToolBarService.startToolBarTimer();
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

    @OnClick(R.id.fabReaderPreviousPage)
    public void onFABPrevPage()
    {
        MangaFeed.getInstance().rxBus().send(new ReaderPageChangeEvent(false));
        mToolBarService.startToolBarTimer();
    }

    @OnClick(R.id.fabReaderNextPage)
    public void onFABNextPage()
    {
        MangaFeed.getInstance().rxBus().send(new ReaderPageChangeEvent(true));
        mToolBarService.startToolBarTimer();
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
        MangaFeed.getInstance().makeToastShort("NOT IMPLEMENTED");
//        mPresenter.refresh();
//        mToolBarService.stopTimer();
    }

    @OnClick(R.id.imageViewReaderScreenOrientationToggle)
    public void onScreenRotateClicked()
    {
        MangaFeed.getInstance().makeToastShort("NOT IMPLEMENTED");
        mToolBarService.startToolBarTimer();
    }

    @OnClick(R.id.imageViewReaderVerticalScrollToggle)
    public void onVerticalScrollClicked()
    {
        MangaFeed.getInstance().makeToastShort("NOT IMPLEMENTED");
        mToolBarService.startToolBarTimer();
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
}
