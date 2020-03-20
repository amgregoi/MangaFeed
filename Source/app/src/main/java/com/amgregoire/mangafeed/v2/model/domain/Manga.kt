package com.amgregoire.mangafeed.v2.model.domain

import com.amgregoire.mangafeed.v2.ui.catalog.enum.FollowType

data class Manga(
        val id:String,
        val name: String,
        val description: String,
        val link: String,
        val alternateNames: String,
        val artists: String,
        val authors: String,
        val genres: String,
        val source: String,
        val followType: FollowType
)
{
    override fun equals(other: Any?): Boolean
    {
        if(other !is Manga) return false
        if(other.id == id) return true
        return false
    }

    override fun hashCode(): Int
    {
        return id.hashCode()
    }
}