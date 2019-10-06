package com.amgregoire.mangafeed.Common.WebSources;


import com.amgregoire.mangafeed.Common.MangaEnums;
import com.amgregoire.mangafeed.Common.RequestWrapper;
import com.amgregoire.mangafeed.Common.WebSources.Base.SourceManga;
import com.amgregoire.mangafeed.Models.Chapter;
import com.amgregoire.mangafeed.Models.Manga;
import com.amgregoire.mangafeed.Utils.MangaDB;
import com.amgregoire.mangafeed.Utils.MangaLogger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;


public class MangaHere extends SourceManga
{
    final public static String TAG = MangaHere.class.getSimpleName();
    final public static String URL = "mangahere";

    final private String SourceKey = "MangaHere";
    final private String mBaseUrl = "http://mangahere.cc";
    final private String mUpdatesUrl = "http://mangahere.cc/latest/";
    final private String mGenres[] = {
            "Action",
            "Adventure",
            "Comedy",
            "Doujinshi",
            "Drama",
            "Ecchi",
            "Fantasy",
            "Gender Bender",
            "Harem",
            "Historical",
            "Horror",
            "Josei",
            "Martial Arts",
            "Mature",
            "Mecha",
            "Mystery",
            "One Shot",
            "Psychological",
            "Romance",
            "School Life",
            "Sci-fi",
            "Seinen",
            "Shoujo",
            "Shoujo Ai",
            "Shounen",
            "Shounen Ai",
            "Slice of Life",
            "Sports",
            "Supernatural",
            "Tragedy",
            "Yaoi",
            "Yuri"
    };

    @Override
    public String getSourceName()
    {
        return SourceKey;
    }

    @Override
    public String getBaseUrl()
    {
        return mBaseUrl;
    }

    @Override
    public MangaEnums.SourceType getSourceType()
    {
        return MangaEnums.SourceType.MANGA;
    }

    @Override
    public String getRecentUpdatesUrl()
    {
        return mUpdatesUrl;
    }

    @Override
    public String[] getGenres()
    {
        return mGenres;
    }

    public List<Manga> parseResponseToRecentList(final String aResponseBody)
    {
        List<Manga> lMangaList = new ArrayList<>();

        Document lParsedDocument = Jsoup.parse(aResponseBody);
        Elements lMangaElements = lParsedDocument.select("ul.manga-list-4-list.line").select("p.manga-list-4-item-title");

        for (Element iWholeElement : lMangaElements)
        {

            String lMangaTitle = iWholeElement.select("a").attr("title");
            String lMangaUrl = iWholeElement.select("a").attr("href");

            lMangaUrl = "{" + SourceKey + "}" + lMangaUrl;
            Manga lManga = MangaDB.getInstance().getManga(lMangaUrl);
            if (lManga != null)
            {
                lMangaList.add(lManga);
            }
            else
            {
                lManga = new Manga(lMangaTitle, lMangaUrl, SourceKey);
                lMangaList.add(lManga);
                MangaDB.getInstance().putManga(lManga);
                updateMangaObservable(new RequestWrapper(lManga))
                        .subscribe
                                (
                                        manga -> MangaLogger.logInfo(TAG, "Finished updating " + manga.getTitle()),
                                        throwable -> MangaLogger.logError(TAG, "Problem updating: " + throwable.getMessage())
                                );
            }

        }

        MangaLogger.logInfo(TAG, " Finished parsing recent updates");

        if (lMangaList.size() == 0)
        {
            return null;
        }
        return lMangaList;
    }

