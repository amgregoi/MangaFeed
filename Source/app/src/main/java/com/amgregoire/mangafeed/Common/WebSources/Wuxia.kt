package com.amgregoire.mangafeed.Common.WebSources


import com.amgregoire.mangafeed.Common.MangaEnums
import com.amgregoire.mangafeed.Common.RequestWrapper
import com.amgregoire.mangafeed.Common.SyncStatusObject
import com.amgregoire.mangafeed.Common.WebSources.Base.SourceNovel
import com.amgregoire.mangafeed.Models.Chapter
import com.amgregoire.mangafeed.Models.Manga
import com.amgregoire.mangafeed.Utils.MangaDB
import com.amgregoire.mangafeed.Utils.MangaLogger
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist
import java.util.*

/**
 * Created by amgregoi on 6/22/17.
 */
class Wuxia : SourceNovel()
{

    private val SourceKey = "Wuxia"
    private val mBaseUrl = "https://www.wuxiaworld.com"


    override fun getSourceName(): String
    {
        return SourceKey
    }

    override fun getBaseUrl(): String
    {
        return mBaseUrl
    }

    override fun getSourceType(): MangaEnums.SourceType
    {
        return MangaEnums.SourceType.NOVEL
    }

    override fun getRecentUpdatesUrl(): String
    {
        return "https://www.wuxiaworld.com/"
    }

    override fun getGenres(): Array<String>
    {
        return arrayOf()
    }

    override fun parseResponseToRecentList(aResponseBody: String): List<Manga>
    {

        val lNovelList = ArrayList<Manga>()

        try
        {
            val lParsedDocument = Jsoup.parse(aResponseBody)
            val lNovelBlocks = lParsedDocument.select("table.table.table-novels").select("tbody").select("tr")

            for (lNovel in lNovelBlocks)
            {
                var lMangaUrl = URL + lNovel.select("td").first().select("span.title").select("a").attr("href")
                val lMangaTitle = lNovel.select("td").first().select("span.title").select("a").text()

                lMangaUrl = lMangaUrl.replaceFirst(Manga.LinkRegex.toRegex(), "{$SourceKey}")
                var lManga: Manga? = MangaDB.getInstance().getManga(lMangaUrl)
                if (lManga == null)
                {
                    lManga = Manga(lMangaTitle, lMangaUrl, SourceKey)
                    MangaDB.getInstance().putManga(lManga)

                    updateMangaObservable(RequestWrapper(lManga))
                            .subscribe(
                                    { manga -> MangaLogger.logInfo(TAG, "Finished updating " + manga.title) },
                                    { throwable -> MangaLogger.logError(TAG, "Problem updating: " + throwable.message) }
                            )
                }

                if (!lNovelList.contains(lManga))
                {
                    lNovelList.add(lManga)
                }
            }

        }
        catch (aException: Exception)
        {
            MangaLogger.logError(TAG, " Failed to parse recent updates: ")
        }


        //Testing stuff out
        return lNovelList
    }

    override fun parseResponseToManga(request: RequestWrapper, responseBody: String): Manga?
    {
        try
        {
            val lParsedDocument = Jsoup.parse(responseBody)

            val lContentLeft = lParsedDocument.select("div.media.media-novel-index").select("div.media-left")
            val lContentRight = lParsedDocument.select("div.media.media-novel-index").select("div.media-body")

            val lManga = MangaDB.getInstance().getManga(request.manga.link)
            val lImage = lContentLeft.select("img.media-object").attr("src")

            val lDetails = StringBuilder()
            val lGenres = lContentRight.first().select("div.tags").select("a")
            for (lGenre in lGenres)
            {
                lDetails.append(lGenre.text())
                lDetails.append(", ")
            }

            if (lDetails.length == 0)
            {
                lDetails.append("N/A")
            }
            else
            {
                lDetails.setLength(lDetails.length - 2) // remove trailing ','
            }

            val lDesc = lContentRight.first().select("div.fr-view").select("p")
            var lDescription = ""
            for (desc in lDesc)
            {
                lDescription += desc.text() + "\n\n"
            }

            lManga.description = lDescription

            lManga.image = lImage

            MangaDB.getInstance().putManga(lManga)
            return lManga
        }
        catch (aException: Exception)
        {
            MangaLogger.logError(TAG, " Failed to update manga: ")
        }

        return null
    }

