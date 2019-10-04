package com.amgregoire.mangafeed.UI.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by amgregoi on 12/6/18.
 */

public class AccountFragmentSettings extends Fragment
{
    public final static String  TAG = AccountFragmentSettings.class.getSimpleName();

    public final static Fragment newInstance()
    {
        return new AccountFragmentSettings();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View lView = inflater.inflate(R.layout.fragment_account_settings, null, false);
        ButterKnife.bind(this, lView);

        return lView;
    }

    @OnClick(R.id.force_update_sources)
    public void onForceUpdateSources()
    {
        MangaFeed.Companion.getApp().updateCatalogs(true);
    }

    @OnClick(R.id.reset_followed_manga_pref)
    public void onResetLibrary()
    {
        Toast.makeText(getContext(), "NOT IMPLEMENTED", Toast.LENGTH_SHORT);
    }

    @OnClick(R.id.reset_cached_chapters)
    public void onResetReadChapters()
    {
        Toast.makeText(getContext(), "NOT IMPLEMENTED", Toast.LENGTH_SHORT);
    }

    @OnClick(R.id.reset_downloaded_chapters)
    public void onRemoveDownloadedChapters()
    {
        Toast.makeText(getContext(), "NOT IMPLEMENTED", Toast.LENGTH_SHORT);
    }
}
