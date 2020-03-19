package com.amgregoire.mangafeed.v2.ui.read

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amgregoire.mangafeed.Common.MangaEnums
import com.amgregoire.mangafeed.Common.RequestWrapper
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.Models.Chapter
import com.amgregoire.mangafeed.Models.Manga
import com.amgregoire.mangafeed.Utils.MangaDB
import com.amgregoire.mangafeed.v2.service.CloudFlareService
import com.amgregoire.mangafeed.v2.service.Logger
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


data class ReaderInfo(var manga: Manga, var chapter: Chapter, var chapters: List<Chapter>)
data class ChapterInfo(var chapter: Chapter, var title: String, var currentPage: Int = 0, var totalPages: Int = 0)

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

    // Note: should only be called from manga info fragment to initialize reader view model data
    // TODO :: account for reversed list throughout vm (currently only account for ascending order due to ui
    fun updateReaderInfo(manga: Manga, chapters: List<Chapter>, chapter: Chapter, isDataReversed: Boolean)
    {
        if (isDataReversed) readerInfo.value = ReaderInfo(manga, chapter, chapters.reversed())
        else readerInfo.value = ReaderInfo(manga, chapter, chapters)

        if (manga.isFollowing)
        {
            MangaDB.getInstance().putChapter(chapter)

            manga.recentChapter = chapter.url
            MangaDB.getInstance().putManga(manga)
        }

        updateChapterInfo(chapter, chapter.chapterTitle, chapter.currentPage, chapter.totalPages)
    }

    private fun updateReaderInfo(manga: Manga, chapters: List<Chapter>, chapter: Chapter)
    {
        readerInfo.value = ReaderInfo(manga, chapter, chapters)

        if (manga.isFollowing)
        {
            MangaDB.getInstance().putChapter(chapter)

            manga.recentChapter = chapter.url
            MangaDB.getInstance().putManga(manga)
        }
    }

    fun updateCurrentChapterByPosition(position: Int)
    {
        val info = readerInfo.value ?: return
        val newChapter = getChapterByPosition(position) ?: return
        if (info.chapter.url == newChapter.url) return

        updateReaderInfo(info.manga, info.chapters, newChapter)
        updateChapterInfo(newChapter, newChapter.chapterTitle, 0, newChapter.totalPages)
    }

    fun getChapterByPosition(position: Int): Chapter?
    {
        val info = readerInfo.value ?: return null
        return info.chapters.getOrNull(position)
    }

    /***
     * Note chapters are ordered in descending order, incrementing is going down the list (-1)
     * TODO :: account for normal and reversed chapter lists
     */
    fun incrementChapter()
    {
        val info = readerInfo.value ?: return
        val position = info.chapters.indexOf(info.chapter)
        val newChapter = info.chapters.getOrNull(position + 1) ?: return

        updateReaderInfo(info.manga, info.chapters, newChapter)
        updateChapterInfo(newChapter, newChapter.chapterTitle, 0, newChapter.totalPages)
    }

    /***
     * Note chapters are ordered in descending order, incrementing is going up the list (+1)
     * TODO :: account for normal and reversed chapter lists
     */
    fun decrementChapter()
    {
        val info = readerInfo.value ?: return
        val position = info.chapters.indexOf(info.chapter)
        val newChapter = info.chapters.getOrNull(position - 1) ?: return

        updateReaderInfo(info.manga, info.chapters, newChapter)
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

    fun updateChapterInfo(chapter: Chapter?, title: String? = null, currentPage: Int? = null, totalPages: Int? = null)
    {
        chapter ?: return

        val info = readerInfo.value ?: return

        if (chapter.url != info.chapter.url) return

        val newInfo = ChapterInfo(chapter, title ?: chapter.chapterTitle, currentPage ?: chapter.currentPage, totalPages ?: chapter.totalPages)
        chapterInfo.value = newInfo
    }

    fun incrementPage(chapter: Chapter)
    {
        val reader = readerInfo.value ?: return
        val current = chapterInfo.value ?: return
        if (reader.chapter.url != chapter.url) return

        if (current.currentPage < current.totalPages) updateChapterInfo(chapter, currentPage = current.currentPage + 1)
    }

    fun decrementPage(chapter: Chapter)
    {
        val reader = readerInfo.value ?: return
        val current = chapterInfo.value ?: return
        if (reader.chapter.url != chapter.url) return
        if (current.currentPage > 0) updateChapterInfo(chapter, currentPage = current.currentPage - 1)
    }

    fun isCurrentChapter(chapter: Chapter): Boolean
    {
        val info = readerInfo.value ?: return false
        return info.chapter.url == chapter.url
    }

    fun getChapterContents(chapter: Chapter, chapterContent: (List<String>, Chapter, Boolean) -> Unit)
    {
        CloudFlareService().verifyCookieAndDoAction {
            val urls = arrayListOf<String>()
            subscriptions.add(
                    MangaFeed.app
                            .currentSource
                            .getChapterImageListObservable(RequestWrapper(chapter))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    { url ->
                                        urls.add(url)
                                        updateChapterInfo(chapter, "Pages loaded: ${urls.size}", 0, 0)

                                    },
                                    { throwable ->
                                        Logger.error(throwable)
                                        updateChapterInfo(chapter, "Problem retrieving pages, try refreshing", 0, 0)
                                    },
                                    {
                                        chapter.totalPages = urls.size
                                        updateChapterInfo(chapter, chapter.chapterTitle, 0, urls.size)
                                        chapterContent.invoke(urls, chapter, MangaFeed.app.currentSourceType == MangaEnums.SourceType.MANGA)
                                    })
            )
        }
    }


    fun getCurrentPosition(): Int
    {
        val info = readerInfo.value ?: return 0
        return info.chapters.indexOf(info.chapter)
    }


    override fun onCleared()
    {
        super.onCleared()
        subscriptions.clear()
    }
}