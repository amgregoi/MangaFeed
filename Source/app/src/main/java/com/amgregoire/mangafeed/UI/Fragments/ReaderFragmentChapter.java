package com.amgregoire.mangafeed.UI.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.Manga;
import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.UI.Activities.NavigationActivity;
import com.amgregoire.mangafeed.UI.Mappers.IReader;
import com.amgregoire.mangafeed.UI.Presenters.ReaderPresChapter;
import com.amgregoire.mangafeed.UI.Widgets.GestureViewPager;
import com.amgregoire.mangafeed.Utils.BusEvents.ReaderChapterChangeEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.ReaderSingleTapEvent;
import com.amgregoire.mangafeed.Utils.MangaLogger;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Andy Gregoire on 3/21/2018.
 */

public class ReaderFragmentChapter extends Fragment implements IReader.ReaderMapChapter
{
    public final static String TAG = ReaderFragmentChapter.class.getSimpleName();
    public final static String FOLLOWING_KEY = TAG + "FOLLOWING";
    public final static String POSITION_KEY = TAG + "POSITION";
    public final static String MANGA_KEY = TAG + "MANGA";


    @BindView(R.id.viewPagerReaderChapter) GestureViewPager mViewPager;

    private IReader.ReaderPresChapter mPresenter;

    public static Fragment newInstance(boolean isFollowing, int position, Manga manga)
    {
        Bundle lBundle = new Bundle();
        lBundle.putBoolean(FOLLOWING_KEY, isFollowing);
        lBundle.putInt(POSITION_KEY, position);
        lBundle.putParcelable(MANGA_KEY, manga);

        Fragment lFragment = new ReaderFragmentChapter();
        lFragment.setArguments(lBundle);

        return lFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View lView = inflater.inflate(R.layout.item_fragment_reader_chapter, null);
        ButterKnife.bind(this, lView);

        mPresenter = new ReaderPresChapter(this);
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
    public void registerAdapter(PagerAdapter adapter)
    {
        mViewPager.setAdapter(adapter);
        mViewPager.setPageMargin(128);
        mViewPager.setOffscreenPageLimit(6);
    }

    @Override
    public void initViews()
    {
        mViewPager.setUserGesureListener(this);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
                // do nothing
            }

            @Override
            public void onPageSelected(int position)
            {
                mPresenter.updateCurrentPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {
                // do nothing
            }
        });
    }

    @Override
    public void onSingleTap()
    {
        MangaFeed.getInstance().rxBus().send(new ReaderSingleTapEvent());
    }

    @Override
    public void onLeft()
    {
        MangaFeed.getInstance().rxBus().send(new ReaderChapterChangeEvent(false));
    }

    @Override
    public void onRight()
    {
        MangaFeed.getInstance().rxBus().send(new ReaderChapterChangeEvent(true));
    }

    @Override
    public void onNextPage()
    {
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
    }

    @Override
    public void onPrevPage()
    {
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
    }

    public void update()
    {
        if (mPresenter != null)
        {
            mPresenter.setNewChapterToolbar();
        }
    }
}
