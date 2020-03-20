package com.amgregoire.mangafeed.v2.model.domain

import com.amgregoire.mangafeed.v2.ui.catalog.enum.FollowType

class UserLibrary(
        val library: ArrayList<Manga>
)
{
    fun filter(followType: FollowType) = library.filter { it.followType == followType }

    fun updateItem(manga:Manga)
    {
        library.remove(manga)
        library.add(manga)
    }
}