    @Override
    public Manga parseResponseToManga(final RequestWrapper request, final String responseBody)
    {
        Document lHtml = Jsoup.parse(responseBody);
        Elements lUsefulSection = lHtml.select("div.detail-info");

        //image url
        Element lImageElement = lUsefulSection.select("img.detail-info-cover-img").first();
        //summary
        Elements lDescriptionElement = lUsefulSection.select("p.detail-info-right-content");


        String lImage = lImageElement.attr("src");
        String lDescription = lDescriptionElement.text();
        String lAuthor = null;
        String lArtist = null;
        String lGenres = "";
        String lStatus = null;

        // Genres
        Elements lGenreElements = lUsefulSection.select("p.detail-info-right-tag-list").select("a");
        for (Element element : lGenreElements)
        {
            lGenres += element.attr("title") + ", ";
        }
        if (!lGenres.isEmpty()) lGenres = lGenres.substring(0, lGenres.length() - 2);


        Manga lNewManga = MangaDB.getInstance().getManga(request.getManga().getLink());
        lNewManga.setImage(lImage);
        lNewManga.setDescription(lDescription);
        lNewManga.setGenres(lGenres);
        lNewManga.setSource(SourceKey);
        lNewManga.setLink(request.getManga().getLink());


        MangaDB.getInstance().putManga(lNewManga);
        MangaLogger.logInfo(TAG, "Finished creating/update manga (" + lNewManga.getTitle() + ")");
        return MangaDB.getInstance().getManga(request.getManga().getLink());
    }

    @Override
    public List<Chapter> parseResponseToChapters(RequestWrapper request, String resposneBody)
    {
        Document lParsedDocument = Jsoup.parse(resposneBody);
        Elements lUpdates = lParsedDocument.select("ul.detail-main-list");
        lParsedDocument = Jsoup.parse(lUpdates.toString());
        List<Chapter> lChapterList = resolveChaptersFromParsedDocument(lParsedDocument, request);

        return lChapterList;
    }

    @Override
    public List<String> parseResponseToPageUrls(final RequestWrapper requestWrapper, final String responseBody)
    {
        List<String> lPageUrls = new ArrayList<>();

        //get base url for images
        Document lParsedDocumentForImage = Jsoup.parse(responseBody);
        Elements lImageUpdates = lParsedDocumentForImage.select("div.pager-list.cp-pager-list").select("span").select("a");

        for (Element iUrl : lImageUpdates)
        {
            if (iUrl.text().equals(">")) continue;
            String page = iUrl.text();
            lPageUrls.add(requestWrapper.getChapter().getUrl().replaceFirst("[0-9]+.html", page + ".html"));
        }

        return lPageUrls;
    }

    @Override
    public String parseResponseToImageUrls(final String responseBody, final String responseUrl)
    {
        Document lParsedDocumentForImage = Jsoup.parse(responseBody);
        String lUrl = lParsedDocumentForImage.select("img.reader-main-img").attr("src").replaceFirst("//", "");
        return lUrl;
    }

    /***
     * This helper function resolves chapters from the specified document.
     * Parent - parseResponseToChapters();
     *
     * @param parsedDocument
     * @return
     */
    private List<Chapter> resolveChaptersFromParsedDocument(final Document parsedDocument, final RequestWrapper request)
    {
        List<Chapter> lChapterList = new ArrayList<>();
        Elements lChapterElements = parsedDocument.getElementsByTag("li");
        int lNumChapters = lChapterElements.size();

        for (Element iChapterElement : lChapterElements)
        {
            String lChapterUrl = iChapterElement.select("a").attr("href");
            String lChapterTitle = iChapterElement.select("p.title3").text();
            String lChapterDate = iChapterElement.select("p.title2").text();

            lChapterUrl = MangaEnums.Source.valueOf(SourceKey).getBaseUrl() + lChapterUrl;

            Chapter lCurChapter = new Chapter(lChapterUrl, request.getManga().getTitle(), lChapterTitle, lChapterDate, lNumChapters, request.getManga()
                                                                                                                                            .getLink(), SourceKey);
            lNumChapters--;

            lChapterList.add(lCurChapter);
        }

        MangaLogger.logInfo(TAG, " Finished parsing chapter list (" + request.getManga().getLink() + ")");
        return lChapterList;
    }
}