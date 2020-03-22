package com.amgregoire.mangafeed.v2.usecase

import com.amgregoire.mangafeed.v2.exception.EmptyException
import com.amgregoire.mangafeed.v2.model.domain.Manga
import com.amgregoire.mangafeed.v2.network.Result
import com.amgregoire.mangafeed.v2.repository.local.LocalMangaRepository

class GetCatalogUseCase(
        private val catalogRepository: LocalMangaRepository = LocalMangaRepository()
)
{
    fun getCatalog(result: (Result<List<Manga>>) -> Unit)
    {
        catalogRepository.getCatalogList {
            if (it.isEmpty()) result(Result.Failure(EmptyException("No manga to view")))
            else result(Result.Success(it))
        }
    }
}
