package com.amgregoire.mangafeed.UI.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.UI.Mappers.IAccount;
import com.amgregoire.mangafeed.UI.Presenters.AccountPres;
import com.amgregoire.mangafeed.Utils.LoginManager;
import com.amgregoire.mangafeed.Utils.SharedPrefs;
import com.amgregoire.mangafeed.v2.ui.map.ToolbarMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public class AccountFragment extends Fragment implements IAccount.AccountMap
{
    public final static String TAG = AccountFragment.class.getSimpleName();
    public final static int MENU_RESOURCE = R.menu.menu_toolbar_account;

    @BindView(R.id.scrollViewAccountRoot) NestedScrollView mScroll;
    @BindView(R.id.tabLayoutAccount) TabLayout mTabLayout;
    @BindView(R.id.viewPagerAccount) ViewPager mViewPager;

    @BindView(R.id.textViewUserName) TextView mUserName;
    @BindView(R.id.textViewSignInOut) TextView mSignInOut;

    private IAccount.AccountPres mPresenter;

    public static AccountFragment newInstance()
    {
        return new AccountFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View lView = inflater.inflate(R.layout.fragment_account, null);
        ButterKnife.bind(this, lView);

        mPresenter = new AccountPres(this, getChildFragmentManager());
        mPresenter.init(getArguments());

        return lView;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mPresenter.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mPresenter.onResume();
    }

    @Override
    public void initViews()
    {
        setupTabLayout();
        mScroll.setFillViewport(true);
        setHeaderUserName();
    }

    @Override
    public void registerAdapter(PagerAdapter adapter)
    {
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mViewPager.setOffscreenPageLimit(2);
    }

    @OnClick({R.id.linearLayoutUserContainer, R.id.imageViewAccountProfile})
    public void onAttemptLogin()
    {
        LoginManager.interact(getContext());
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser)
    {
        if(isVisibleToUser)
        {
            ((ToolbarMap)getActivity()).setOptionsMenu(R.menu.menu_toolbar_account);
        }
    }

    @Override
    public void setHeaderUserName()
    {
        String lUserName = SharedPrefs.getUserName();
        mUserName.setText(lUserName == null ? "Guest" : lUserName);
        mSignInOut.setText(lUserName == null ? "Sign in" : "Sign out");
    }

    /***
     * This function sets up the tab layout.
     *
     */
    private void setupTabLayout()
    {
        mTabLayout.addTab(mTabLayout.newTab().setText("Sources"));
        mTabLayout.addTab(mTabLayout.newTab().setText("Stats"));

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                mViewPager.setCurrentItem(tab.getPosition(), false);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {
                // do nothing
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {
                // do nothing
            }
        });
    }
}
