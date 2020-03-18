package com.amgregoire.mangafeed.v2.service;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by amgregoi on 10/8/18.
 */

public class KeyboardUtil
{
    private final static int MAGIC_NUMBER = 200;

    public static void hide(Activity activity)
    {
        activity.getPackageName();
        if(isShowing(activity))
        {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0);
        }
    }

    public static void show(Activity activity)
    {
        if(!isShowing(activity))
        {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
        }
    }

    private static boolean isShowing(Activity activity)
    {
        Rect r = new Rect();
        View lRootView = ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
        lRootView.getWindowVisibleDisplayFrame(r);
        float mScreenDensity = activity.getResources().getDisplayMetrics().density;
        int heightDiff = lRootView.getRootView().getHeight() - (r.bottom - r.top);
        float dp = heightDiff / mScreenDensity;

        return dp > MAGIC_NUMBER;
    }
}
