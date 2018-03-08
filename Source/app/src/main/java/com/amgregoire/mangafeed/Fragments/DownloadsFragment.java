package com.amgregoire.mangafeed.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amgregoire.mangafeed.R;

import butterknife.ButterKnife;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public class DownloadsFragment extends Fragment
{
    public final static String TAG = DownloadsFragment.class.getSimpleName();

    public static DownloadsFragment newInstance()
    {
        return new DownloadsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View lView = inflater.inflate(R.layout.frament_offline, null);
        ButterKnife.bind(this, lView);

        if (savedInstanceState != null)
        {
            // restore state
        }

        //init

        return lView;
    }

}
