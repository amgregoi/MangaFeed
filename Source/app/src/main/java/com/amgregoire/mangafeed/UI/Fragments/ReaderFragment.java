package com.amgregoire.mangafeed.UI.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import com.amgregoire.mangafeed.Models.Manga;
import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.UI.Activities.NavigationActivity;
import com.amgregoire.mangafeed.UI.BackHandledFragment;
import com.amgregoire.mangafeed.UI.Mappers.IReader;
import com.amgregoire.mangafeed.UI.Presenters.ReaderPres;
import com.amgregoire.mangafeed.UI.Widgets.NoScrollViewPager;
import com.amgregoire.mangafeed.Utils.MangaLogger;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Andy Gregoire on 3/21/2018.
 */

public class ReaderFragment extends BackHandledFragment implements IReader.ReaderMap
{
    public final static String TAG = ReaderFragment.class.getSimpleName();
    public final static String MANGA_KEY = TAG + "MANGA";
    public final static String POSITION_KEY = TAG + "POSITION";

    @BindView(R.id.noScrollViewPagerReader) NoScrollViewPager mViewPager;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.textViewReaderChapterTitle) TextView mChapterTitle;

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
    public void initViews()
    {
        setRetainInstance(true);
        setupToolbar();

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
    }

    public void incrementChapter()
    {
        mViewPager.incrementCurrentItem();
    }

    public void decrementChapter()
    {
        mViewPager.decrememntCurrentItem();
    }

    @Override
    public void setPagerPosition(int position)
    {
        mViewPager.setCurrentItem(position);
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
}
