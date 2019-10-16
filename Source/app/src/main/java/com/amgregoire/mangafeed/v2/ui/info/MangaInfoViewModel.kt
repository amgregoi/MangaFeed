package com.amgregoire.mangafeed.v2.ui.info


import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.amgregoire.mangafeed.Common.RequestWrapper
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.Models.Chapter
import com.amgregoire.mangafeed.Models.Manga
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.Utils.MangaDB
import com.amgregoire.mangafeed.ioScope
import com.amgregoire.mangafeed.v2.ui.Logger
import com.amgregoire.mangafeed.v2.ui.catalog.enum.FollowType
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch

class MangaInfoViewModelFactory(private var app: Application, var manga: Manga) : ViewModelProvider.Factory
{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T
    {
        return MangaInfoViewModel(app, manga) as T
    }
}

data class MangaInfo(val manga: Manga, val chapters: List<Chapter>)
data class MangaInfoBottomNav(val followText: Int, val followIcon: Int, val startText: Int)
class MangaInfoViewModel(val app: Application, var baseManga: Manga) : AndroidViewModel(app)
{
    private val subscribers = CompositeDisposable()

    val state = MutableLiveData<State>()
    val mangaInfo = MutableLiveData<MangaInfo>()
    val mangaInfoBottomNav = MutableLiveData<MangaInfoBottomNav>()

    private var chapters = listOf<Chapter>()

    init
    {
        refresh()
    }

    fun setManga(manga: Manga)
    {
        baseManga = manga
        refresh()
    }

    fun setFollowStatus(followType: FollowType)
    {
        val startText = if (baseManga.recentChapter != null) R.string.text_continue
        else R.string.text_start

        mangaInfoBottomNav.value = MangaInfoBottomNav(followType.stringRes, followType.drawableRes, startText)

        baseManga.updateFollowing(followType.value)
        if (followType == FollowType.Unfollow) baseManga.recentChapter = null
        MangaDB.getInstance().putManga(baseManga)
    }

    fun setFollowStatus(followType: Int)
    {
        val type = FollowType.getTypeFromValue(followType)
        setFollowStatus(type)
    }

    fun refresh()
    {
        state.value = State.Loading
        ioScope.launch {
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
                    source.updateMangaObservable(RequestWrapper(baseManga))
                            .subscribe(
                                    { newManga ->
                                        baseManga = newManga
                                        mangaInfo.value = MangaInfo(baseManga, chapters)
                                        state.value = State.Complete
                                        MangaFeed.app.updateManga(newManga)
                                    },
                                    { throwable ->
                                        mangaInfo.value = MangaInfo(baseManga, chapters)
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
                    source.getChapterListObservable(RequestWrapper(baseManga))
                            .subscribe(
                                    { chapters ->
                                        MangaFeed.app.currentChapters = chapters
                                        this.chapters = chapters
                                        mangaInfo.value = MangaInfo(baseManga, chapters)
                                    },
                                    { throwable ->
                                        Logger.error(throwable)
                                        mangaInfo.value = MangaInfo(baseManga, chapters)
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