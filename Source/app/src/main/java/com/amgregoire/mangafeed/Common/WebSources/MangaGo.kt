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
import com.amgregoire.mangafeed.v2.model.domain.Manga
import com.amgregoire.mangafeed.v2.model.mappers.MangaToDbMangaMapper
import com.amgregoire.mangafeed.v2.model.mappers.DbMangaToMangaMapper
import com.amgregoire.mangafeed.v2.service.Logger
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.*

class MangaGo : SourceManga()
{

    private val mBaseUrl = "http://www.mangago.me"
    private val mUpdatesUrl = "http://www.mangago.me"
    private val mCatalogUrl = "http://www.mangago.me/list/directory/all/"
    private val mGenres = arrayOf("Yaoi", "Doujinshi", "Shonen Ai", "Shoujo", "Yuri", "Romance", "Fantasy", "Comedy", "Smut", "Adult", "School Life", "Mystery", "One Shot", "Ecchi", "Shounen", "Martial Arts", "Shoujo Ai", "Supernatural", "Drama", "Action", "Adventure", "Harem", "Historical", "Horror", "Josei", "Mature", "Mecha", "Psychological", "Sci-fi", "Seinen", "Slice Of Life", "Sports", "Gender Bender", "Tragedy", "Bara", "Shotacon", "Webtoons")

    override fun requiresCloudFlare(): Boolean
    {
        return false
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

    override fun parseResponseToRecentList(responseBody: String): List<Manga>?
    {
        val lMangaList = ArrayList<Manga>()

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

                    lMangaUrl = lMangaUrl.replaceFirst(DbManga.LinkRegex.toRegex(), "")
                    var manga: Manga? = localMangaRepository.getManga(lMangaUrl)
                    if (manga != null)
                    {
                        lMangaList.add(manga)
                    }
                    else
                    {
                        manga = localMangaRepository.putManga(DbManga(lMangaTitle, lMangaUrl, SourceKey)) ?: continue
                        lMangaList.add(manga)

                        updateMangaObservable(RequestWrapper(manga))
                                .subscribe(
                                        { manga -> MangaLogger.logInfo(TAG, "Finished updating " + manga.name) },
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

    override fun parseResponseToManga(request: RequestWrapper, responseBody: String): Manga?
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

            val lManga = localMangaRepository.getManga(request.manga.link)?.apply {
                alternateNames = lAlternate ?: ""
                image = lImage
                description = lDescription
                artists = lArtist ?: ""
                authors = lAuthor ?: ""
                genres = lGenres ?: ""
                status = lStatus ?: ""
                source = SourceKey
                link = request.manga.link

                localMangaRepository.putManga(this)
                MangaLogger.logInfo(TAG, "Finished creating/updating manga ($name)")
                return this
            }


        }
        catch (aException: Exception)
        {
            MangaLogger.logError(TAG, request.manga.link, aException.message)
            Logger.error(aException, "")
        }

        return null
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


    private fun convertCatalogPageToMangaList(response: String): List<Manga>
    {
        Logger.error("Starting local catalog", "")
        val result = ArrayList<DbManga>()

        val lDatabase = MangaDB.getInstance()
        val lParsedDocument = Jsoup.parse(response)
        val lLinks = lParsedDocument.select("ul.manga-list.circle-list").select("a")

        for (link in lLinks)
        {
            val name = link.text()
            var url = link.attr("href")
            url = url.replaceFirst(DbManga.LinkRegex.toRegex(), "")
            if (url[url.length - 1] != '/')
            {
                url += "/" //add ending slash to url if missing
            }

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
        val existing = arrayListOf<Manga>()

        for (list in links)
        {
            existing.addAll(localMangaRepository.getExistingManga(list, sourceName))
        }

        val dbMangaMapper = MangaToDbMangaMapper()
        val mangaMapper = DbMangaToMangaMapper()

        result.removeAll(existing.map { dbMangaMapper.map(it) })

        result.map { old ->
            Logger.info("Added new item -> " + old.link)
            localMangaRepository.putManga(old)
        }

        return result.map { mangaMapper.map(it) }.toList()
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

                    var dbManga = DbManga(name, url, SourceKey)
                    if (!localMangaRepository.containsManga(url, SourceKey))
                    {
                        localMangaRepository.putManga(dbManga)
                                ?.apply {
                                    Logger.info("Added new item -> " + name)
                                    // update new entry info

                                    MangaFeed.app
                                            .getSourceByTag(TAG)
                                            .updateMangaObservable(RequestWrapper(this))
                                            .subscribe(
                                                    { manga -> Logger.info("Finished updating (" + TAG + ") " + manga.name) },
                                                    { throwable ->
                                                        MangaLogger.logError(TAG, "Problem updating: " + throwable
                                                                .message)
                                                    }
                                            )
                                }

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

            lChapterList.add(DbChapter(lChapterUrl, request.manga.name, lChapterTitle, lChapterDate, lNumChapters, request.manga
                    .link, SourceKey))

            lNumChapters--
        }

        return lChapterList
    }

    companion object
    {
        val TAG = MangaGo::class.java.simpleName
        val URL = "http://www.mangago.me"
        val SourceKey = "MangaGo"
    }
}



