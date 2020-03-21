package com.amgregoire.mangafeed.v2.repository.remote

import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.ioScope
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.model.domain.Manga
import com.amgregoire.mangafeed.v2.model.mappers.ApiMangaToMangaMapper
import com.amgregoire.mangafeed.v2.model.mappers.MangaToCreateMangaRequestMapper
import com.amgregoire.mangafeed.v2.model.mappers.UserLibraryMapper
import com.amgregoire.mangafeed.v2.network.Result
import com.amgregoire.mangafeed.v2.network.api.MangaApi
import com.amgregoire.mangafeed.v2.network.api.UserMangaApi
import com.amgregoire.mangafeed.v2.network.result
import com.amgregoire.mangafeed.v2.repository.local.LocalUserRepository
import com.amgregoire.mangafeed.v2.service.Logger
import kotlinx.coroutines.launch

class RemoteMangaRepository(
        private val localUserRepository: LocalUserRepository = LocalUserRepository(),
        private val userMangaApi: UserMangaApi = UserMangaApi.getInstance(),
        private val mangaApi: MangaApi = MangaApi.getInstance(),
        private val userLibraryMapper: UserLibraryMapper = UserLibraryMapper(),
        private val apiMangaToMangaMapper: ApiMangaToMangaMapper = ApiMangaToMangaMapper(),
        private val mangaToCreateMangaRequestMapper: MangaToCreateMangaRequestMapper = MangaToCreateMangaRequestMapper()
)
{

    /***
     * This function updates the users follow status of a specified manga.
     * If there is not reference to the remote version of the manga it will attempt to retrieve it before updating the users status.
     *
     * @param result Function1<RetroResult<User>, Unit>
     * @return Job
     */
    fun updateManga(manga: Manga, followTypeId: String?) = ioScope.launch {
        try
        {
            val user = localUserRepository.getUser() ?: kotlin.run {
                return@launch
            }

            val mangaId = manga.id
            /***
             * If ID is null, we must create/retrieve the remote version of this manga.
             * If we successfully retrieve the model, we can update the follow status for the user
             */
            if (mangaId == null)
            {
                createManga(manga) { createResult ->
                    when (createResult)
                    {
                        is Result.Success ->
                        {
                            val newMangaId = createResult.value.id ?: return@createManga

                            val request = UserMangaApi.FollowTypeRequest(followTypeId)
                            ioScope.launch {
                                val library = userMangaApi.updateMangaFollowStatus(user.id, newMangaId, request).result {
                                    userLibraryMapper.map(it)
                                }

                                when (library)
                                {
                                    is Result.Success ->
                                    {
                                        Logger.debug("Successfully updated remote follow status")
                                        MangaFeed.app.userLibrary = library.value
                                    }
                                    is Result.Failure ->
                                    {
                                        Logger.debug("Failed to update follow status")
                                    }
                                }
                            }
                        }
                        is Result.Failure ->
                        {
                            val x = createResult
                            Logger.error("Failed to create/retrieve manga")
                        }
                    }
                }

                return@launch
            }

            /***
             * We have the remote models ID, so we can go ahead and update the follow status
             */

            val request = UserMangaApi.FollowTypeRequest(followTypeId)
            userMangaApi.updateMangaFollowStatus(user.id, mangaId, request).result {
                userLibraryMapper.map(it)
            }
        }
        catch (ex: Exception)
        {
            Logger.error(ex)
        }
    }

    /***
     * This function attempts to create a manga on the remote database.
     * It will return a newly created manga, or one that was already created previously.
     *
     * @param manga Manga
     * @param result Function1<Result<Manga>, Unit>
     * @return Job
     */
    private fun createManga(manga: Manga, result: (Result<Manga>) -> Unit) = ioScope.launch {
        val res = try
        {
            val user = localUserRepository.getUser() ?: kotlin.run {
                result(Result.Failure())
                return@launch
            }

            val request = mangaToCreateMangaRequestMapper.map(manga)
            mangaApi.createManga(request).result {
                apiMangaToMangaMapper.map(it)
                        .apply {
                            followType = manga.followType
                            recentChapter = manga.recentChapter
                        }
            }
        }
        catch (ex: Exception)
        {
            Result.Failure<Manga>(ex)
        }

        uiScope.launch { result(res) }
    }

    /***
     * This function updates the remote Manga item, to keep image links, description, and other information up to date.
     *
     * @param manga Manga
     * @param result Function1<Result<Manga>, Unit>
     * @return Job
     */
    fun updateManga(manga: Manga) = ioScope.launch {
        try
        {
            if (!manga.requiresUpdate) return@launch
            localUserRepository.getUser() ?: return@launch

            val mangaId = manga.id
            if (mangaId == null)
            {
                createManga(manga) { result ->
                    when (result)
                    {
                        is Result.Success ->
                        {
                            val newMangaId = result.value.id ?: return@createManga
                            val request = mangaToCreateMangaRequestMapper.map(manga)
                            ioScope.launch {
                                val updateResult = mangaApi.updateManga(newMangaId, request).result {
                                    apiMangaToMangaMapper.map(it)
                                            .apply {
                                                followType = manga.followType
                                                recentChapter = manga.recentChapter
                                            }
                                }

                                when (updateResult)
                                {
                                    is Result.Success -> Logger.error("Successfully updated remote manga")
                                    is Result.Failure -> Logger.error("Failed to update remote manga")

                                }
                            }
                        }
                        is Result.Failure ->
                        {
                            Logger.error("Failed to create/retrieve manga")
                        }
                    }
                }

                return@launch
            }

            /***
             *
             */
            val request = mangaToCreateMangaRequestMapper.map(manga)
            mangaApi.updateManga(mangaId, request).result {
                apiMangaToMangaMapper.map(it)
                        .apply {
                            followType = manga.followType
                            recentChapter = manga.recentChapter
                        }
            }
        }
        catch (ex: Exception)
        {
            Logger.error(ex)
        }
    }
}