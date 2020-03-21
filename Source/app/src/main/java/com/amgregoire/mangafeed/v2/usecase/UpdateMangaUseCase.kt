package com.amgregoire.mangafeed.v2.usecase

import com.amgregoire.mangafeed.v2.enums.FollowType
import com.amgregoire.mangafeed.v2.model.domain.Manga
import com.amgregoire.mangafeed.v2.repository.local.LocalMangaRepository
import com.amgregoire.mangafeed.v2.repository.remote.RemoteMangaRepository

class UpdateMangaUseCase(
        private val local: LocalMangaRepository = LocalMangaRepository(),
        private val remote: RemoteMangaRepository = RemoteMangaRepository()
)
{
    fun updateManga(manga: Manga)
    {
        local.updateManga(manga, null)
        remote.updateManga(manga)
    }

    fun updateMangaFollowStatus(manga: Manga, followType: FollowType?)
    {
        local.updateManga(manga, followType)
        remote.updateManga(manga, followType?.id)
    }
}