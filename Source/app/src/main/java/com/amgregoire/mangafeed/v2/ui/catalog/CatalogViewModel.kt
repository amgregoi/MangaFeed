package com.amgregoire.mangafeed.v2.ui.catalog

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.Models.Manga
import com.amgregoire.mangafeed.Utils.MangaDB
import com.amgregoire.mangafeed.ioScope
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.service.Logger
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CatalogViewModel : ViewModel()
{
    private val subscribers = CompositeDisposable()

    val recent = MutableLiveData<List<Manga>>()
    val library = MutableLiveData<List<Manga>>()
    val all = MutableLiveData<List<Manga>>()

    val lastItem = MutableLiveData<Manga>()
    val queryFilter = MutableLiveData<String>()

    var isLastItemComplete = true

    init
    {
        retrieveRecentList()
        retrieveLibrary()
        retrieveAll()
    }

    fun setQuery(query: String) = ioScope.launch {
        val prevQuery = queryFilter.value ?: ""
        if (prevQuery.length > 1 && query.isEmpty()) return@launch

        uiScope.launch { queryFilter.value = query }
    }

    private fun retrieveAll() = ioScope.launch {
        val database = MangaDB.getInstance() // Dagger injection?
        subscribers.add(
                database.catalogList
                        .cache()
                        .subscribe(
                                { mangaList -> all.value = mangaList },
                                { throwable -> all.value = listOf() }
                        )
        )
    }


    private fun retrieveLibrary() = ioScope.launch {
        val database = MangaDB.getInstance() // Dagger injection?
        subscribers.add(
                database.libraryList
                        .cache()
                        .subscribe(
                                { mangaList -> library.value = mangaList },
                                { library.value = listOf() }
                        )
        )
    }

    fun retrieveRecentList() = ioScope.launch {
        val source = MangaFeed.app.currentSource
        subscribers.add(
                source.recentMangaObservable
                        .cache()
                        .subscribe(
                                { mangaList -> recent.value = mangaList },
                                {
                                    Logger.error("Failed to retrieve recents list: $it")
                                    recent.value = listOf()
                                }
                        )
        )
    }

    fun setLastItem(manga: Manga) = ioScope.launch {
        isLastItemComplete = false

        uiScope.launch { lastItem.value = manga }

        delay(750)
        isLastItemComplete = true
    }

    override fun onCleared()
    {
        super.onCleared()
        subscribers.clear()
    }
}