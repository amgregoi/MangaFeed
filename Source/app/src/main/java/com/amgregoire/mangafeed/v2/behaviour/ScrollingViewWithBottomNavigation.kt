package com.amgregoire.mangafeed.v2.behaviour

import android.content.Context
import android.support.design.widget.AppBarLayout
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.CoordinatorLayout
import android.util.AttributeSet
import android.view.View


class ScrollingViewWithBottomNavigationBehavior() : AppBarLayout.ScrollingViewBehavior()
{
    constructor(context: Context, attrs: AttributeSet) : this()
    {
        AppBarLayout.ScrollingViewBehavior(context, attrs)
    }

    // We add a bottom margin to avoid the bottom navigation bar
    private var bottomMargin = 0

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean
    {
        return super.layoutDependsOn(parent, child, dependency) || dependency is BottomNavigationView
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean
    {
        val result = super.onDependentViewChanged(parent, child, dependency)

        if (dependency is BottomNavigationView && dependency.height != bottomMargin)
        {
            bottomMargin = dependency.height
            val layout = child.layoutParams as CoordinatorLayout.LayoutParams
            layout.bottomMargin = bottomMargin
            child.requestLayout()
            return true
        }
        else
        {
            return result
        }
    }
}