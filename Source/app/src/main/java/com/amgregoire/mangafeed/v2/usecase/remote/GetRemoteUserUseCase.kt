package com.amgregoire.mangafeed.v2.usecase.remote

import com.amgregoire.mangafeed.v2.network.Result
import com.amgregoire.mangafeed.v2.model.domain.User
import com.amgregoire.mangafeed.v2.model.dto.ApiUser
import com.amgregoire.mangafeed.v2.repository.remote.FullUser
import com.amgregoire.mangafeed.v2.repository.remote.RemoteUserRepository
import com.amgregoire.mangafeed.v2.service.Logger

class GetRemoteUserUseCase
{
    private val userRepo = RemoteUserRepository()

    fun user(result:(FullUser?) -> Unit)
    {
        userRepo.getUser{
            when (it)
            {
                is Result.Success ->
                {
                    result(it.value)
                }
                is Result.Failure ->
                {
                    Logger.error("Error code: ${it.code}")
                    Logger.error(it.throwable.cause)
                    result(null)
                }
            }
        }
    }
}