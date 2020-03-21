package com.amgregoire.mangafeed.v2.network.api

import com.amgregoire.mangafeed.v2.model.dto.ApiUser
import com.amgregoire.mangafeed.v2.model.dto.small.MangaSmallQualified
import com.amgregoire.mangafeed.v2.model.dto.small.MangaSmallUnqualified
import com.amgregoire.mangafeed.v2.network.Result
import com.amgregoire.mangafeed.v2.network.RetroService
import com.amgregoire.mangafeed.v2.repository.remote.FullUser
import retrofit2.Call
import retrofit2.http.*
import java.util.*

/**
 * Created by amgregoi on 3/20/19.
 */

interface UserMangaApi
{
    companion object
    {
        fun getInstance(): UserMangaApi = RetroService.retrofit.create(UserMangaApi::class.java)
    }


    data class FollowTypeRequest(val followTypeId: String? = null)
    data class UnqualifiedMangaRequest(val mangas: List<MangaSmallUnqualified>)
    data class QualifiedMangaRequest(val mangaIds: List<MangaSmallQualified>)

    /***
     *
     * @param userId String
     * @param requestBody UnqualifiedMangaRequest
     */
    @POST("/users/{userId}/manga/unqualified")
    fun createManga(@Path("userId") userId: String, @Body requestBody: UnqualifiedMangaRequest):Call<Result<ApiUser>>


    /***
     *
     * @param userId String
     * @param requestBody UnqualifiedMangaRequest
     */
    @POST("/users/{userId}/manga/unqualified")
    fun addMangaToUser(@Path("userId") userId: String, @Body requestBody: UnqualifiedMangaRequest):Call<Result<ApiUser>>

    /***
     *
     * @param userId String
     * @param requestBody QualifiedMangaRequest
     */
    @POST("/users/{userId}/manga")
    fun addMangaToUser(@Path("userId") userId: String, @Body requestBody: QualifiedMangaRequest):Call<Result<ApiUser>>

    /***
     *
     * @param userId String
     * @param requestBody UnqualifiedMangaRequest
     */
    @DELETE("/users/{userId}/manga/unqualified")
    fun removeMangaToUser(@Path("userId") userId: String, @Body requestBody: UnqualifiedMangaRequest): Call<Result<ApiUser>>

    /***
     *
     * @param userId String
     * @param requestBody QualifiedMangaRequest
     */
    @DELETE("/users/{userId}/manga")
    fun removeMangaToUser(@Path("userId") userId: String, @Body requestBody: QualifiedMangaRequest): Call<Result<ApiUser>>

    /***
     *
     * @param userId String
     * @param mangaId String
     * @param requestBody FollowTypeRequest
     */
    @PUT("/users/{userId}/manga/{mangaId}")
    fun updateMangaFollowStatus(@Path("userId") userId: String, @Path("mangaId") mangaId: String, @Body requestBody: FollowTypeRequest): Call<Result<ApiUser>>
}