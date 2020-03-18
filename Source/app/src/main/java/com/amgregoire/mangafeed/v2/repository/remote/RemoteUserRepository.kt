package com.amgregoire.mangafeed.v2.repository.remote

import com.amgregoire.mangafeed.ioScope
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.model.domain.User
import com.amgregoire.mangafeed.v2.model.dto.ApiUser
import com.amgregoire.mangafeed.v2.model.mappers.GetUserDataMapper
import com.amgregoire.mangafeed.v2.model.mappers.LoginUserDataMapper
import com.amgregoire.mangafeed.v2.network.Result
import com.amgregoire.mangafeed.v2.network.api.UserApi
import com.amgregoire.mangafeed.v2.network.response.LoginResponse
import com.amgregoire.mangafeed.v2.network.result
import kotlinx.coroutines.launch

class RemoteUserRepository(
        private val loginUserMapper: LoginUserDataMapper = LoginUserDataMapper(),
        private val getUserMapper: GetUserDataMapper = GetUserDataMapper(),
        private val userApi: UserApi = UserApi.getInstance()
)
{
    /***
     *
     * @param result Function1<RetroResult<User>, Unit>
     * @return Job
     */
    fun getUser(result: (Result<User>) -> Unit) = ioScope.launch {
        val res = try
        {
            userApi.getUser().result { mapUser(it) }
        }
        catch (ex: Exception)
        {
            Result.Failure<User>(ex)
        }

        uiScope.launch { result(res) }
    }

    /***
     *
     * @param email String
     * @param password String
     * @param result Function1<RetroResult<User>, Unit>
     * @return Job
     */
    fun login(email: String, password: String, result: (Result<User>) -> Unit) = ioScope.launch {
        val res = try
        {
            val loginRequest = UserApi.UserLoginRequest(email, password)
            userApi.postLogin(loginRequest).result { mapUser(it) }
        }
        catch (ex: Exception)
        {
            Result.Failure<User>(ex)
        }

        uiScope.launch { result(res) }
    }

    /***
     *
     * @param name String
     * @param email String
     * @param password String
     * @param result Function1<RetroResult<User>, Unit>
     * @return Job
     */
    fun signUp(name: String, email: String, password: String, result: (Result<User>) -> Unit) = ioScope.launch {
        val res = try
        {
            val loginRequest = UserApi.CreateUserRequest(name, email, password)
            userApi.postSignUp(loginRequest).result { mapUser(it) }
        }
        catch (ex: Exception)
        {
            Result.Failure<User>(ex)
        }

        uiScope.launch { result(res) }
    }

    // Mappers

    private fun mapUser(input: LoginResponse) = loginUserMapper.map(input)
    private fun mapUser(input: ApiUser) = getUserMapper.map(input)
}