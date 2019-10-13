package com.amgregoire.mangafeed.v2;

/**
 * Created by amgregoi on 2/4/19.
 */

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.amgregoire.mangafeed.v2.service.ScreenUtil;

public class LockedBottomSheetBehaviour extends BottomSheetBehavior<ConstraintLayout>
{
    private int toolbarHeight;

    public LockedBottomSheetBehaviour()
    {
        super();
    }

    public LockedBottomSheetBehaviour(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.toolbarHeight = ScreenUtil.INSTANCE.getToolbarHeight(context);
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, ConstraintLayout child, MotionEvent event)
    {
        return false;
    }

    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, ConstraintLayout child, MotionEvent event)
    {
        return false;
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, ConstraintLayout child, View directTargetChild, View target, int nestedScrollAxes)
    {
        return false;
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, ConstraintLayout child, View target, int dx, int dy, int[] consumed)
    {
    }

    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, ConstraintLayout child, View target)
    {
    }

    @Override
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, ConstraintLayout child, View target, float velocityX, float velocityY)
    {
        return false;
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull ConstraintLayout child, @NonNull View directTargetChild, @NonNull View target, int axes, int type)
    {
        return true;
    }

    /***
     * Testing
     */
    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, ConstraintLayout child, View dependency)
    {
        return dependency instanceof TabLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, ConstraintLayout child, View dependency)
    {
        if (dependency instanceof TabLayout)
        {
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            int fabBottomMargin = lp.bottomMargin;
            int distanceToScroll = child.getHeight() + fabBottomMargin;
            float ratio = (float) dependency.getY() / (float) toolbarHeight;
            child.setTranslationY(-distanceToScroll * ratio);
        }
        return true;
    }
}