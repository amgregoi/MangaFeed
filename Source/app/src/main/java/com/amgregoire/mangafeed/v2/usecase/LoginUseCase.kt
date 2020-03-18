package com.amgregoire.mangafeed.v2.usecase

import com.amgregoire.mangafeed.v2.network.Result
import com.amgregoire.mangafeed.v2.model.domain.User
import com.amgregoire.mangafeed.v2.repository.remote.RemoteUserRepository
import com.amgregoire.mangafeed.v2.service.Logger

class LoginUseCase
{
    private val userRepo = RemoteUserRepository()

    fun login(email: String, password: String, result: (User?) -> Unit)
    {
        userRepo.login(email, password) {
            when (it)
            {
                is Result.Success -> result(it.value)
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