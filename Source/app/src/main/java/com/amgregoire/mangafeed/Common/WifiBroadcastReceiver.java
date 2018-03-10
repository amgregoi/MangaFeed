package com.amgregoire.mangafeed.Common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Andy Gregoire on 3/10/2018.
 */

public class WifiBroadcastReceiver extends BroadcastReceiver
{



    public interface WifiResponseListener
    {
        void hasInternet();
        void hasNoInternet();
    }


    private WifiResponseListener mListener;

    public WifiBroadcastReceiver()
    {

    }

    public WifiBroadcastReceiver(WifiResponseListener listener)
    {
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        try
        {
                if (hasInternet(context))
                {
                    mListener.hasInternet();
                }
                else
                {
                    mListener.hasNoInternet();
                }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static boolean hasInternet(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected())
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
