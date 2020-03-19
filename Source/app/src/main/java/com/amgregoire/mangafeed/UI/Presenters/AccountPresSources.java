package com.amgregoire.mangafeed.UI.Presenters;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amgregoire.mangafeed.UI.Adapters.SourceRecycleAdapter;
import com.amgregoire.mangafeed.Common.MangaEnums;
import com.amgregoire.mangafeed.UI.Mappers.IAccount;
import com.amgregoire.mangafeed.Utils.MangaLogger;

import java.util.Arrays;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public class AccountPresSources implements IAccount.AccountSourcePres
{
    public final static String TAG = AccountPresSources.class.getSimpleName();

    private IAccount.AccountSourceMap mMap;
    private SourceRecycleAdapter mAdapter;
    private RecyclerView.LayoutManager mManager;

    public AccountPresSources(IAccount.AccountSourceMap map)
    {
        mMap = map;
    }

    @Override
    public void init(Bundle bundle)
    {
        try
        {
            mMap.initViews();

            if (mAdapter == null)
            {
                mManager = new LinearLayoutManager(mMap.getContext());
                mAdapter = new SourceRecycleAdapter(Arrays.asList(MangaEnums.Source.values()));
            }

            mMap.registerAdapter(mAdapter, mManager);
        }
        catch (Exception ex)
        {
            MangaLogger.logError(TAG, ex.getMessage());
        }
    }
}
