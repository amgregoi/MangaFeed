package com.amgregoire.mangafeed.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amgregoire.mangafeed.R;

import butterknife.ButterKnife;

public class RecentFragment extends Fragment
{
    public final static String TAG = RecentFragment.class.getSimpleName();

    /***
     * This function creates and returns a new instance of the CatalotFragment.
     *
     * @return
     */
    public static CatalogFragment newInstance()
    {
        return new CatalogFragment();
    }

    @Override
    public View onCreateView(LayoutInflater aInflater, @Nullable ViewGroup aContainer, @Nullable Bundle aSavedInstanceState)
    {
        View lView = aInflater.inflate(R.layout.home_pager_item, aContainer, false);
        ButterKnife.bind(this, lView);

        return lView;
    }
}
