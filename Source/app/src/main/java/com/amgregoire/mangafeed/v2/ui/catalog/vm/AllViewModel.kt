package com.amgregoire.mangafeed.v2.ui.catalog.vm

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import com.amgregoire.mangafeed.Models.Manga
import com.amgregoire.mangafeed.Utils.MangaDB
import com.amgregoire.mangafeed.Utils.SharedPrefs
import io.reactivex.disposables.CompositeDisposable

class AllViewModel(app: Application) : AndroidViewModel(app)
{
    val state = MutableLiveData<State>()
    private val subscribers = CompositeDisposable()

    init
    {
        retrieveAll()
    }

    private fun retrieveAll()
    {
        state.value = State.Loading

        val database = MangaDB.getInstance() // Dagger injection?
        val source = SharedPrefs.getSavedSource() // Dagger injection?
        subscribers.add(
                database.catalogList
                        .cache()
                        .subscribe(
                                { mangaList ->
                                    if (mangaList.isEmpty()) state.value = State.Failed(Error("Uh oh, $source does not seem to have any manga."), canRefresh = false)
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