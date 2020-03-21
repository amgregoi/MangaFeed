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
    fun getUser(): Call<Result<LoginResponse>>

    /***
     *
     */
    @POST("/users/signup")
    fun postSignUp(@Body requestBody: CreateUserRequest): Call<Result<LoginResponse>>

    data class CreateUserRequest(val email: String, val password: String)

    /***
     *
     */
    @POST("/users/login")
    fun postSignIn(@Body requestBody: UserSignInRequest): Call<Result<LoginResponse>>

    data class UserSignInRequest(val email: String, val password: String)


    /***
     *
     */
    @POST("/users/logout")
    fun postSignOut(@Body requestBody: UserLogoutRequest): Call<Result<String>>

    data class UserLogoutRequest(val accessToken: String)

}