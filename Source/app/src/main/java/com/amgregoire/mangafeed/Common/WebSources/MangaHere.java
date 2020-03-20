package com.amgregoire.mangafeed.Common.WebSources;


import com.amgregoire.mangafeed.Common.MangaEnums;
import com.amgregoire.mangafeed.Common.RequestWrapper;
import com.amgregoire.mangafeed.Common.WebSources.Base.SourceManga;
import com.amgregoire.mangafeed.Models.DbChapter;
import com.amgregoire.mangafeed.Models.DbManga;
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

    public List<DbManga> parseResponseToRecentList(final String aResponseBody)
    {
        List<DbManga> lDbMangaList = new ArrayList<>();

        Document lParsedDocument = Jsoup.parse(aResponseBody);
        Elements lMangaElements = lParsedDocument.select("ul.manga-list-4-list.line").select("p.manga-list-4-item-title");

        for (Element iWholeElement : lMangaElements)
        {

            String lMangaTitle = iWholeElement.select("a").attr("title");
            String lMangaUrl = iWholeElement.select("a").attr("href");

            lMangaUrl = "{" + SourceKey + "}" + lMangaUrl;
            DbManga lDbManga = MangaDB.getInstance().getManga(lMangaUrl);
            if (lDbManga != null)
            {
                lDbMangaList.add(lDbManga);
            }
            else
            {
                lDbManga = new DbManga(lMangaTitle, lMangaUrl, SourceKey);
                lDbMangaList.add(lDbManga);
                MangaDB.getInstance().putManga(lDbManga);
                updateMangaObservable(new RequestWrapper(lDbManga))
                        .subscribe
                                (
                                        manga -> MangaLogger.logInfo(TAG, "Finished updating " + manga.getTitle()),
                                        throwable -> MangaLogger.logError(TAG, "Problem updating: " + throwable.getMessage())
                                );
            }

        }

        MangaLogger.logInfo(TAG, " Finished parsing recent updates");

        if (lDbMangaList.size() == 0)
        {
            return null;
        }
        return lDbMangaList;
    }

    @Override
    public DbManga parseResponseToManga(final RequestWrapper request, final String responseBody)
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


        DbManga lNewDbManga = MangaDB.getInstance().getManga(request.getManga().getLink());
        lNewDbManga.setImage(lImage);
        lNewDbManga.setDescription(lDescription);
        lNewDbManga.setGenres(lGenres);
        lNewDbManga.setSource(SourceKey);
        lNewDbManga.setLink(request.getManga().getLink());


        MangaDB.getInstance().putManga(lNewDbManga);
        MangaLogger.logInfo(TAG, "Finished creating/update manga (" + lNewDbManga.getTitle() + ")");
        return MangaDB.getInstance().getManga(request.getManga().getLink());
    }

    @Override
    public List<DbChapter> parseResponseToChapters(RequestWrapper request, String resposneBody)
    {
        Document lParsedDocument = Jsoup.parse(resposneBody);
        Elements lUpdates = lParsedDocument.select("ul.detail-main-list");
        lParsedDocument = Jsoup.parse(lUpdates.toString());
        List<DbChapter> lDbChapterList = resolveChaptersFromParsedDocument(lParsedDocument, request);

        return lDbChapterList;
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
    private List<DbChapter> resolveChaptersFromParsedDocument(final Document parsedDocument, final RequestWrapper request)
    {
        List<DbChapter> lDbChapterList = new ArrayList<>();
        Elements lChapterElements = parsedDocument.getElementsByTag("li");
        int lNumChapters = lChapterElements.size();

        for (Element iChapterElement : lChapterElements)
        {
            String lChapterUrl = iChapterElement.select("a").attr("href");
            String lChapterTitle = iChapterElement.select("p.title3").text();
            String lChapterDate = iChapterElement.select("p.title2").text();

            lChapterUrl = MangaEnums.Source.valueOf(SourceKey).getBaseUrl() + lChapterUrl;

            DbChapter lCurDbChapter = new DbChapter(lChapterUrl, request.getManga().getTitle(), lChapterTitle, lChapterDate, lNumChapters, request.getManga()
                                                                                                                                                  .getLink(), SourceKey);
            lNumChapters--;

            lDbChapterList.add(lCurDbChapter);
        }

        MangaLogger.logInfo(TAG, " Finished parsing chapter list (" + request.getManga().getLink() + ")");
        return lDbChapterList;
    }
}