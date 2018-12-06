package com.amgregoire.mangafeed.Common.WebSources;


import com.amgregoire.mangafeed.Common.MangaEnums;
import com.amgregoire.mangafeed.Common.RequestWrapper;
import com.amgregoire.mangafeed.Common.WebSources.Base.SourceNovel;
import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.Chapter;
import com.amgregoire.mangafeed.Models.Manga;
import com.amgregoire.mangafeed.Utils.MangaDB;
import com.amgregoire.mangafeed.Utils.MangaLogger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by amgregoi on 6/22/17.
 */
public class Wuxia extends SourceNovel
{
    public final static String TAG = Wuxia.class.getSimpleName();
    final public static String URL = "https://www.wuxiaworld.com";

    private final String SourceKey = "Wuxia";


    @Override
    public String getSourceName()
    {
        return SourceKey;
    }

    @Override
    public MangaEnums.SourceType getSourceType()
    {
        return MangaEnums.SourceType.NOVEL;
    }

    @Override
    public String getRecentUpdatesUrl()
    {
        return "https://www.wuxiaworld.com/";
    }

    @Override
    public String[] getGenres()
    {
        return new String[0];
    }

    @Override
    public List<Manga> parseResponseToRecentList(String aResponseBody)
    {

        List<Manga> lNovelList = new ArrayList<>();

        try
        {
            Document lParsedDocument = Jsoup.parse(aResponseBody);
            Elements lNovelBlocks = lParsedDocument.select("table.table.table-novels").select("tbody").select("tr");

            for (Element lNovel : lNovelBlocks)
            {
                String lMangaUrl = URL + lNovel.select("td").first().select("span.title").select("a").attr("href");
                String lMangaTitle = lNovel.select("td").first().select("span.title").select("a").text();
                Manga lManga = MangaDB.getInstance().getManga(lMangaUrl);
                if (lManga == null)
                {
                    lManga = new Manga(lMangaTitle, lMangaUrl, SourceKey);
                    MangaDB.getInstance().putManga(lManga);

                    updateMangaObservable(new RequestWrapper(lManga))
                            .subscribe
                                    (
                                            manga -> MangaLogger.logInfo(TAG, "Finished updating " + manga.title),
                                            throwable -> MangaLogger.logError(TAG, "Problem updating: " + throwable.getMessage())
                                    );
                }

                if (!lNovelList.contains(lManga))
                {
                    lNovelList.add(lManga);
                }
            }

        }
        catch (Exception aException)
        {
            MangaLogger.logError(TAG, " Failed to parse recent updates: ");
        }


        //Testing stuff out
        return lNovelList;
    }

    @Override
    public Manga parseResponseToManga(RequestWrapper request, String responseBody)
    {
        try
        {
            Document lParsedDocument = Jsoup.parse(responseBody);

            Elements lContentLeft = lParsedDocument.select("div.media.media-novel-index").select("div.media-left");
            Elements lContentRight = lParsedDocument.select("div.media.media-novel-index").select("div.media-body");

            Manga lManga = MangaDB.getInstance().getManga(request.getMangaUrl());
            String lImage = lContentLeft.select("img.media-object").attr("src");

            StringBuilder lDetails = new StringBuilder();
            Elements lGenres = lContentRight.first().select("div.tags").select("a");
            for (Element lGenre : lGenres)
            {
                lDetails.append(lGenre.text());
                lDetails.append(", ");
            }

            if (lDetails.length() == 0)
            {
                lDetails.append("N/A");
            }
            else
            {
                lDetails.setLength(lDetails.length() - 2); // remove trailing ','
            }

            Elements lDesc = lContentRight.first().select("div.fr-view").select("p");
            String lDescription = "";
            for (Element desc : lDesc)
            {
                lDescription += desc.text() + "\n\n";
            }

            lManga.setDescription(lDescription);

            lManga.setPicUrl(lImage);

            MangaDB.getInstance().putManga(lManga);
            return lManga;
        }
        catch (Exception aException)
        {
            MangaLogger.logError(TAG, " Failed to update manga: ");
        }

        return null;
    }

