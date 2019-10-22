package com.amgregoire.mangafeed.UI.Widgets;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.amgregoire.mangafeed.Common.MangaEnums;
import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.R;
import com.amgregoire.mangafeed.UI.Adapters.ImagePagerAdapter;
import com.amgregoire.mangafeed.Utils.MangaLogger;
import com.amgregoire.mangafeed.Utils.SharedPrefs;
import com.amgregoire.mangafeed.v2.custom.EmptyState;
import com.amgregoire.mangafeed.v2.service.Logger;

public class GestureViewPager extends ViewPager implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener
{
    public final static String TAG = GestureViewPager.class.getSimpleName();

    private GestureImageView mGestureImageView;
    private GestureDetector mGestureDetector;
    private UserGestureListener mUserGestureListener;
    private EmptyState mEmptyState;

    private boolean mVertical;

    public GestureViewPager(Context context)
    {
        super(context);
        mGestureDetector = new GestureDetector(getContext(), this);
        setScrollerType();
    }

    public GestureViewPager(Context aContext, AttributeSet aAttributeSet)
    {
        super(aContext, aAttributeSet);
        mGestureDetector = new GestureDetector(getContext(), this);
        setScrollerType();
    }

    public boolean setScrollerType()
    {
        mVertical = SharedPrefs.getChapterScrollVertical();

        if (mVertical)
        {
            setPageTransformer(true, new VerticalPageTransformer());
            setOverScrollMode(OVER_SCROLL_IF_CONTENT_SCROLLS);
            return true;
        }
        else
        {
            setPageTransformer(true, null);
            return false;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event)
    {
        //Try catch was added to handle androids viewpager bug that pops up sometimes.
        //super.onInterceptTouchEvent throws index out of bounds exception.
        try
        {
            fetchGestureImageViewByTag();

            mGestureDetector.onTouchEvent(event);

            if (mEmptyState != null) mEmptyState.dispatchTouchEvent(event);

            //ACTION_DOWN workaround for checkSwipe()
            if (event.getAction() == MotionEvent.ACTION_DOWN)
                mHorizontalSwipeX = event.getX();

            if (mGestureImageView != null)
            {
                if (!mGestureImageView.canScrollParent(mVertical))
                {
                    return false;
                }

                if (mVertical)
                {
                    boolean lResult = super.onInterceptTouchEvent(swapXY(event));
                    swapXY(event); // return touch coordinates to original reference frame for any child views
                    return lResult;
                }
            }

            return super.onInterceptTouchEvent(event);
        }
        catch (Exception aException)
        {
            MangaLogger.logError(TAG, aException.getMessage());
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        checkOverScroll(ev);

        if (mVertical) return super.onTouchEvent(swapXY(ev));
        else return super.onTouchEvent(ev);
    }

    private void checkOverScroll(MotionEvent ev)
    {
        if (getCurrentItem() == 0 && checkSwipe(ev) == eSwipeDirection.LEFT)
        {
            if (MangaFeed.Companion.getApp().getCurrentSourceType() == MangaEnums.SourceType.MANGA)
                mUserGestureListener.onLeft();
        }
        else if (getAdapter() != null && getAdapter().getCount() - 1 == getCurrentItem() && checkSwipe(ev) == eSwipeDirection.RIGHT)
        {
            if (MangaFeed.Companion.getApp().getCurrentSourceType() == MangaEnums.SourceType.MANGA)
                mUserGestureListener.onRight();
        }
    }

    float mHorizontalSwipeX;

    private eSwipeDirection checkSwipe(MotionEvent ev)
    {
        switch (ev.getAction())
        {
            //ACTION_DOWN is getting handled up stream, need to look into this..
            case MotionEvent.ACTION_DOWN:
                mHorizontalSwipeX = ev.getX();
                break;
            case MotionEvent.ACTION_UP:
                if (mHorizontalSwipeX > 0)
                {
                    if (mHorizontalSwipeX < ev.getX())
                    {
                        return eSwipeDirection.LEFT;
                    }
                    else
                    {
                        return eSwipeDirection.RIGHT;
                    }
                }
        }

        return eSwipeDirection.NEUTRAL;
    }

    private void fetchGestureImageViewByTag()
    {
        mEmptyState = findViewWithTag(ImagePagerAdapter.Companion.getEMPTY_TAG() + ":" + getCurrentItem());
        mGestureImageView = findViewWithTag(ImagePagerAdapter.Companion.getIMAGE_TAG() + ":" + getCurrentItem());
    }

    /**
     * Swaps the X and Y coordinates of your touch event.
     */
    private MotionEvent swapXY(MotionEvent ev)
    {
        float width = getWidth();
        float height = getHeight();

        float newX = (ev.getY() / height) * width;
        float newY = (ev.getX() / width) * height;

        ev.setLocation(newX, newY);

        return ev;
    }

    @Override
    public boolean onDown(MotionEvent aEvent)
    {
        if (mGestureImageView != null)
        {
            if (mGestureImageView.isInitialized())
            {
                mGestureImageView.cancelFling();
            }
        }
        return true;
    }

    @Override
    public void onShowPress(MotionEvent aEvent)
    {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent aEvent)
    {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent aEvent, MotionEvent aEvent2, float aXDistance, float aYDistance)
    {
        if (mGestureImageView != null)
        {
            if (mGestureImageView.isInitialized())
            {
                mGestureImageView.postTranslate(-aXDistance, -aYDistance);
            }
        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent aEvent)
    {
    }

    @Override
    public boolean onFling(MotionEvent aEvent, MotionEvent aEvent2, float aXDistance, float aYDistance)
    {
        if (mGestureImageView != null)
        {
            if (mGestureImageView.isInitialized())
            {
                mGestureImageView.startFling(aXDistance, aYDistance);
            }
        }
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent aEvent)
    {
        final float positionX = aEvent.getX();

        if (positionX < getWidth() * 0.2f)
        {
            decrementCurrentItem();
        }
        else if (positionX > getWidth() * 0.8f)
        {
            incrementCurrentItem();
        }
        else
        {
            if (mUserGestureListener != null)
            {
                mUserGestureListener.onSingleTap();
            }
        }

        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent aEvent)
    {
        if (mGestureImageView != null)
        {
            if (mGestureImageView.isInitialized())
            {
                if (mGestureImageView.getScale() > mGestureImageView.MIN_SCALE)
                {
                    mGestureImageView.zoomToPoint(mGestureImageView.MIN_SCALE, getWidth() / 2, getHeight() / 2);
                }
                else if (mGestureImageView.getScale() < mGestureImageView.MED_SCALE)
                {
                    mGestureImageView.zoomToPoint(mGestureImageView.MED_SCALE, aEvent.getX(), aEvent.getY());
                }
            }
        }
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent aEvent)
    {
        return false;
    }

    public void decrementCurrentItem()
    {
        int position = getCurrentItem();
        if (position != 0)
        {
            setCurrentItem(position - 1, true);
        }
    }

    public void incrementCurrentItem()
    {
        int position = getCurrentItem();
        if (getAdapter() != null)
        {
            if (position != getAdapter().getCount() - 1)
            {
                setCurrentItem(position + 1, true);
            }
        }
    }

    public void setUserGestureListener(UserGestureListener singleTapListener)
    {
        mUserGestureListener = singleTapListener;
    }

    public boolean toggleVerticalScroller()
    {
        return setScrollerType();
    }

    public interface UserGestureListener
    {
        void onSingleTap();

        void onLeft();

        void onRight();
    }

    private enum eSwipeDirection
    {
        LEFT,
        RIGHT,
        NEUTRAL
    }
}

class VerticalPageTransformer implements ViewPager.PageTransformer
{

    @Override
    public void transformPage(View view, float position)
    {

        if (position < -1)
        { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setAlpha(0);

        }
        else if (position <= 1)
        { // [-1,1]
            view.setAlpha(1);

            // Counteract the default slide transition
            view.setTranslationX(view.getWidth() * -position);

            //set Y position to swipe in from top
            float yPosition = position * view.getHeight();
            view.setTranslationY(yPosition);

        }
        else
        { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setAlpha(0);
        }
    }
}