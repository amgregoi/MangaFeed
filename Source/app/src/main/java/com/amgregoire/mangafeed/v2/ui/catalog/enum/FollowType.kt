package com.amgregoire.mangafeed.v2.ui.catalog.enum

import com.amgregoire.mangafeed.R

enum class FollowType(var value: Int, var stringRes: Int, var drawableRes: Int)
{
    Unfollow(0, R.string.manga_info_header_fab_follow, R.drawable.ic_heart_outline_white_24dp),
    Reading(1, R.string.manga_info_header_fab_reading, R.drawable.ic_heart_white_24dp),
    Completed(2, R.string.manga_info_header_fab_complete, R.drawable.ic_heart_white_24dp),
    On_Hold(3, R.string.manga_info_header_fab_on_hold, R.drawable.ic_heart_white_24dp),
    Plan_to_Read(4, R.string.manga_info_header_fab_plan_to_read, R.drawable.ic_heart_white_24dp);

    override fun toString(): String
    {
        return super.toString().replace("_", " ")
    }
}