package com.amgregoire.mangafeed.v2.usecase

import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.ioScope
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.model.domain.Manga
import com.amgregoire.mangafeed.v2.service.CloudFlareService
import com.amgregoire.mangafeed.v2.service.Logger
import kotlinx.coroutines.launch

class GetRecentUseCase
{
    fun retrieveRecentList(result: (List<Manga>) -> Unit) = ioScope.launch {
        CloudFlareService().verifyCookieAndDoAction {
            val source = MangaFeed.app.currentSource
            source.recentMangaObservable
                    .cache()
                    .subscribe(
                            { mangaList -> uiScope.launch { result(mangaList) } },
                            {
                                Logger.error("Failed to retrieve recents list: $it")
                                uiScope.launch { result(listOf()) }
                            }
                    )
        }
    }
}