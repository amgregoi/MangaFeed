package com.amgregoire.mangafeed.v2.ui.catalog.vm

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.Models.Manga
import com.amgregoire.mangafeed.Utils.MangaDB
import io.reactivex.disposables.CompositeDisposable

class LibraryViewModel(app: Application) : AndroidViewModel(app)
{
    val state = MutableLiveData<State>()
    private val subscribers = CompositeDisposable()

    init
    {
        retrieveLibrary()
    }

    private fun retrieveLibrary()
    {
        state.value = State.Loading

        val database = MangaDB.getInstance() // Dagger injection?
        subscribers.add(
                database.libraryList
                        .cache()
                        .subscribe(
                                { mangaList ->
                                    if(mangaList.isEmpty())state.value = State.Failed(Error("Uh oh, it seems you don't have any favourites"), canRefresh = false)
                                    else state.value = State.Complete(mangaList)
                                },
                                { throwable ->
                                    state.value = State.Failed(Error("Something went wrong, check your internet connection or please try back later"), canRefresh = true)
                                }
                        )
        )
    }

    sealed class State
    {
        object Loading : State()
        data class Complete(val mangaList: List<Manga>) : State()
        data class Failed(val error: Error, val canRefresh: Boolean) : State()
    }
}