    @Override
    public List<Chapter> parseResponseToChapters(RequestWrapper request, String responseBody)
    {
        List<Chapter> lChapterList = new ArrayList<>();

        try
        {
            Document lParsedDocument = Jsoup.parse(responseBody);
            Elements lContent = lParsedDocument.select("div#accordion.panel-group")
                                               .select("li.chapter-item")
                                               .select("a");

            int lCount = 1;

            for (Element iChapter : lContent)
            {
                String lUrl = URL + iChapter.attr("href");
                String lChapterTitle = iChapter.text();
                lChapterList.add(new Chapter(lUrl, request.getMangaTitle(), lChapterTitle, "-", lCount, request.getMangaUrl()));
                lCount++;
            }
        }
        catch (Exception aException)
        {
            MangaLogger.logError(TAG, aException.getMessage());
        }

        Collections.reverse(lChapterList); //Descending order
        return lChapterList;
    }

    @Override
    public List<String> parseResponseToPageUrls(String aResponseBody)
    {
        return null;
    }

    @Override
    public String parseResponseToImageUrls(String aResponseBody, String aResponseUrl)
    {
        try
        {
            Document lParsedDocument = Jsoup.parse(aResponseBody);
            Elements lContent = lParsedDocument.select("div.panel.panel-default");
            return Jsoup.clean(lContent.toString(), Whitelist.relaxed());
        }
        catch (Exception aException)
        {
            MangaLogger.logError(TAG, aException.getMessage());
        }

        return "Failed to pull page.";
    }

    @Override
    public void updateLocalCatalog()
    {

        String[] lLinks = {"https://www.wuxiaworld.com/tag/completed",
                "https://www.wuxiaworld.com/language/chinese",
                "https://www.wuxiaworld.com/language/korean",
                "https://www.wuxiaworld.com/language/english"};

        Observable.create((ObservableOnSubscribe<String>) emitter ->
        {
            try
            {
                for(String link : lLinks)
                {
                    emitter.onNext(link);
                }
            }
            catch (Exception ex)
            {
                emitter.onError(ex);
            }
            emitter.onComplete();
        }).subscribe(link -> updateCatalogObservable(link).subscribe(o ->
                {
                    String responseBody = (String) o;
                    MangaDB lDatabase = MangaDB.getInstance();
                    Document lParsedDocument = Jsoup.parse(responseBody);
                    Elements lItemGroups = lParsedDocument.select("ul.media-list.genres-list").select("li.media");

                    for (Element group : lItemGroups)
                    {

                        String lImage = group.select("div.media-left").select("img.media-object").attr("href");

                        String lTitle = group.select("div.media-body").select("div.media-heading").select("a").text();
                        String lLink = URL + group.select("div.media-body").select("div.media-heading").select("a").attr("href");
                        StringBuilder lDescription = new StringBuilder();

                        Elements lDesc = group.select("div.media-body").select("p");

                        for(Element desc : lDesc)
                        {
                            lDescription.append(desc.text()).append("\n\n");
                        }

                        if (!lDatabase.containsManga(lLink))
                        {
                            Manga lNewManga = new Manga(lTitle, lLink, SourceKey);
                            lNewManga.setImage(lImage);
                            lNewManga.setDescription(lDescription.toString());

                            lDatabase.putManga(lNewManga);
                            // update new entry info
                            MangaFeed.getInstance()
                                     .getSourceByTag(TAG)
                                     .updateMangaObservable(new RequestWrapper(lNewManga))
                                     .subscribe(manga -> MangaLogger.logInfo(TAG, "Finished updating (" + TAG + ") " + manga.title),
                                             throwable -> MangaLogger.logError(TAG, "Problem updating: " + throwable
                                                     .getMessage()));
                        }

                    }
                },
                throwable -> MangaLogger.logError(TAG, throwable.toString())));
    }
}
