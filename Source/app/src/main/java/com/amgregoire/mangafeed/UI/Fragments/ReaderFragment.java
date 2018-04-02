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

import com.amgregoire.mangafeed.Models.Manga;
import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.UI.Mappers.IReader;
import com.amgregoire.mangafeed.UI.Presenters.ReaderPres;
import com.amgregoire.mangafeed.UI.Widgets.NoScrollViewPager;
import com.amgregoire.mangafeed.Utils.MangaLogger;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Andy Gregoire on 3/21/2018.
 */

public class ReaderFragment extends Fragment implements IReader.ReaderMap
{
    public final static String TAG = ReaderFragment.class.getSimpleName();
    public final static String MANGA_KEY = TAG + "MANGA";
    public final static String POSITION_KEY = TAG + "POSITION";

    @BindView(R.id.noScrollViewPagerReader) NoScrollViewPager mViewPager;

    private IReader.ReaderPres mPresenter;

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
    public void registerAdapter(PagerAdapter adapter)
    {
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(0);
    }

    @Override
    public void initViews()
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
//                mPresenter.updateToolbar(aPosition);
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {
                // Do nothing
            }
        });
    }

    public void incrementChapter()
    {
        MangaLogger.logError(TAG, "pos: " + mViewPager.getCurrentItem());
        mViewPager.incrementCurrentItem();
        MangaLogger.logError(TAG, "pos: " + mViewPager.getCurrentItem());
    }

    public void decrementChapter()
    {
        MangaLogger.logError(TAG, "pos: " + mViewPager.getCurrentItem());
        mViewPager.decrememntCurrentItem();
        MangaLogger.logError(TAG, "pos: " + mViewPager.getCurrentItem());
    }

    @Override
    public void setPagerPosition(int position)
    {
        mViewPager.setCurrentItem(position);
    }
}
