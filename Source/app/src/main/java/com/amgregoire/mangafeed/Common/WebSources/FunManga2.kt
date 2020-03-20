package com.amgregoire.mangafeed.Common.WebSources


import com.amgregoire.mangafeed.Common.MangaEnums
import com.amgregoire.mangafeed.Common.RequestWrapper
import com.amgregoire.mangafeed.Common.SyncStatusObject
import com.amgregoire.mangafeed.Common.WebSources.Base.SourceManga
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.Models.DbChapter
import com.amgregoire.mangafeed.Models.DbManga
import com.amgregoire.mangafeed.Utils.MangaDB
import com.amgregoire.mangafeed.Utils.MangaLogger
import com.amgregoire.mangafeed.v2.service.Logger
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.*

class FunManga : SourceManga()
{

    private val mBaseUrl = "https://www.funmanga.com"
    private val mUpdatesUrl = "https://funmanga.com/latest-chapters"
    private val mCatalogUrl = "https://funmanga.com/manga-list/"
    private val mGenres = arrayOf("Joy", "Action", "Adult", "Adventure", "Comedy", "Doujinshi", "Drama", "Ecchi", "Fantasy", "Gender Bender", "Harem", "Historical", "Horror", "Josei", "Lolicon", "Manga", "Manhua", "Manhwa", "Martial Arts", "Mature", "Mecha", "Mystery", "One shot", "Psychological", "Romance", "School Life", "Sci fi", "Seinen", "Shotacon", "Shoujo", "Shoujo Ai", "Shounen", "Shounen Ai", "Slice of Life", "Smut", "Sports", "Supernatural", "Tragedy", "Yaoi", "Yuri")

