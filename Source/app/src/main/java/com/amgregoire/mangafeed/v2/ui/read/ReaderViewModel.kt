package com.amgregoire.mangafeed.v2.ui.read

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amgregoire.mangafeed.Common.MangaEnums
import com.amgregoire.mangafeed.Common.RequestWrapper
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.Models.DbChapter
import com.amgregoire.mangafeed.v2.model.domain.Manga
import com.amgregoire.mangafeed.v2.repository.local.LocalChapterRepository
import com.amgregoire.mangafeed.v2.repository.local.LocalMangaRepository
import com.amgregoire.mangafeed.v2.service.CloudFlareService
import com.amgregoire.mangafeed.v2.service.Logger
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


data class ReaderInfo(var manga: Manga, var dbChapter: DbChapter, var dbChapters: List<DbChapter>)
data class ChapterInfo(var dbChapter: DbChapter, var title: String, var currentPage: Int = 0, var totalPages: Int = 0)

sealed class ReaderUIState
{
    object INIT : ReaderUIState()
    object SHOW : ReaderUIState()
    object HIDE : ReaderUIState()
    object BLOCK : ReaderUIState()
}

class ReaderViewModel : ViewModel()
{
    val uiState = MutableLiveData<ReaderUIState>()
    val readerInfo = MutableLiveData<ReaderInfo>()
    val chapterInfo = MutableLiveData<ChapterInfo>()

    private val subscriptions = CompositeDisposable()
    private val localMangaRepository = LocalMangaRepository()
    private val localChapterRepository = LocalChapterRepository()

    // Note: should only be called from manga info fragment to initialize reader view model data
    // TODO :: account for reversed list throughout vm (currently only account for ascending order due to ui
    fun updateReaderInfo(manga: Manga, dbChapters: List<DbChapter>, dbChapter: DbChapter, isDataReversed: Boolean)
    {
        if (isDataReversed) readerInfo.value = ReaderInfo(manga, dbChapter, dbChapters.reversed())
        else readerInfo.value = ReaderInfo(manga, dbChapter, dbChapters)

        if (manga.isFollowing)
        {
            localChapterRepository.putChapter(dbChapter)
            manga.recentChapter = dbChapter.url
            localMangaRepository.updateManga(manga)
        }

        updateChapterInfo(dbChapter, dbChapter.chapterTitle, dbChapter.currentPage, dbChapter.totalPages)
    }

    private fun updateReaderInfo(manga: Manga, dbChapters: List<DbChapter>, dbChapter: DbChapter)
    {
        readerInfo.value = ReaderInfo(manga, dbChapter, dbChapters)

        if (manga.isFollowing)
        {
            localChapterRepository.putChapter(dbChapter)
            manga.recentChapter = dbChapter.url
            localMangaRepository.updateManga(manga)
        }
    }

    fun updateCurrentChapterByPosition(position: Int)
    {
        val info = readerInfo.value ?: return
        val newChapter = getChapterByPosition(position) ?: return
        if (info.dbChapter.url == newChapter.url) return

        updateReaderInfo(info.manga, info.dbChapters, newChapter)
        updateChapterInfo(newChapter, newChapter.chapterTitle, 0, newChapter.totalPages)
    }

    fun getChapterByPosition(position: Int): DbChapter?
    {
        val info = readerInfo.value ?: return null
        return info.dbChapters.getOrNull(position)
    }

    /***
     * Note chapters are ordered in descending order, incrementing is going down the list (-1)
     * TODO :: account for normal and reversed chapter lists
     */
    fun incrementChapter()
    {
        val info = readerInfo.value ?: return
        val position = info.dbChapters.indexOf(info.dbChapter)
        val newChapter = info.dbChapters.getOrNull(position + 1) ?: return

        updateReaderInfo(info.manga, info.dbChapters, newChapter)
        updateChapterInfo(newChapter, newChapter.chapterTitle, 0, newChapter.totalPages)
    }

    /***
     * Note chapters are ordered in descending order, incrementing is going up the list (+1)
     * TODO :: account for normal and reversed chapter lists
     */
    fun decrementChapter()
    {
        val info = readerInfo.value ?: return
        val position = info.dbChapters.indexOf(info.dbChapter)
        val newChapter = info.dbChapters.getOrNull(position - 1) ?: return

        updateReaderInfo(info.manga, info.dbChapters, newChapter)
        updateChapterInfo(newChapter, newChapter.chapterTitle, 0, newChapter.totalPages)
    }

    fun toggleUiState()
    {
        val state = uiState.value ?: run {
            uiState.value = ReaderUIState.INIT
            return
        }

        uiState.value =
                if (state == ReaderUIState.HIDE) ReaderUIState.SHOW
                else ReaderUIState.HIDE
    }

    fun setUiStateBlock()
    {
        //        uiState.value = ReaderUIState.BLOCK
    }

    fun setUIStateShow()
    {
        uiState.value = ReaderUIState.SHOW
    }

    fun updateChapterInfo(dbChapter: DbChapter?, title: String? = null, currentPage: Int? = null, totalPages: Int? = null)
    {
        dbChapter ?: return

        val info = readerInfo.value ?: return

        if (dbChapter.url != info.dbChapter.url) return

        val newInfo = ChapterInfo(dbChapter, title ?: dbChapter.chapterTitle, currentPage ?: dbChapter.currentPage, totalPages ?: dbChapter.totalPages)
        chapterInfo.value = newInfo
    }

    fun incrementPage(dbChapter: DbChapter)
    {
        val reader = readerInfo.value ?: return
        val current = chapterInfo.value ?: return
        if (reader.dbChapter.url != dbChapter.url) return

        if (current.currentPage < current.totalPages) updateChapterInfo(dbChapter, currentPage = current.currentPage + 1)
    }

    fun decrementPage(dbChapter: DbChapter)
    {
        val reader = readerInfo.value ?: return
        val current = chapterInfo.value ?: return
        if (reader.dbChapter.url != dbChapter.url) return
        if (current.currentPage > 0) updateChapterInfo(dbChapter, currentPage = current.currentPage - 1)
    }

    fun isCurrentChapter(dbChapter: DbChapter): Boolean
    {
        val info = readerInfo.value ?: return false
        return info.dbChapter.url == dbChapter.url
    }

    fun getChapterContents(dbChapter: DbChapter, chapterContent: (List<String>, DbChapter, Boolean) -> Unit)
    {
        CloudFlareService().verifyCookieAndDoAction {
            val urls = arrayListOf<String>()
            subscriptions.add(
                    MangaFeed.app
                            .currentSource
                            .getChapterImageListObservable(RequestWrapper(dbChapter))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    { url ->
                                        urls.add(url)
                                        updateChapterInfo(dbChapter, "Pages loaded: ${urls.size}", 0, 0)

                                    },
                                    { throwable ->
                                        Logger.error(throwable)
                                        updateChapterInfo(dbChapter, "Problem retrieving pages, try refreshing", 0, 0)
                                    },
                                    {
                                        dbChapter.totalPages = urls.size
                                        updateChapterInfo(dbChapter, dbChapter.chapterTitle, 0, urls.size)
                                        chapterContent.invoke(urls, dbChapter, MangaFeed.app.currentSourceType == MangaEnums.SourceType.MANGA)
                                    })
            )
        }
    }


    fun getCurrentPosition(): Int
    {
        val info = readerInfo.value ?: return 0
        return info.dbChapters.indexOf(info.dbChapter)
    }


    override fun onCleared()
    {
        super.onCleared()
        subscriptions.clear()
    }
}