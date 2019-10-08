package com.amgregoire.mangafeed.v2.ui.catalog.vm

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.Models.Manga
import io.reactivex.disposables.CompositeDisposable

class RecentViewModel(app: Application) : AndroidViewModel(app)
{
    val state = MutableLiveData<State>()
    private val subscribers = CompositeDisposable()

    init
    {
        retrieveRecentList()
    }

    fun retrieveRecentList()
    {
        state.value = State.Loading
        val source = MangaFeed.app.currentSource
        subscribers.add(
                source.recentMangaObservable
                        .cache()
                        .subscribe(
                                { mangaList ->
                                    if (mangaList.isEmpty()) state.value = State.Failed(Error("We are having problems with ${source.sourceName}, please try  back later"), canRefresh = true)
                                    else state.value = State.Complete(mangaList)
                                },
                                {
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