package com.amgregoire.mangafeed.UI.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amgregoire.mangafeed.UI.Presenters.HomePresBase;
import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.UI.Presenters.HomePresCatalog;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeFragmentCatalog extends HomeFragmentsBase
{
    public final static String TAG = HomeFragmentCatalog.class.getSimpleName();

    @BindView(R.id.testTextView) TextView mTestText;

    /***
     * This function creates and returns a new instance of the CatalotFragment.
     *
     * @return
     */
    public static HomeFragmentCatalog newInstance()
    {
        return new HomeFragmentCatalog();
    }

    @Override
    public View onCreateView(LayoutInflater aInflater, @Nullable ViewGroup aContainer, @Nullable Bundle aSavedInstanceState)
    {
        View lView = aInflater.inflate(R.layout.fragment_home_pager_item, aContainer, false);
        ButterKnife.bind(this, lView);

        mPresenter = new HomePresCatalog(this);
        mPresenter.init(getArguments());
        mTestText.setText("Catalog.. OH BABY");

        return lView;
    }

}