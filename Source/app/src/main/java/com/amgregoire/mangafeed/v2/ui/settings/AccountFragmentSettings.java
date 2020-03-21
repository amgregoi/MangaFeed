package com.amgregoire.mangafeed.v2.ui.settings;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.Utils.SharedPrefs;
import com.amgregoire.mangafeed.v2.service.Logger;
import com.amgregoire.mangafeed.v2.ui.base.BaseFragment;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by amgregoi on 12/6/18.
 */

public class AccountFragmentSettings extends BaseFragment
{
    public final static String TAG = AccountFragmentSettings.class.getSimpleName();

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
//        DialogFragment fragment = new UpdateSourceDialog();
//        fragment.show(getChildFragmentManager(),"update");
//        MangaFeed.Companion.getApp().getCurrentSource().updateLocalCatalog();
//        MangaFeed.Companion.getApp().updateCatalogs(true);
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

    @OnClick(R.id.ll_light_theme)
    public void onLightTheme()
    {
        SharedPrefs.setLayoutTheme(true);
        getActivity().recreate();
    }

    @OnClick(R.id.ll_theme_dark)
    public void onDarkTheme()
    {
        SharedPrefs.setLayoutTheme(false);

        try
        {
            getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getActivity().recreate();
        }
        catch (NullPointerException ex)
        {
            Logger.INSTANCE.error(ex, "");
        }
    }
}