    override fun parseResponseToChapters(request: RequestWrapper, responseBody: String): List<Chapter>
    {
        val lChapterList = ArrayList<Chapter>()

        try
        {
            val lParsedDocument = Jsoup.parse(responseBody)
            val lContent = lParsedDocument.select("div#accordion.panel-group")
                    .select("li.chapter-item")
                    .select("a")

            var lCount = 1

            for (iChapter in lContent)
            {
                val lUrl = URL + iChapter.attr("href")
                val lChapterTitle = iChapter.text()
                lChapterList.add(Chapter(lUrl, request.manga.title, lChapterTitle, "-", lCount, request.manga.link, SourceKey))
                lCount++
            }
        }
        catch (aException: Exception)
        {
            MangaLogger.logError(TAG, aException.message)
        }

        Collections.reverse(lChapterList) //Descending order
        return lChapterList
    }

    override fun parseResponseToPageUrls(requestWrapper: RequestWrapper, aResponseBody: String): List<String>?
    {
        return null
    }

    override fun parseResponseToImageUrls(aResponseBody: String, aResponseUrl: String): String
    {
        try
        {
            val lParsedDocument = Jsoup.parse(aResponseBody)
            val lContent = lParsedDocument.select("div.panel.panel-default")
            return Jsoup.clean(lContent.toString(), Whitelist.relaxed())
        }
        catch (aException: Exception)
        {
            MangaLogger.logError(TAG, aException.message)
        }

        return "Failed to pull page."
    }

    override fun updateLocalCatalogV2(): Observable<SyncStatusObject>
    {
        val links = listOf("https://www.wuxiaworld.com/tag/completed", "https://www.wuxiaworld.com/language/chinese", "https://www.wuxiaworld.com/language/korean", "https://www.wuxiaworld.com/language/english")
        val pages = links.map { link -> updateCatalogObservable(link) }
        var counter = 0

        return Observable.fromIterable(pages)
                .subscribeOn(Schedulers.computation())
                .flatMap { obs -> obs.subscribeOn(Schedulers.computation()) }
                .map { response ->
                    counter++
                    Pair(counter, response)
                }
                .map { (count, response) -> Pair(count, convertCatalogPageToMangaList(response as String)) }
                .flatMap { (count, list) -> Observable.just(SyncStatusObject(count, links.size, list)) }

    }

    private fun convertCatalogPageToMangaList(response: String): List<Manga>
    {
        val result = arrayListOf<Manga>()
        val lDatabase = MangaDB.getInstance()
        val lParsedDocument = Jsoup.parse(response)
        val lItemGroups = lParsedDocument.select("ul.media-list.genres-list").select("li.media")

        for (group in lItemGroups)
        {

            val lImage = group.select("div.media-left").select("img.media-object").attr("href")
            val lTitle = group.select("div.media-body").select("div.media-heading").select("a").text()
            val lLink = URL + group.select("div.media-body").select("div.media-heading").select("a").attr("href")
            val lDescription = StringBuilder()

            val lDesc = group.select("div.media-body").select("p")

            for (desc in lDesc)
            {
                lDescription.append(desc.text()).append("\n\n")
            }

            var lNewManga = Manga(lTitle, lLink, SourceKey)
            if (!lDatabase.containsManga(lNewManga))
            {
                lNewManga.image = lImage
                lNewManga.description = lDescription.toString()
                lNewManga = lDatabase.putManga(lNewManga)
                result.add(lNewManga)
            }
        }

        return result.toList()
    }

    companion object
    {
        val TAG = Wuxia::class.java.simpleName
        val URL = "https://www.wuxiaworld.com"
    }
}
