package com.amgregoire.mangafeed.v2.ui.info


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.amgregoire.mangafeed.Common.RequestWrapper
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.Models.DbChapter
import com.amgregoire.mangafeed.Models.DbManga
import com.amgregoire.mangafeed.R
import com.amgregoire.mangafeed.Utils.MangaDB
import com.amgregoire.mangafeed.ioScope
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.service.CloudFlareService
import com.amgregoire.mangafeed.v2.service.Logger
import com.amgregoire.mangafeed.v2.ui.catalog.enum.FollowType
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch

class MangaInfoViewModelFactory(private var app: Application, var dbManga: DbManga) : ViewModelProvider.Factory
{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T
    {
        return MangaInfoViewModel(app, dbManga) as T
    }
}

data class MangaInfo(val dbManga: DbManga, val dbChapters: List<DbChapter>)
data class MangaInfoBottomNav(val followText: Int, val followIcon: Int, val startText: Int)

class MangaInfoViewModel(val app: Application, var dbManga: DbManga) : AndroidViewModel(app)
{
    private val subscribers = CompositeDisposable()

    val state = MutableLiveData<State>()
    val mangaInfo = MutableLiveData<MangaInfo>()
    val mangaInfoBottomNav = MutableLiveData<MangaInfoBottomNav>()

    private var chapters = listOf<DbChapter>()

    init
    {
        refresh()
    }

    fun setFollowStatus(followType: FollowType) = ioScope.launch {
        val startText = if (!dbManga.recentChapter.isNullOrEmpty()) R.string.text_continue
        else R.string.text_start

        uiScope.launch { mangaInfoBottomNav.value = MangaInfoBottomNav(followType.stringRes, followType.drawableRes, startText) }

        dbManga.updateFollowing(followType.value)
        if (followType == FollowType.Unfollow) dbManga.recentChapter = null
        MangaDB.getInstance().putManga(dbManga)
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
                    source.updateMangaObservable(RequestWrapper(dbManga))
                            .subscribe(
                                    { newManga ->
                                        dbManga = newManga
                                        mangaInfo.value = MangaInfo(dbManga, chapters)
                                        state.value = State.Complete
                                    },
                                    { throwable ->
                                        mangaInfo.value = MangaInfo(dbManga, chapters)
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
                    source.getChapterListObservable(RequestWrapper(dbManga))
                            .subscribe(
                                    { chapters ->
                                        MangaFeed.app.currentDbChapters = chapters
                                        this.chapters = chapters
                                        mangaInfo.value = MangaInfo(dbManga, chapters)
                                    },
                                    { throwable ->
                                        Logger.error(throwable)
                                        mangaInfo.value = MangaInfo(dbManga, chapters)
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