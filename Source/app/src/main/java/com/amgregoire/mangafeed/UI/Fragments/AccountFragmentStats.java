package com.amgregoire.mangafeed.UI.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amgregoire.mangafeed.Common.MangaEnums;
import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.UI.Mappers.IAccount;
import com.amgregoire.mangafeed.UI.Presenters.AccountPresStats;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public class AccountFragmentStats extends Fragment implements IAccount.AccountStatsMap
{
    public final static String TAG = AccountFragmentStats.class.getSimpleName();

    @BindView(R.id.textViewAccountStatsSourceName) TextView mSourceName;
    @BindView(R.id.textViewAccountStatsCompleted) TextView mCompletedCount;
    @BindView(R.id.textViewAccountStatsOnHold) TextView mOnHoldCount;
    @BindView(R.id.textViewAccountStatsPlanToRead) TextView mPlanToReadCount;
    @BindView(R.id.textViewAccountStatsReading) TextView mReadingCount;

    private IAccount.AccountStatsPres mPresenter;

    /***
     * This function creates and returns a new instance of the OfflineFragment.
     *
     * @return
     */
    public static AccountFragmentStats newInstance()
    {
        return new AccountFragmentStats();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View lView = inflater.inflate(R.layout.fragment_account_stats, container, false);
        ButterKnife.bind(this, lView);

        mPresenter = new AccountPresStats(this, getActivity().getSupportFragmentManager());
        mPresenter.init(getArguments());

        return lView;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mPresenter.unSubEventBus();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mPresenter.subEventBus();
    }

    @OnClick(R.id.linearLayoutStatsReadingContainer)
    public void onReadingContainerClick()
    {
        mPresenter.startFilterFragment(1, MangaEnums.FollowType.Reading.name());
    }

    @OnClick(R.id.linearLayoutStatsCompletedContainer)
    public void onCompletedContainerClick()
    {
        mPresenter.startFilterFragment(2, MangaEnums.FollowType.Completed.name());
    }

    @OnClick(R.id.linearLayoutStatsOnHoldContainer)
    public void onOnHoldContainerClick()
    {
        mPresenter.startFilterFragment(3, MangaEnums.FollowType.On_Hold.name());
    }

    @OnClick(R.id.linearLayoutStatsPlanToReadContainer)
    public void onPlanToReadContainerClick()
    {
        mPresenter.startFilterFragment(4, MangaEnums.FollowType.Plan_to_Read.name());
    }

    @Override
    public void initViews()
    {
        mSourceName.setText(MangaFeed.getInstance().getCurrentSource().getSourceName());
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void setFollowStats(List<Long> values)
    {
        mReadingCount.setText(Long.toString(values.get(0)));
        mCompletedCount.setText(Long.toString(values.get(1)));
        mOnHoldCount.setText(Long.toString(values.get(2)));
        mPlanToReadCount.setText(Long.toString(values.get(3)));
    }

}
