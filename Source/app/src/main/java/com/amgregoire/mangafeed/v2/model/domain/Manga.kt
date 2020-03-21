package com.amgregoire.mangafeed.v2.model.domain

import com.amgregoire.mangafeed.v2.enums.FollowType
import com.amgregoire.mangafeed.v2.enums.Source

data class Manga(
        var id: String?,
        val name: String,
        var link: String,
        var description: String,
        var image: String,
        var alternateNames: String,
        var artists: String,
        var authors: String,
        var genres: String,
        var source: String,
        var status: String,
        var followType: FollowType,
        var recentChapter: String,
        var requiresUpdate: Boolean
)
{
    val isFollowing: Boolean
        get() = followType.value > 0

    fun getUrl(): String
    {
        val source = Source.getSourceByName(source)
        return source.baseUrl + link
    }

    override fun equals(other: Any?): Boolean
    {
        if (other !is Manga) return false
        if (other.link == link && other.source == source) return true
        return false
    }

    override fun hashCode(): Int
    {
        return "$source:$link".hashCode()
    }

}