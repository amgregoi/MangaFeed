package com.amgregoire.mangafeed.v2.ui

import android.content.Context
import com.amgregoire.mangafeed.R

object AttrService
{
    fun getAttrColor(context: Context, attr:Int):Int
    {
        val attrs = intArrayOf(attr)
        val ta = context.obtainStyledAttributes(attrs)
        val color = ta.getResourceId(0, R.color.colorAccent)
        ta.recycle()
        return color
    }
}