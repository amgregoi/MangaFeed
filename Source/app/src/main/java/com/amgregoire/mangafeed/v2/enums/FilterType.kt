package com.amgregoire.mangafeed.v2.enums

/***
 * This enum is for the various manga filter status'
 */
enum class FilterType(val value: Int)
{
    NONE(0), READING(1), ON_HOLD(3), COMPLETE(2), FOLLOWING(5);

}