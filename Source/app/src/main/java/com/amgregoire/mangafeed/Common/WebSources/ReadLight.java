package com.amgregoire.mangafeed.Common.WebSources;


import com.amgregoire.mangafeed.Common.RequestWrapper;
import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.Chapter;
import com.amgregoire.mangafeed.Models.Manga;
import com.amgregoire.mangafeed.Common.MangaEnums;
import com.amgregoire.mangafeed.Utils.MangaDB;
import com.amgregoire.mangafeed.Utils.MangaLogger;
import com.amgregoire.mangafeed.Common.WebSources.Base.SourceNovel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by amgregoi on 6/22/17.
 */
public class ReadLight extends SourceNovel
{
    public final static String TAG = ReadLight.class.getSimpleName();
    final public static String URL = "readlight";

    private final String SourceKey = "ReadLight";


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
        return "https://www.readlightnovel.org/";
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
            Elements lNovelBlocks = lParsedDocument.select("div.novel-block");


            for (Element lNovel : lNovelBlocks)
            {
                Document lMenuItems = Jsoup.parse(lNovel.toString());
                String lMangaUrl = lMenuItems.select("div.novel-cover").select("a").attr("href");
                String lMangaTitle = lMenuItems.select("div.novel-title").select("a").text();
                String lThumb = lMenuItems.select("div.novel-cover").select("img").attr("src");
                lMangaTitle = lMangaTitle.replaceFirst(" Ch. \\d+", "");

                Manga lManga = MangaDB.getInstance().getManga(lMangaUrl);
                if (lManga == null)
                {
                    lManga = new Manga(lMangaTitle, lMangaUrl, SourceKey);
                    lManga.setPicUrl(lThumb);
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

            Elements lContentLeft = lParsedDocument.select("div.novel").select("div.novel-left");
            Elements lContentRight = lParsedDocument.select("div.novel").select("div.novel-right");
            Elements lNovelDetailsLeft = lContentLeft.select("div.novel-details")
                                                     .select("div.novel-detail-item");
            Elements lNovelDetailsRight = lContentRight.select("div.novel-details")
                                                       .select("div.novel-detail-item");


            Manga lManga = MangaDB.getInstance().getManga(request.getMangaUrl());
            String lImage = lContentLeft.select("div.novel-cover").select("img").attr("src");

            StringBuilder lDetails = new StringBuilder();
            for (int i = 0; i < lNovelDetailsLeft.size(); i++)
            {
                Elements lGenres = lNovelDetailsLeft.get(i).select("div.novel-detail-body");
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

                switch (i)
                {
                    case 0: //Type
                    case 2: //Tags
                    case 3: //Language
                    case 6: //Year
                    default:
                        break;
                    case 1: //Genre
                        lManga.setGenres(lDetails.toString());
                        break;
                    case 4: //Author
                        lManga.setAuthor(lDetails.toString());
                        break;
                    case 5: //Artist
                        lManga.setArtist(lDetails.toString());
                        break;
                    case 7: //Status
                        lManga.setStatus(lDetails.toString());
                        break;
                }

                lDetails.setLength(0);
            }

            for (int i = 0; i < lNovelDetailsRight.size(); i++)
            {

                Elements lDetailBlocks = lNovelDetailsRight.get(i).select("div.novel-detail-body");
                for (Element lDetailBlock : lDetailBlocks)
                {
                    lDetails.append(lDetailBlock.text());
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

                switch (i)
                {
                    case 0: //??
                    case 3: //Related Series
                    case 4: //You may also
                    default:
                        break;
                    case 1: //Description
                        lManga.setDescription(lDetails.toString());
                        break;
                    case 2: //Alt Names
                        lManga.setAlternate(lDetails.toString());
                        break;
                }

                lDetails.setLength(0);
            }

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
            Elements lContent = lParsedDocument.select("div.col-lg-12.chapters")
                                               .select("div.tab-content")
                                               .select("a");

            int lCount = 1;

            for (Element iChapter : lContent)
            {
                String lUrl = iChapter.attr("href");
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
            Elements lContent = lParsedDocument.select("div.chapter-content3");
            lContent.select("div.row").first().remove();

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
        String lCatalogUrl = "https://www.readlightnovel.org/novel-list";
        updateCatalogObservable(lCatalogUrl).subscribe(o ->
                {
                    String responseBody = (String) o;
                    MangaDB lDatabase = MangaDB.getInstance();
                    Document lParsedDocument = Jsoup.parse(responseBody);
                    Elements lItemGroups = lParsedDocument.select("div.list-by-word-body");

                    for (Element group : lItemGroups)
                    {
                        Elements lItems = group.select("li");

                        for (Element item : lItems)
                        {
                            String name = item.select("a").first().text();
                            String url = item.select("a").first().attr("href");

                            if (!lDatabase.containsManga(url))
                            {
                                Manga lNewManga = new Manga(name, url, SourceKey);
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

                    }
                },
                throwable -> MangaLogger.logError(TAG, throwable.toString()));
    }
}
