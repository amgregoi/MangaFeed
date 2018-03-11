package com.amgregoire.mangafeed.UI.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amgregoire.mangafeed.Common.WifiBroadcastReceiver;
import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.UI.Mappers.IAccount;
import com.amgregoire.mangafeed.Utils.MangaDB;
import com.amgregoire.mangafeed.Utils.MangaLogger;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public class AccountFragmentStats extends Fragment implements IAccount.AccountStatseMap
{
    public final static String TAG = AccountFragmentStats.class.getSimpleName();

    @BindView(R.id.textViewAccountStatsSourceName) TextView mSourceName;
    @BindView(R.id.textViewAccountStatsCompleted) TextView mCompletedCount;
    @BindView(R.id.textViewAccountStatsOnHold) TextView mOnHoldCount;
    @BindView(R.id.textViewAccountStatsPlanToRead) TextView mPlanToReadCount;
    @BindView(R.id.textViewAccountStatsReading) TextView mReadingCount;

    private List<Long> lStatValues;

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

        initViews();

        return lView;
    }

    @OnClick(R.id.linearLayoutStatsCompletedContainer)
    public void onCompletedContainerClick()
    {
        if (lStatValues.get(1) > 0)
        {
            startFilteredFragment(2, "Completed");
        }
        else
        {
            MangaFeed.getInstance().makeToastShort("You have no items in this section");
        }
    }

    @OnClick(R.id.linearLayoutStatsReadingContainer)
    public void onReadingContainerClick()
    {
        if (lStatValues.get(0) > 0)
        {
            startFilteredFragment(1, "Reading");
        }
        else
        {
            MangaFeed.getInstance().makeToastShort("You have no items in this section");
        }
    }

    @OnClick(R.id.linearLayoutStatsOnHoldContainer)
    public void onOnHoldContainerClick()
    {
        if (lStatValues.get(2) > 0)
        {
            startFilteredFragment(3, "On-Hold");
        }
        else
        {
            MangaFeed.getInstance().makeToastShort("You have no items in this section");
        }
    }

    @OnClick(R.id.linearLayoutStatsPlanToReadContainer)
    public void onPlanToReadContainerClick()
    {
        if (lStatValues.get(3) > 0)
        {
            startFilteredFragment(4, "Plan to Read");
        }
        else
        {
            MangaFeed.getInstance().makeToastShort("You have no items in this section");
        }
    }

    private void startFilteredFragment(int filter, String title)
    {
        if (WifiBroadcastReceiver.hasInternet(getContext()))
        {
            Fragment lFragment = AccountFragmentFiltered.newInstance(filter, title);
            getActivity().getSupportFragmentManager()
                         .beginTransaction()
                         .add(android.R.id.content, lFragment)
                         .addToBackStack(null)
                         .commit();
        }
        else
        {
            MangaFeed.getInstance().makeToastShort("You are currently offline, try again later.");
        }
    }

    @Override
    public void initViews()
    {
        // Do nothing.
        lStatValues = new ArrayList<>();

        mSourceName.setText(MangaFeed.getInstance().getCurrentSource().getSourceName());
        MangaDB.getInstance()
               .test(1, 2, 3, 4)
               .subscribe(aLong -> lStatValues.add(aLong),
                       throwable -> MangaLogger.logError(TAG, throwable.getMessage()),
                       () ->
                       {
                           mReadingCount.setText(Long.toString(lStatValues.get(0)));
                           mCompletedCount.setText(Long.toString(lStatValues.get(1)));
                           mOnHoldCount.setText(Long.toString(lStatValues.get(2)));
                           mPlanToReadCount.setText(Long.toString(lStatValues.get(3)));
                       });
    }


}
