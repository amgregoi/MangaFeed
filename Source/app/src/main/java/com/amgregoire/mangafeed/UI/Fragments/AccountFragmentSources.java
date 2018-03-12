package com.amgregoire.mangafeed.UI.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amgregoire.mangafeed.Common.RecyclerViewSpaceDecoration;
import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.UI.Mappers.IAccount;
import com.amgregoire.mangafeed.UI.Presenters.AccountPresSources;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public class AccountFragmentSources extends Fragment implements IAccount.AccountSourceMap
{
    public final static String TAG = AccountFragmentSources.class.getSimpleName();

    @BindView(R.id.recyclerViewAccountSource) RecyclerView mRecyclerView;

    private IAccount.AccountSourcePres mPresenter;
    private RecyclerViewSpaceDecoration mSpaceDecor;

    /***
     * This function creates and returns a new instance of the OfflineFragment.
     *
     * @return
     */
    public static AccountFragmentSources newInstance()
    {
        return new AccountFragmentSources();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View lView = inflater.inflate(R.layout.fragment_account_sources, container, false);
        ButterKnife.bind(this, lView);

        mPresenter = new AccountPresSources(this);
        mPresenter.init(getArguments());

        return lView;
    }

    @Override
    public void initViews()
    {

    }

    @Override
    public void registerAdapter(RecyclerView.Adapter adapter, RecyclerView.LayoutManager manager)
    {
        if(mSpaceDecor == null)
        {
            mSpaceDecor = new RecyclerViewSpaceDecoration(10);
        }
        else
        {
            mRecyclerView.invalidateItemDecorations();
        }

        mRecyclerView.addItemDecoration(mSpaceDecor);
        mRecyclerView.getItemAnimator().setChangeDuration(0);

        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(adapter);
    }
}
