package com.amgregoire.mangafeed.UI.Presenters;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.amgregoire.mangafeed.Adapters.SourceRecycleAdapter;
import com.amgregoire.mangafeed.Common.MangaEnums;
import com.amgregoire.mangafeed.UI.Mappers.IAccount;

import java.util.Arrays;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public class AccountPresSources implements IAccount.AccountSourcePres
{
    private IAccount.AccountSourceMap mMap;
    private SourceRecycleAdapter mAdapter;
    private RecyclerView.LayoutManager mManager;
    private boolean mNeedsDecor = true;

    public AccountPresSources(IAccount.AccountSourceMap map)
    {
        mMap = map;
    }

    @Override
    public void init(Bundle bundle)
    {

        mMap.initViews();

        if (mAdapter == null)
        {
            mManager = new LinearLayoutManager(mMap.getContext());
            mAdapter = new SourceRecycleAdapter(Arrays.asList(MangaEnums.Source.values()));
        }

        mMap.registerAdapter(mAdapter,mManager, mNeedsDecor);
        mNeedsDecor = false;
    }
}
