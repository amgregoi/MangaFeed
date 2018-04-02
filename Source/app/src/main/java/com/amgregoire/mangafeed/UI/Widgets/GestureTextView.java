package com.amgregoire.mangafeed.UI.Widgets;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by amgregoi on 6/22/17.
 */
public class GestureTextView extends AppCompatTextView implements GestureDetector.OnDoubleTapListener
{

    private GestureViewPager.UserGestureListener mUserGestureListener;

    public GestureTextView(Context context)
    {
        super(context);
    }

    public GestureTextView(Context context, @Nullable AttributeSet attrs)
    {
        super(context, attrs);
    }

    public GestureTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    public GestureTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes)
//    {
//        super(context, attrs, defStyleAttr, defStyleRes);
//    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return super.onTouchEvent(event);
    }

    public void setUserGesureListener(GestureViewPager.UserGestureListener singleTapListener)
    {
        mUserGestureListener = singleTapListener;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e)
    {
        mUserGestureListener.onSingleTap();
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e)
    {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e)
    {
        return false;
    }
}
