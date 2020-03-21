package com.amgregoire.mangafeed.v2.widget;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
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
