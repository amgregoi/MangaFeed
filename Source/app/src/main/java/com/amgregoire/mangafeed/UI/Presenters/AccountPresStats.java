package com.amgregoire.mangafeed.UI.Presenters;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.amgregoire.mangafeed.Adapters.AccountViewPagerAdapter;
import com.amgregoire.mangafeed.UI.Mappers.IAccount;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public class AccountPresStats implements IAccount.AccountPres
{
    private IAccount.AccountMap mMap;
    private AccountViewPagerAdapter mAdapter;
    private FragmentManager mManager;

    public AccountPresStats(IAccount.AccountMap map, FragmentManager manager)
    {
        mMap = map;
        mManager = manager;
    }

    @Override
    public void init(Bundle bundle)
    {

        mMap.initViews();

        if (mAdapter == null)
        {
            mAdapter = new AccountViewPagerAdapter(mManager, 2);
        }

        mMap.registerAdapter(mAdapter);
    }
}
