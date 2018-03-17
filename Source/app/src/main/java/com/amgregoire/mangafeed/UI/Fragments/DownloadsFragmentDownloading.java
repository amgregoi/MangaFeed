package com.amgregoire.mangafeed.UI.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amgregoire.mangafeed.R;

import butterknife.ButterKnife;

public class DownloadsFragmentDownloading extends Fragment
{
    public final static String TAG = DownloadsFragmentDownloading.class.getSimpleName();

    /***
     * This function creates and returns a new instance of the RecentFragment.
     *
     * @return
     */
    public static DownloadsFragmentDownloading newInstance()
    {
        return new DownloadsFragmentDownloading();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View lView = inflater.inflate(R.layout.fragment_downloads_downloading, container, false);
        ButterKnife.bind(this, lView);

        return lView;
    }

}
