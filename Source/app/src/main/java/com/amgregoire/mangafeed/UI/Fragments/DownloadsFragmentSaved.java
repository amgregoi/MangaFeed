package com.amgregoire.mangafeed.UI.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amgregoire.mangafeed.R;

import butterknife.ButterKnife;

public class DownloadsFragmentSaved extends Fragment
{
    public final static String TAG = DownloadsFragmentSaved.class.getSimpleName();

    /***
     * This function creates and returns a new instance of the RecentFragment.
     *
     * @return
     */
    public static DownloadsFragmentSaved newInstance()
    {
        return new DownloadsFragmentSaved();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View lView = inflater.inflate(R.layout.fragment_downloads_saved, container, false);
        ButterKnife.bind(this, lView);

        return lView;
    }

}
