package com.amgregoire.mangafeed.v2.network.api

import com.amgregoire.mangafeed.v2.model.dto.ApiUser
import com.amgregoire.mangafeed.v2.network.Result
import com.amgregoire.mangafeed.v2.network.RetroService
import com.amgregoire.mangafeed.v2.network.response.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Created by amgregoi on 3/20/19.
 */

interface UserApi
{
    companion object
    {
        fun getInstance(): UserApi = RetroService.retrofit.create(UserApi::class.java)
    }

    /***
     *
     */
    @GET("/users")
    fun getUser(): Call<Result<ApiUser>>

    /***
     *
     */
    @POST("/users/signup")
    fun postSignUp(@Body requestBody: CreateUserRequest): Call<Result<LoginResponse>>

    data class CreateUserRequest(val name: String, val email: String, val password: String)

    /***
     *
     */
    @POST("/users/login")
    fun postLogin(@Body requestBody: UserLoginRequest): Call<Result<LoginResponse>>

    data class UserLoginRequest(val email: String, val password: String)


    /***
     *
     */
    @POST("/users/logout")
    fun postLogout(@Body requestBody: UserLogoutRequest): Call<Result<String>>

    data class UserLogoutRequest(val accessToken: String)

}