package com.amgregoire.mangafeed.UI.Fragments;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amgregoire.mangafeed.R;

import butterknife.ButterKnife;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public class OfflineFragment extends Fragment
{
    public final static String TAG = OfflineFragment.class.getSimpleName();

    /***
     * This function creates and returns a new instance of the OfflineFragment.
     *
     * @return
     */
    public static OfflineFragment newInstance()
    {
        return new OfflineFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View lView = inflater.inflate(R.layout.fragment_offline, container, false);
        ButterKnife.bind(this, lView);

        return lView;
    }

}
