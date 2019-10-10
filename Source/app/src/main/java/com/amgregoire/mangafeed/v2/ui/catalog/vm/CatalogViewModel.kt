package com.amgregoire.mangafeed.v2.ui.catalog.vm

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.Models.Manga
import com.amgregoire.mangafeed.Utils.MangaDB
import io.reactivex.disposables.CompositeDisposable

class CatalogViewModel() : ViewModel()
{
    private val subscribers = CompositeDisposable()

    val recent = MutableLiveData<List<Manga>>()
    val library = MutableLiveData<List<Manga>>()
    val all = MutableLiveData<List<Manga>>()

    init
    {
        retrieveRecentList()
        retrieveLibrary()
        retrieveAll()
    }

    private fun retrieveAll()
    {
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


    private fun retrieveLibrary()
    {
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

    fun retrieveRecentList()
    {
        val source = MangaFeed.app.currentSource
        subscribers.add(
                source.recentMangaObservable
                        .cache()
                        .subscribe(
                                { mangaList -> recent.value = mangaList },
                                { recent.value = listOf() }
                        )
        )
    }

    override fun onCleared()
    {
        super.onCleared()
        subscribers.clear()
    }
}