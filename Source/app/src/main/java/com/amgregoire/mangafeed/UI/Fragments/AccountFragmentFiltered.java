package com.amgregoire.mangafeed.UI.Fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amgregoire.mangafeed.Common.RecyclerViewSpaceDecoration;
import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.UI.Mappers.IAccount;
import com.amgregoire.mangafeed.UI.Presenters.AccountPresFiltered;
import com.amgregoire.mangafeed.Utils.MangaLogger;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Andy Gregoire on 3/11/2018.
 */

public class AccountFragmentFiltered extends Fragment implements IAccount.AccountFilteredMap
{
    public final static String TAG = AccountFragmentFiltered.class.getSimpleName();
    public final static String FILTER_KEY = TAG + "FILTER_KEY";
    public final static String TITLE_KEY = TAG + "TITLE_KEY";

    @BindView(R.id.recyclerViewFilteredManga) RecyclerView mRecyclerView;
    @BindView(R.id.toolbarFilteredManga) Toolbar mToolbar;

    private IAccount.AccountFilteredPres mPresenter;
    private RecyclerViewSpaceDecoration mSpaceDecor;

    public static AccountFragmentFiltered newInstance(int filter, String title)
    {
        AccountFragmentFiltered lResult = new AccountFragmentFiltered();
        Bundle lBundle = new Bundle();
        lBundle.putInt(FILTER_KEY, filter);
        lBundle.putString(TITLE_KEY, title);
        lResult.setArguments(lBundle);

        return lResult;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View lView = inflater.inflate(R.layout.fragment_manga_filtered, null);
        ButterKnife.bind(this, lView);

        mPresenter = new AccountPresFiltered(this);
        mPresenter.init(getArguments());

        return lView;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mPresenter.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mPresenter.onPause();
    }

    @Override
    public void initViews()
    {
        setupToolbar();
    }

    @Override
    public void registerAdapter(RecyclerView.Adapter adapter, RecyclerView.LayoutManager manager)
    {
        if (mSpaceDecor == null)
        {
            mSpaceDecor = new RecyclerViewSpaceDecoration(20);
            mRecyclerView.addItemDecoration(mSpaceDecor);
            mRecyclerView.getItemAnimator().setChangeDuration(0);
        }
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(adapter);
    }

    /***
     * This function sets up the fragments toolbar.
     *
     */
    private void setupToolbar()
    {
        mToolbar.setTitle(getArguments().getString(TITLE_KEY));
        mToolbar.setNavigationOnClickListener(view ->
        {
            FragmentManager manager = getFragmentManager();
            if (manager != null)
            {
                manager.popBackStack();
            }
            else
            {
                MangaLogger.logError(TAG, "There was an issue with back button for filter fragment");
            }
        });
    }
}
