package com.amgregoire.mangafeed.UI.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.UI.Presenters.HomePresLibrary;

import butterknife.ButterKnife;

public class HomeFragmentLibrary extends HomeFragmentsBase
{
    public final static String TAG = HomeFragmentLibrary.class.getSimpleName();

    /***
     * This function creates and returns a new instance of the LibraryFragment.
     *
     * @return
     */
    public static HomeFragmentLibrary newInstance()
    {
        return new HomeFragmentLibrary();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View lView = inflater.inflate(R.layout.item_fragment_home_view_pager, container, false);
        ButterKnife.bind(this, lView);

        mPresenter = new HomePresLibrary(this);
        mPresenter.init(getArguments());

        return lView;
    }
}