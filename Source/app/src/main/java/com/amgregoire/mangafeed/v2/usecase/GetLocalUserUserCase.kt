package com.amgregoire.mangafeed.v2.usecase

import com.amgregoire.mangafeed.v2.repository.local.LocalUserRepository

class GetLocalUserUserCase
{
    val userRepo = LocalUserRepository()

    fun user() = userRepo.getUser()
}