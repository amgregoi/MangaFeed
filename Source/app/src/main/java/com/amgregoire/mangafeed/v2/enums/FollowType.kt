package com.amgregoire.mangafeed.v2.enums

import com.amgregoire.mangafeed.R

/***
 * This enum is for the various follow status types.
 */
enum class FollowType(var value: Int, var stringRes: Int, var drawableRes: Int, val id: String? = null)
{
    Unfollow(0, R.string.manga_info_header_fab_follow, R.drawable.ic_heart_outline_white_24dp),
    Reading(1, R.string.manga_info_header_fab_reading, R.drawable.ic_heart_white_24dp, "fa919567-3b29-4623-a371-df48826ca2f5"),
    Completed(2, R.string.manga_info_header_fab_complete, R.drawable.ic_heart_white_24dp, "bab9d3ff-e98c-4318-a80d-bddc4b5ca858"),
    On_Hold(3, R.string.manga_info_header_fab_on_hold, R.drawable.ic_heart_white_24dp, "31d1fd74-c8ce-46ee-8ff6-3bbfc2a3fc3c"),
    Plan_to_Read(4, R.string.manga_info_header_fab_plan_to_read, R.drawable.ic_heart_white_24dp, "17a274a1-a587-4ad0-b9ce-5ef4d4e6fc14");

    override fun toString(): String
    {
        return super.toString().replace("_", " ")
    }

    companion object
    {
        fun getTypeFromValue(value: Int): FollowType
        {
            if (value >= values().size) return Unfollow
            return values()[value]
        }
    }
}