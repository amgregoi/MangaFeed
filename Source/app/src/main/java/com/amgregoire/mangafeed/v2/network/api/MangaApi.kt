package com.amgregoire.mangafeed.v2.network.api

import com.amgregoire.mangafeed.v2.model.dto.ApiManga
import com.amgregoire.mangafeed.v2.network.Result
import com.amgregoire.mangafeed.v2.network.RetroService
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Created by amgregoi on 3/20/19.
 */

interface MangaApi
{
    companion object
    {
        fun getInstance(): MangaApi = RetroService.retrofit.create(MangaApi::class.java)
    }

    /***
     *
     */
    @POST("/manga")
    fun createManga(@Body requestBody: CreateMangaRequest): Call<Result<ApiManga>>

    data class CreateMangaRequest(
            @SerializedName("alternate") val alternate: String,
            @SerializedName("artist") val artist: List<String>,
            @SerializedName("author") val author: List<String>,
            @SerializedName("description") val description: String,
            @SerializedName("genres") val genres: List<String>,
            @SerializedName("image") val image: String,
            @SerializedName("link") val link: String,
            @SerializedName("name") val name: String,
            @SerializedName("source") val source: String,
            @SerializedName("status") val status: String
    )

    /***
     *
     */
    @PUT("/manga/{mangaId}")
    fun updateManga(@Path("mangaId") mangaId: String, @Body requestBody: CreateMangaRequest): Call<Result<ApiManga>>
}