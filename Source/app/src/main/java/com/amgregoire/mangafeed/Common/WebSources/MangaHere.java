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
    final private String mBaseUrl = "http://mangahere.co/";
    final private String mUpdatesUrl = "http://mangahere.co/latest/";
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
        Document lParsedDocument = Jsoup.parse(aResponseBody);
        Elements lUpdates = lParsedDocument.select("div.manga_updates");
        lParsedDocument = Jsoup.parse(lUpdates.toString());
        List<Manga> lMangaList = resolveMangaFromRecentDocument(lParsedDocument);

        return lMangaList;
    }

    @Override
    public Manga parseResponseToManga(final RequestWrapper request, final String responseBody)
    {
        Document lHtml = Jsoup.parse(responseBody);
        Elements lUsefulSection = lHtml.select("div.manga_detail_top.clearfix");

        //image url
        Element lImageElement = lUsefulSection.select("img").first();
        //summary
        Elements lHeaderInfo = lUsefulSection.select("ul.detail_topText").select("li");


        if (lImageElement != null && lHeaderInfo != null)
        {
            String lImage = lImageElement.attr("src");
            String lDescription = null;
            String lAlternate = null;
            String lAuthor = null;
            String lArtist = null;
            String lGenres = null;
            String lStatus = null;

            for (int i = 0; i < lHeaderInfo.size(); i++)
            {
                if (i == 2)
                {
                    lAlternate = lHeaderInfo.get(i)
                                            .text()
                                            .replace(lHeaderInfo.get(i).select("label").text(), "");
                }
                else if (i == 4)
                {
                    lAuthor = lHeaderInfo.get(i)
                                         .text()
                                         .replace(lHeaderInfo.get(i).select("label").text(), "");
                }
                else if (i == 5)
                {
                    lArtist = lHeaderInfo.get(i)
                                         .text()
                                         .replace(lHeaderInfo.get(i).select("label").text(), "");
                }
                else if (i == 3)
                {
                    lGenres = lHeaderInfo.get(i)
                                         .text()
                                         .replace(lHeaderInfo.get(i).select("label").text(), "");
                }
                else if (i == 6)
                {
                    lStatus = lHeaderInfo.get(i)
                                         .text()
                                         .replace(lHeaderInfo.get(i).select("label").text(), "");
                }
                else if (i == 8)
                {
                    lDescription = lHeaderInfo.get(i).text();
                }
            }

            Manga lNewManga = MangaDB.getInstance().getManga(request.getMangaUrl());
            lNewManga.setAlternate(lAlternate);
            lNewManga.setPicUrl(lImage);
            lNewManga.setDescription(lDescription);
            lNewManga.setArtist(lArtist);
            lNewManga.setAuthor(lAuthor);
            lNewManga.setmGenre(lGenres);
            lNewManga.setStatus(lStatus);
            lNewManga.setSource(SourceKey);
            lNewManga.setMangaUrl(request.getMangaUrl());


            MangaDB.getInstance().putManga(lNewManga);
            MangaLogger.logInfo(TAG, "Finished creating/update manga (" + lNewManga.getTitle() + ")");
            return MangaDB.getInstance().getManga(request.getMangaUrl());
        }

        return null;
    }

    @Override
    public List<Chapter> parseResponseToChapters(RequestWrapper request, String resposneBody)
    {
        Document lParsedDocument = Jsoup.parse(resposneBody);
        Elements lUpdates = lParsedDocument.select("div.detail_list")
                                           .select("ul")
                                           .not("ul.tab_comment.clearfix");
        lParsedDocument = Jsoup.parse(lUpdates.toString());
        List<Chapter> lChapterList = resolveChaptersFromParsedDocument(lParsedDocument, request);

        return lChapterList;
    }

    @Override
    public List<String> parseResponseToPageUrls(final String responseBody)
    {
        List<String> lPageUrls = new ArrayList<>();

        //get base url for images
        Document lParsedDocumentForImage = Jsoup.parse(responseBody);
        Elements lImageUpdates = lParsedDocumentForImage.select("select.wid60")
                                                        .first()
                                                        .select("option");

        for (Element iUrl : lImageUpdates)
        {
            lPageUrls.add(iUrl.attr("value"));
        }

        return lPageUrls;
    }

    @Override
    public String parseResponseToImageUrls(final String responseBody, final String responseUrl)
    {
        Document lParsedDocumentForImage = Jsoup.parse(responseBody);
        String lUrl = lParsedDocumentForImage.select("section#viewer.read_img")
                                             .select("img#image")
                                             .attr("src");

        return lUrl;
    }

    /***
     * This helper function resolves chapters from the specified document.
     * Parent - parseResponseToChapters();
     *
     * @param parsedDocument
     * @param title
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
            String lChapterTitle = iChapterElement.select("span.left").text();
            String lChapterDate = iChapterElement.select("span.right").text();

            Chapter lCurChapter = new Chapter(lChapterUrl, request.getMangaTitle(), lChapterTitle, lChapterDate, lNumChapters, request.getMangaUrl());
            lNumChapters--;

            lChapterList.add(lCurChapter);
        }

        MangaLogger.logInfo(TAG, " Finished parsing chapter list (" + request.getMangaTitle() + ")");
        return lChapterList;
    }

    /***
     * This helper function resolves manga objects from the specified document.
     * Parent - parseResponseToRecentList
     * @param parsedDocument
     * @return
     */
    private List<Manga> resolveMangaFromRecentDocument(final Document parsedDocument)
    {
        List<Manga> lMangaList = new ArrayList<>();
        Elements lMangaElements = parsedDocument.select("dl");

        for (Element iWholeElement : lMangaElements)
        {
            Document lParseSections = Jsoup.parse(iWholeElement.toString());
            Elements lUsefulElements = lParseSections.select("dt");
            for (Element iUsefulElement : lUsefulElements)
            {
                String lMangaTitle = iUsefulElement.select("a").attr("rel");
                String lMangaUrl = iUsefulElement.select("a").attr("href");

                lMangaUrl = lMangaUrl.replace("//", "http://");

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
                                            manga -> MangaLogger.logInfo(TAG, "Finished updating " + manga.title),
                                            throwable -> MangaLogger.logError(TAG, "Problem updating: " + throwable                                                    .getMessage())
                                    );
                }
            }
        }

        MangaLogger.logInfo(TAG, " Finished parsing recent updates");

        if (lMangaList.size() == 0)
        {
            return null;
        }
        return lMangaList;
    }
}