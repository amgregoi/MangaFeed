package com.amgregoire.mangafeed.v2.repository.local

import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.v2.model.domain.User
import com.amgregoire.mangafeed.v2.model.dto.ApiUser

class LocalUserRepository
{
    fun getUser(): User?
    {
        return MangaFeed.app.user
    }
}