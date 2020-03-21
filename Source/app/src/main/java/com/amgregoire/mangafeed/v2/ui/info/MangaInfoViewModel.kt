package com.amgregoire.mangafeed.v2.ui.info


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.amgregoire.mangafeed.Common.RequestWrapper
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.Models.DbChapter
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.ioScope
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.enums.FollowType
import com.amgregoire.mangafeed.v2.model.domain.Manga
import com.amgregoire.mangafeed.v2.service.CloudFlareService
import com.amgregoire.mangafeed.v2.service.Logger
import com.amgregoire.mangafeed.v2.usecase.UpdateMangaUseCase
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch

class MangaInfoViewModelFactory(private var app: Application, var manga: Manga) : ViewModelProvider.Factory
{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T
    {
        return MangaInfoViewModel(app, manga) as T
    }
}

data class MangaInfo(val manga: Manga, val dbChapters: List<DbChapter>)

data class MangaInfoBottomNav(val followText: Int, val followIcon: Int, val startText: Int)

class MangaInfoViewModel(val app: Application, var manga: Manga) : AndroidViewModel(app)
{
    private val subscribers = CompositeDisposable()

    private val updateMangaUseCase = UpdateMangaUseCase()

    val state = MutableLiveData<State>()
    val mangaInfo = MutableLiveData<MangaInfo>()
    val mangaInfoBottomNav = MutableLiveData<MangaInfoBottomNav>()

    private var chapters = listOf<DbChapter>()

    init
    {
        refresh()
    }

    fun setFollowStatus(followType: FollowType) = ioScope.launch {
        val startText = if (manga.recentChapter.isNotEmpty()) R.string.text_continue
        else R.string.text_start

        uiScope.launch { mangaInfoBottomNav.value = MangaInfoBottomNav(followType.stringRes, followType.drawableRes, startText) }

        if (manga.followType == followType) return@launch
        updateMangaUseCase.updateMangaFollowStatus(manga, followType)
    }

    fun setFollowStatus(followType: Int) = ioScope.launch {
        val type = FollowType.getTypeFromValue(followType)
        setFollowStatus(type)
    }

    fun refresh() = ioScope.launch {
        uiScope.launch { state.value = State.Loading }

        CloudFlareService().verifyCookieAndDoAction {
            fetchMangaInfoOnline()
            fetchChapterListOnline()
        }
    }

    private fun fetchMangaInfoOnline()
    {
        try
        {
            val source = MangaFeed.app.currentSource
            subscribers.add(
                    source.updateMangaObservable(RequestWrapper(manga))
                            .subscribe(
                                    { newManga ->
                                        manga = newManga
                                        mangaInfo.value = MangaInfo(manga, chapters)
                                        updateMangaUseCase.updateManga(manga)
                                        state.value = State.Complete
                                    },
                                    { throwable ->
                                        mangaInfo.value = MangaInfo(manga, chapters)
                                        state.value = State.Complete
                                        Logger.error(throwable)
                                    }
                            )
            )
        }
        catch (ex: Exception)
        {
            Logger.error(ex)
        }

    }

    /***
     * This function retrieves the chapter list from its source.
     *
     */
    private fun fetchChapterListOnline()
    {
        try
        {
            val source = MangaFeed.app.currentSource
            subscribers.add(
                    source.getChapterListObservable(RequestWrapper(manga))
                            .subscribe(
                                    { chapters ->
                                        MangaFeed.app.currentDbChapters = chapters
                                        this.chapters = chapters
                                        mangaInfo.value = MangaInfo(manga, chapters)
                                    },
                                    { throwable ->
                                        Logger.error(throwable)
                                        mangaInfo.value = MangaInfo(manga, chapters)
                                    }
                            )
            )
        }
        catch (ex: Exception)
        {
            Logger.error(ex)
        }

    }

    sealed class State
    {
        object Loading : State()
        object Complete : State()
        object Failed : State()
    }
}