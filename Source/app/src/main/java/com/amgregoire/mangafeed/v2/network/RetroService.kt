package com.amgregoire.mangafeed.v2.network

import android.util.Base64
import com.amgregoire.mangafeed.BuildConfig
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.v2.extension.base64Encode
import com.amgregoire.mangafeed.v2.extension.getAuthToken
import com.amgregoire.mangafeed.v2.extension.toJson
import com.amgregoire.mangafeed.v2.service.NetworkLogService
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetroService
{
    var retrofit: Retrofit

    private val okhttp = provideOkHttpClient()
    private val logService = NetworkLogService()

    init
    {
        retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(okhttp)
                .addCallAdapterFactory(ResultCallFactory())
                .addConverterFactory(GsonConverterFactory.create(provideGson()))
                .build()
    }

    fun provideGson() = GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .setLenient()
            .create()

    private fun provideBaseOkHttpClient(): OkHttpClient.Builder = OkHttpClient.Builder()
            .connectTimeout(25, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .addInterceptor(provideResultInterceptor())


    private fun provideOkHttpClient() = provideBaseOkHttpClient()
            .addInterceptor(provideAuthInterceptor())
            .addInterceptor(provideLoggingInterceptor())
            .build()

    /********************************************************************************************************
     *
     * Interceptors
     *
     *******************************************************************************************************/
    private fun provideResultInterceptor(): Interceptor
    {
        return Interceptor { chain ->
            val response = chain.proceed(chain.request())
            logService.logResponse(response)
            return@Interceptor response
        }
    }

    private fun provideAuthInterceptor(): Interceptor
    {
        return Interceptor { chain ->
            val newRequest = chain.request().newBuilder()

            MangaFeed.app.user?.let {
                val token = it.getAuthToken().toJson().base64Encode(Base64.NO_WRAP)
                newRequest.addHeader(AuthConstants.Authorization, "${AuthConstants.Bearer} $token")
            }

            chain.proceed(newRequest.build())
        }
    }

    private fun provideLoggingInterceptor(): Interceptor
    {
        return Interceptor { chain ->
            val request = chain.request()
            logService.logRequest(request)
            chain.proceed(request)
        }
    }

    /********************************************************************************************************
     *
     * Helper Functions
     *
     *******************************************************************************************************/


    object AuthConstants
    {
        const val Authorization = "Authorization"
        const val Bearer = "Bearer "

    }



}