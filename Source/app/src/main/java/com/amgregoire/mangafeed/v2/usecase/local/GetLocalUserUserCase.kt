package com.amgregoire.mangafeed.v2.usecase.local

import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.v2.repository.local.LocalUserRepository

class GetLocalUserUserCase
{
    val userRepo = LocalUserRepository()

    fun user() = userRepo.getUser()

    fun isGuest() = userRepo.getUser() == null && MangaFeed.app.isSignedIn
}