    override fun requiresCloudFlare(): Boolean
    {
        return true
    }

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
        return MangaEnums.SourceType.MANGA
    }

    override fun getRecentUpdatesUrl(): String
    {
        return mUpdatesUrl
    }

    override fun getGenres(): Array<String>
    {
        return mGenres
    }

    override fun parseResponseToRecentList(responseBody: String): List<DbManga>?
    {
        val lMangaList = ArrayList<DbManga>()

        try
        {
            val lParsedDocument = Jsoup.parse(responseBody)
            val lMangaElements = lParsedDocument.select("div.manga_updates").select("dl")

            for (iWholeElement in lMangaElements)
            {
                val lParseSections = Jsoup.parse(iWholeElement.toString())
                val lUsefulElements = lParseSections.select("dt")
                for (iUsefulElement in lUsefulElements)
                {
                    val lMangaTitle = iUsefulElement.select("a").attr("title")
                    var lMangaUrl = iUsefulElement.select("a").attr("href")

                    if (lMangaUrl[lMangaUrl.length - 1] != '/')
                    {
                        lMangaUrl += "/" //add ending slash to url if missing
                    }

                    lMangaUrl = lMangaUrl.replaceFirst(DbManga.LinkRegex.toRegex(), "{$SourceKey}")
                    var lDbManga: DbManga? = MangaDB.getInstance().getManga(lMangaUrl)
                    if (lDbManga != null)
                    {
                        lMangaList.add(lDbManga)
                    }
                    else
                    {
                        lDbManga = MangaDB.getInstance().putManga(DbManga(lMangaTitle, lMangaUrl, SourceKey))
                        lMangaList.add(lDbManga)

                        updateMangaObservable(RequestWrapper(lDbManga))
                                .subscribe(
                                        { manga -> MangaLogger.logInfo(TAG, "Finished updating " + manga.title) },
                                        { throwable ->
                                            MangaLogger.logError(TAG, "Problem updating: " + throwable
                                                    .message)
                                        }
                                )
                    }
                }
            }
        }
        catch (aException: Exception)
        {
            //            MangaLogger.logError(TAG, responseBody);
            MangaLogger.logError(TAG, " Failed to parse recent updates: " + aException.message)
        }

        MangaLogger.logInfo(TAG, "Finished parsing recent updates")

        return if (lMangaList.size == 0)
        {
            null
        }
        else lMangaList
    }

    override fun parseResponseToManga(request: RequestWrapper, responseBody: String): DbManga
    {
        val lHtml = Jsoup.parse(responseBody)

        try
        {
            val lImageElement = lHtml.body().select("img.img-responsive.mobile-img").first()
            val lDescriptionElement = lHtml.body()
                    .select("div.note.note-default.margin-top-15")
                    .first()
            val lInfo = lHtml.body().select("dl.dl-horizontal").select("dd")

            var lImage = ""
            if (lImageElement != null && lImageElement.hasAttr("src")) lImage = lImageElement.attr("src")


            var lDescription = ""
            if (lDescriptionElement != null) lDescription = lDescriptionElement.text()
            var lAlternate: String? = null
            var lAuthor: String? = null
            var lArtist: String? = null
            var lGenres: String? = null
            var lStatus: String? = null


            if (lInfo != null)
            {
                for (i in lInfo.indices)
                {
                    when (i)
                    {
                        0 -> lAlternate = lInfo[i].text()
                        5 -> lAuthor = lInfo[i].text()
                        4 -> lArtist = lInfo[i].text()
                        2 -> lGenres = lInfo[i].text()
                        1 -> lStatus = lInfo[i].text()
                    }
                }
            }

            val lManga = MangaDB.getInstance().getManga(request.manga.link)
            lManga.alternate = lAlternate
            lManga.image = lImage
            lManga.description = lDescription
            lManga.artist = lArtist
            lManga.author = lAuthor
            lManga.genres = lGenres
            lManga.status = lStatus
            lManga.source = SourceKey
            lManga.link = request.manga.link

            MangaDB.getInstance().putManga(lManga)

            MangaLogger.logInfo(TAG, "Finished creating/updating manga (" + lManga.title + ")")
        }
        catch (aException: Exception)
        {
            MangaLogger.logError(TAG, request.manga.link, aException.message)
            Logger.error(aException, "")
        }

        return MangaDB.getInstance().getManga(request.manga.link)
    }

    override fun parseResponseToChapters(request: RequestWrapper, responseBody: String): List<DbChapter>
    {
        val lParsedDocument = Jsoup.parse(responseBody)

        return resolveChaptersFromParsedDocument(lParsedDocument, request)
    }

    override fun parseResponseToPageUrls(requestWrapper: RequestWrapper, responseBody: String): List<String>
    {
        val lImages = ArrayList<String>()

        val lDoc = Jsoup.parse(responseBody)
        val lNav = lDoc.select("h5.widget-heading").select("select").select("option")

        val lPages = lNav.size

        for (i in 1 until lPages)
        {
            val lLink = lNav[i].attr("value")
            lImages.add(lLink)
        }

        return lImages
    }

    override fun parseResponseToImageUrls(responseBody: String, responseUrl: String): String
    {
        val lParsedDocument = Jsoup.parse(responseBody)

        return lParsedDocument.select("img.img-responsive").attr("src")
    }

    /***
     * New way to update catalog, provides feedback to ui to gage activity.
     * Currently only added for FunManga
     */
    override fun updateLocalCatalogV2(): Observable<SyncStatusObject>
    {
        val endPoints = listOf(' ', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z')
        val pages = endPoints.map { char -> updateCatalogObservable(mCatalogUrl + char) }
        var counter = 0
        return Observable.fromIterable(pages)
                .subscribeOn(Schedulers.computation())
                .flatMap { obs -> obs.subscribeOn(Schedulers.computation()) }
                .map { response ->
                    counter++
                    Pair(counter, response)
                }
                .map { (count, response) -> Pair(count, convertCatalogPageToMangaList(response as String)) }
                .flatMap { (count, list) -> Observable.just(SyncStatusObject(count, endPoints.size, list)) }
    }


    private fun convertCatalogPageToMangaList(response: String): List<DbManga>
    {
        Logger.error("Starting local catalog", "")
        val result = ArrayList<DbManga>()

        val lDatabase = MangaDB.getInstance()
        val lParsedDocument = Jsoup.parse(response)
        val lLinks = lParsedDocument.select("ul.manga-list.circle-list").select("a")

        for (link in lLinks)
        {
            val name = link.text()
            val url = link.attr("href") + "/"

            //            var newManga = Manga(name, url, SourceKey)
            //            if (!lDatabase.containsManga(newManga))
            //            {
            //                newManga = lDatabase.putManga(newManga)
            //                Logger.info("Added new item -> " + newManga.title)
            //                result.add(newManga)
            //            }
            result.add(DbManga(name, url, SourceKey))
        }

        val links = result.map { m -> m.link }.chunked(200)
        val existing = arrayListOf<DbManga>()

        for (list in links)
        {
            existing.addAll(lDatabase.getExistingManga(list, sourceName))
        }

        result.removeAll(existing)

        result.map { old ->
            Logger.info("Added new item -> " + old.title)
            lDatabase.putManga(old)
        }

        return result.toList()
    }

    override fun updateLocalCatalog()
    {
        var lEndPoint = ' '

        while (lEndPoint != 'z')
        {

            updateCatalogObservable(mCatalogUrl + lEndPoint).subscribe({ response ->
                Logger.error("Starting local catalog", "")
                val responseBody = response as String
                val lDatabase = MangaDB.getInstance()
                val lParsedDocument = Jsoup.parse(responseBody)
                val lLinks = lParsedDocument.select("ul.manga-list.circle-list").select("a")

                for (link in lLinks)
                {
                    val name = link.text()
                    val url = link.attr("href") + "/"

                    var newManga = DbManga(name, url, SourceKey)
                    if (!lDatabase.containsManga(newManga))
                    {
                        newManga = lDatabase.putManga(newManga)
                        Logger.info("Added new item -> " + newManga.title)
                        // update new entry info

                        MangaFeed.app
                                .getSourceByTag(TAG)
                                .updateMangaObservable(RequestWrapper(newManga))
                                .subscribe(
                                        { manga -> Logger.info("Finished updating (" + TAG + ") " + manga.title) },
                                        { throwable ->
                                            MangaLogger.logError(TAG, "Problem updating: " + throwable
                                                    .message)
                                        }
                                )
                    }
                }
            },
                    { throwable -> MangaLogger.logError(TAG, throwable.toString()) })

            if (lEndPoint == ' ')
            {
                lEndPoint = 'a'
            }
            else
            {
                lEndPoint++
            }
        }


    }

    /***
     * This helper function resolves chapters from the specified document and returns a list of chapters.
     * Parent - parseResponseToChapters();
     *
     * @param parsedDocument
     * @return
     */
    private fun resolveChaptersFromParsedDocument(parsedDocument: Document, request: RequestWrapper): List<DbChapter>
    {
        val lChapterList = ArrayList<DbChapter>()
        val lChapterElements = parsedDocument.select("ul.chapter-list").select("li")
        var lNumChapters = lChapterElements.size

        var lChapterUrl: String
        var lChapterTitle: String
        var lChapterDate: String

        for (iChapterElement in lChapterElements)
        {
            lChapterUrl = iChapterElement.select("a").attr("href")
            lChapterTitle = iChapterElement.select("span").first().text()
            lChapterDate = iChapterElement.select("span")[1].text()

            lChapterList.add(DbChapter(lChapterUrl, request.manga.title, lChapterTitle, lChapterDate, lNumChapters, request.manga
                    .link, SourceKey))

            lNumChapters--
        }

        return lChapterList
    }

    companion object
    {
        val TAG = FunManga::class.java.simpleName
        val URL = "https://www.funmanga.com"
        val SourceKey = "FunManga"
    }
}



