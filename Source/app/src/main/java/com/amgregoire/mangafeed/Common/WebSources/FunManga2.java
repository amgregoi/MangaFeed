package com.amgregoire.mangafeed.Common.WebSources;


import com.amgregoire.mangafeed.Common.MangaEnums;
import com.amgregoire.mangafeed.Common.RequestWrapper;
import com.amgregoire.mangafeed.Common.WebSources.Base.SourceManga;
import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.Chapter;
import com.amgregoire.mangafeed.Models.Manga;
import com.amgregoire.mangafeed.Utils.MangaDB;
import com.amgregoire.mangafeed.Utils.MangaLogger;
import com.amgregoire.mangafeed.v2.service.Logger;
import com.google.gson.Gson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class FunManga2 extends SourceManga
{
    public final static String TAG = FunManga.class.getSimpleName();
    public final static String URL = "https://www.funmanga.com";

    private final String SourceKey = "FunManga";
    private final String mBaseUrl = "https://www.funmanga.com";
    private final String mUpdatesUrl = "https://funmanga.com/latest-chapters";
    private final String mCatalogUrl = "https://funmanga.com/manga-list/";
    private final String mGenres[] =
            {
                    "Joy",
                    "Action",
                    "Adult",
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
                    "Lolicon",
                    "Manga",
                    "Manhua",
                    "Manhwa",
                    "Martial Arts",
                    "Mature",
                    "Mecha",
                    "Mystery",
                    "One shot",
                    "Psychological",
                    "Romance",
                    "School Life",
                    "Sci fi",
                    "Seinen",
                    "Shotacon",
                    "Shoujo",
                    "Shoujo Ai",
                    "Shounen",
                    "Shounen Ai",
                    "Slice of Life",
                    "Smut",
                    "Sports",
                    "Supernatural",
                    "Tragedy",
                    "Yaoi",
                    "Yuri"
            };

    @Override
    public boolean requiresCloudFlare()
    {
        return true;
    }

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

    @Override
    public List<Manga> parseResponseToRecentList(final String responseBody)
    {
        List<Manga> lMangaList = new ArrayList<>();

        try
        {
            Document lParsedDocument = Jsoup.parse(responseBody);
            Elements lMangaElements = lParsedDocument.select("div.manga_updates").select("dl");

            for (Element iWholeElement : lMangaElements)
            {
                Document lParseSections = Jsoup.parse(iWholeElement.toString());
                Elements lUsefulElements = lParseSections.select("dt");
                for (Element iUsefulElement : lUsefulElements)
                {
                    String lMangaTitle = iUsefulElement.select("a").attr("title");
                    String lMangaUrl = iUsefulElement.select("a").attr("href");

                    if (lMangaUrl.charAt(lMangaUrl.length() - 1) != '/')
                    {
                        lMangaUrl += "/"; //add ending slash to url if missing
                    }

                    lMangaUrl = lMangaUrl.replaceFirst(Manga.Companion.getLinkRegex(), "{" + SourceKey + "}");
                    Manga lManga = MangaDB.getInstance().getManga(lMangaUrl);
                    if (lManga != null)
                    {
                        lMangaList.add(lManga);
                    }
                    else
                    {
                        lManga = MangaDB.getInstance().putManga(new Manga(lMangaTitle, lMangaUrl, SourceKey));
                        lMangaList.add(lManga);

                        updateMangaObservable(new RequestWrapper(lManga))
                                .subscribe
                                        (
                                                manga -> MangaLogger.logInfo(TAG, "Finished updating " + manga.getTitle()),
                                                throwable -> MangaLogger.logError(TAG, "Problem updating: " + throwable
                                                        .getMessage())
                                        );
                    }
                }
            }
        }
        catch (Exception aException)
        {
//            MangaLogger.logError(TAG, responseBody);
            MangaLogger.logError(TAG, " Failed to parse recent updates: " + aException.getMessage());
        }

        MangaLogger.logInfo(TAG, "Finished parsing recent updates");

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

        try
        {
            Element lImageElement = lHtml.body().select("img.img-responsive.mobile-img").first();
            Element lDescriptionElement = lHtml.body()
                                               .select("div.note.note-default.margin-top-15")
                                               .first();
            Elements lInfo = lHtml.body().select("dl.dl-horizontal").select("dd");

            String lImage = "";
            if (lImageElement != null && lImageElement.hasAttr("src")) lImage = lImageElement.attr("src");


            String lDescription = "";
            if (lDescriptionElement != null) lDescription = lDescriptionElement.text();
            String lAlternate = null;
            String lAuthor = null;
            String lArtist = null;
            String lGenres = null;
            String lStatus = null;


            if (lInfo != null)
            {
                for (int i = 0; i < lInfo.size(); i++)
                {
                    if (i == 0)
                    {
                        lAlternate = lInfo.get(i).text();
                    }
                    else if (i == 5)
                    {
                        lAuthor = lInfo.get(i).text();
                    }
                    else if (i == 4)
                    {
                        lArtist = lInfo.get(i).text();
                    }
                    else if (i == 2)
                    {
                        lGenres = lInfo.get(i).text();
                    }
                    else if (i == 1)
                    {
                        lStatus = lInfo.get(i).text();
                    }
                }
            }

            Manga lManga = MangaDB.getInstance().getManga(request.getManga().getLink());
            lManga.setAlternate(lAlternate);
            lManga.setImage(lImage);
            lManga.setDescription(lDescription);
            lManga.setArtist(lArtist);
            lManga.setAuthor(lAuthor);
            lManga.setGenres(lGenres);
            lManga.setStatus(lStatus);
            lManga.setSource(SourceKey);
            lManga.setLink(request.getManga().getLink());

            MangaDB.getInstance().putManga(lManga);

            MangaLogger.logInfo(TAG, "Finished creating/updating manga (" + lManga.getTitle() + ")");
        }
        catch (Exception aException)
        {
            MangaLogger.logError(TAG, request.getManga().getLink(), aException.getMessage());
            Logger.INSTANCE.error(aException, "");
        }

        return MangaDB.getInstance().getManga(request.getManga().getLink());
    }

    @Override
    public List<Chapter> parseResponseToChapters(RequestWrapper request, String responseBody)
    {
        Document lParsedDocument = Jsoup.parse(responseBody);
        List<Chapter> lChapterList = resolveChaptersFromParsedDocument(lParsedDocument, request);

        return lChapterList;
    }

    @Override
    public List<String> parseResponseToPageUrls(final RequestWrapper requestWrapper, final String responseBody)
    {
        List<String> lImages = new ArrayList<>();

        Document lDoc = Jsoup.parse(responseBody);
        Elements lNav = lDoc.select("h5.widget-heading").select("select").select("option");

        int lPages = lNav.size();

        for (int i = 1; i < lPages; i++)
        {
            String lLink = lNav.get(i).attr("value");
            lImages.add(lLink);
        }

        return lImages;
    }

    @Override
    public String parseResponseToImageUrls(final String responseBody, final String responseUrl)
    {
        Document lParsedDocument = Jsoup.parse(responseBody);
        String lLink = lParsedDocument.select("img.img-responsive").attr("src");

        return lLink;
    }

    @Override
    public void updateLocalCatalog()
    {
        List<Observable> pages = new ArrayList<>();
        char lEndPoint = ' ';

        while (lEndPoint != 'z')
        {
            pages.add(updateCatalogObservable(mCatalogUrl + lEndPoint));

            if (lEndPoint == ' ')
            {
                lEndPoint = 'a';
            }
            else
            {
                lEndPoint++;
            }
        }

        Single temp = Observable.fromIterable(pages)
                                .subscribeOn(Schedulers.computation())
                                .flatMap(obs -> obs.subscribeOn(Schedulers.computation()))
                                .flatMap(response -> test((String) response))
                                .toList();

        temp.subscribe(o -> {
            String x = new Gson().toJson(o);
            Logger.INSTANCE.error(x, "");
        }, new Consumer<Throwable>()
        {
            @Override
            public void accept(Throwable throwable) throws Exception
            {
                Logger.INSTANCE.error(throwable, "oh no");
            }
        });
    }


    public List<Manga> test(String response)
    {
        Logger.INSTANCE.error("Starting local catalog", "");
        ArrayList<Manga> result = new ArrayList<>();

        String responseBody = (String) response;
        MangaDB lDatabase = MangaDB.getInstance();
        Document lParsedDocument = Jsoup.parse(responseBody);
        Elements lLinks = lParsedDocument.select("ul.manga-list.circle-list").select("a");

        for (Element link : lLinks)
        {
            String name = link.text();
            String url = link.attr("href") + "/";

            Manga newManga = new Manga(name, url, SourceKey);
            if (!lDatabase.containsManga(newManga))
            {
                newManga = lDatabase.putManga(newManga);
                Logger.INSTANCE.info("Added new item -> " + newManga.getTitle());
                result.add(newManga);
            }
        }

        return result;
    }

    //    @Override
    public void updateLocalCatalog2()
    {
        char lEndPoint = ' ';

        while (lEndPoint != 'z')
        {

            updateCatalogObservable(mCatalogUrl + lEndPoint).subscribe(response ->
                    {
                        Logger.INSTANCE.error("Starting local catalog", "");
                        String responseBody = (String) response;
                        MangaDB lDatabase = MangaDB.getInstance();
                        Document lParsedDocument = Jsoup.parse(responseBody);
                        Elements lLinks = lParsedDocument.select("ul.manga-list.circle-list").select("a");

                        for (Element link : lLinks)
                        {
                            String name = link.text();
                            String url = link.attr("href") + "/";

                            Manga newManga = new Manga(name, url, SourceKey);
                            if (!lDatabase.containsManga(newManga))
                            {
                                newManga = lDatabase.putManga(newManga);
                                Logger.INSTANCE.info("Added new item -> " + newManga.getTitle());
                                // update new entry info

                                MangaFeed.Companion.getApp()
                                                   .getSourceByTag(TAG)
                                                   .updateMangaObservable(new RequestWrapper(newManga))
                                                   .subscribe(
                                                           manga -> Logger.INSTANCE.info("Finished updating (" + TAG + ") " + manga.getTitle()),
                                                           throwable -> MangaLogger.logError(TAG, "Problem updating: " + throwable
                                                                   .getMessage())
                                                   );
                            }
                        }
                    },
                    throwable -> MangaLogger.logError(TAG, throwable.toString()));

            if (lEndPoint == ' ')
            {
                lEndPoint = 'a';
            }
            else
            {
                lEndPoint++;
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
    private List<Chapter> resolveChaptersFromParsedDocument(final Document parsedDocument, final RequestWrapper request)
    {
        List<Chapter> lChapterList = new ArrayList<>();
        Elements lChapterElements = parsedDocument.select("ul.chapter-list").select("li");
        int lNumChapters = lChapterElements.size();

        String lChapterUrl, lChapterTitle, lChapterDate;

        for (Element iChapterElement : lChapterElements)
        {
            lChapterUrl = iChapterElement.select("a").attr("href");
            lChapterTitle = iChapterElement.select("span").first().text();
            lChapterDate = iChapterElement.select("span").get(1).text();

            lChapterList.add(new Chapter(lChapterUrl, request.getManga().getTitle(), lChapterTitle, lChapterDate, lNumChapters, request.getManga()
                                                                                                                                       .getLink(), SourceKey));

            lNumChapters--;
        }

        return lChapterList;
    }
}


