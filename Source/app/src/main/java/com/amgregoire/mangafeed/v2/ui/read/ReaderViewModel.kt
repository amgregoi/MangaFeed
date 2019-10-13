package com.amgregoire.mangafeed.v2.ui.read

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.amgregoire.mangafeed.Common.MangaEnums
import com.amgregoire.mangafeed.Common.RequestWrapper
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.Models.Chapter
import com.amgregoire.mangafeed.Models.Manga
import com.amgregoire.mangafeed.v2.ui.Logger
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


data class ReaderInfo(var manga: Manga, var chapters: List<Chapter>, var chapter: Chapter)
data class ChapterInfo(var title: String, var currentPage: Int = 0, var totalPages: Int = 0)

sealed class ReaderUIState
{
    object SHOW : ReaderUIState()
    object HIDE : ReaderUIState()
}

class ReaderViewModel : ViewModel()
{
    val uiState = MutableLiveData<ReaderUIState>()
    val readerInfo = MutableLiveData<ReaderInfo>()
    val chapterInfo = MutableLiveData<ChapterInfo>()

    private val subscriptions = CompositeDisposable()

    fun updateReaderInfo(manga: Manga, chapters: List<Chapter>, chapter: Chapter)
    {
        readerInfo.value = ReaderInfo(manga, chapters, chapter)
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
     */
    fun incrementChapter()
    {
        val info = readerInfo.value ?: return
        val position = info.chapters.indexOf(info.chapter)
        //        updateCurrentChapterByPosition(position - 1)

        val newChapter = info.chapters.getOrNull(position - 1) ?: return
        updateReaderInfo(info.manga, info.chapters, newChapter)
        updateChapterInfo(newChapter, newChapter.chapterTitle, 0, newChapter.totalPages)
    }

    /***
     * Note chapters are ordered in descending order, incrementing is going up the list (+1)
     */
    fun decrementChapter()
    {
        val info = readerInfo.value ?: return
        val position = info.chapters.indexOf(info.chapter)
        //        updateCurrentChapterByPosition(position + 1)

        val newChapter = info.chapters.getOrNull(position + 1) ?: return
        updateReaderInfo(info.manga, info.chapters, newChapter)
        updateChapterInfo(newChapter, newChapter.chapterTitle, 0, newChapter.totalPages)
    }

    fun toggleUiState()
    {
        val state = uiState.value ?: ReaderUIState.SHOW
        uiState.value =
                if (state == ReaderUIState.SHOW) ReaderUIState.HIDE
                else ReaderUIState.SHOW
    }

    fun updateChapterInfo(chapter: Chapter?, title: String? = null, currentPage: Int? = null, totalPages: Int? = null)
    {
        chapter ?: return

        val info = readerInfo.value ?: return

        if (chapter.url != info.chapter.url) return

        val current = (chapterInfo.value ?: ChapterInfo(""))
                .apply {
                    title?.let { this.title = title }
                    currentPage?.let { this.currentPage = currentPage }
                    totalPages?.let { this.totalPages = totalPages }
                }

        chapterInfo.value = current
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

    fun getChapterContents(chapter: Chapter, chapterContent: (List<String>, Chapter, Boolean) -> Unit)
    {
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