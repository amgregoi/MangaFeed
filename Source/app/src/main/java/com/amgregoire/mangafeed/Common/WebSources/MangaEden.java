package com.amgregoire.mangafeed.Common.WebSources;


import com.amgregoire.mangafeed.Common.MangaEnums;
import com.amgregoire.mangafeed.Common.RequestWrapper;
import com.amgregoire.mangafeed.Common.WebSources.Base.SourceManga;
import com.amgregoire.mangafeed.Models.Chapter;
import com.amgregoire.mangafeed.Models.Manga;
import com.amgregoire.mangafeed.Utils.MangaDB;
import com.amgregoire.mangafeed.Utils.MangaLogger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MangaEden extends SourceManga
{
    final public static String TAG = MangaEden.class.getSimpleName();
    final public static String URL = "funmanga";

    final private String SourceKey = "MangaEden";
    final private String mBaseUrl = "https://www.mangaeden.com";
    final private String mUpdatesUrl = "http://www.mangaeden.com/ajax/news/1/0/";
    final private String mGenres[] = {
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
            "Shounen",
            "Slice of Life",
            "Smut",
            "Sports",
            "Supernatural",
            "Tragedy",
            "Webtoons",
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

    @Override
    public List<Manga> parseResponseToRecentList(final String responseBody)
    {
        List<Manga> lMangaList = new ArrayList<>();
        Elements lMangaElements = Jsoup.parse(responseBody).select("body > li");

        for (Element iMangaBlock : lMangaElements)
        {
            Element iUrlElement = iMangaBlock.select("div.newsManga").first();
            Element iTitleElement = iMangaBlock.select("div.manga_tooltop_header > a").first();

            String lTitle = iTitleElement.text();
            String lUrl = "https://www.mangaeden.com/api/manga/" + iUrlElement.id().substring(0, 24) + "/";

            lUrl = lUrl.replaceFirst(Manga.Companion.getLinkRegex(), "{" + SourceKey + "}");
            Manga lManga = MangaDB.getInstance().getManga(lUrl);

            if (lManga != null)
            {
                lMangaList.add(lManga);
            }
            else
            {
                lManga = new Manga(lTitle, lUrl, SourceKey);
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
        try
        {
            JSONObject lParsedJsonObject = new JSONObject(responseBody);

            String lGenres = "";
            JSONArray lGenreArrayNodes = lParsedJsonObject.getJSONArray("categories");
            for (int i = 0; i < lGenreArrayNodes.length(); i++)
            {
                if (i != lGenreArrayNodes.length() - 1)
                {
                    lGenres += lGenreArrayNodes.getString(i) + ", ";
                }
                else
                {
                    lGenres += lGenreArrayNodes.getString(i);
                }
            }

            Manga lNewManga = MangaDB.getInstance().getManga(request.getManga().getLink());

            lNewManga.setArtist(lParsedJsonObject.getString("artist"));
            lNewManga.setAuthor(lParsedJsonObject.getString("author"));
            lNewManga.setDescription(lParsedJsonObject.getString("description").trim());
            lNewManga.setGenres(lGenres);
            lNewManga.setImage("https://cdn.mangaeden.com/mangasimg/" + lParsedJsonObject.getString("image"));
            lNewManga.setInitialized(1);

            MangaDB.getInstance().putManga(lNewManga);
            MangaLogger.logError(TAG, "Finished creating/update manga (" + lNewManga.getTitle() + ")");
            return lNewManga;
        }
        catch (Exception aException)
        {
            MangaLogger.logError(TAG, aException.getMessage());
            return null;
        }
    }

    @Override
    public List<Chapter> parseResponseToChapters(RequestWrapper request, String responseBody)
    {
        List<Chapter> lChapterList = null;

        try
        {
            JSONObject lParsedJsonObject = new JSONObject(responseBody);

            lChapterList = resolveChaptersFromParsedJson(lParsedJsonObject, request);
            lChapterList = setNumberForChapterList(lChapterList);
        }
        catch (JSONException aException)
        {
            MangaLogger.logError(TAG, aException.getMessage());
        }

        MangaLogger.logInfo(TAG, "Finished parsing chapter list (" + request.getManga().getLink() + ")");
        return lChapterList;

    }

    @Override
    public List<String> parseResponseToPageUrls(final RequestWrapper requestWrapper, final String responseBody)
    {
        List<String> lImageList = null;

        try
        {
            JSONObject lParsedJson = new JSONObject(responseBody);
            lImageList = new ArrayList<>();

            JSONArray lImageArrayNodes = lParsedJson.getJSONArray("images");
            for (int i = 0; i < lImageArrayNodes.length(); i++)
            {
                JSONArray lCurrentImageNode = lImageArrayNodes.getJSONArray(i);

                lImageList.add("https://cdn.mangaeden.com/mangasimg/" + lCurrentImageNode.getString(1));
            }

            Collections.reverse(lImageList);
        }
        catch (JSONException aException)
        {
            MangaLogger.logError(TAG, aException.getMessage());
        }

        MangaLogger.logInfo(TAG, "Finished parsing page url list.");
        return lImageList;
    }

    @Override
    public String parseResponseToImageUrls(String aResponseBody, final String aResponseUrl)
    {
        //Note: This method was added to keep consistency with the other sources
        return aResponseUrl;
    }

    /***
     * This helper function resolves and builds chapters from the parsed json
     * Parent - parseResponseToChapters();
     *
     * @param aParsedJson
     * @return
     * @throws JSONException
     */
    private List<Chapter> resolveChaptersFromParsedJson(JSONObject aParsedJson, RequestWrapper request) throws JSONException
    {
        List<Chapter> lChapterList = new ArrayList<>();

        String lMangaTitle = aParsedJson.getString("title");
        JSONArray lChapterArrayNodes = aParsedJson.getJSONArray("chapters");
        for (int i = 0; i < lChapterArrayNodes.length(); i++)
        {
            JSONArray lCurrentChapterArray = lChapterArrayNodes.getJSONArray(i);

            Chapter lCurrentChapter = constructChapterFromJSONArray(lCurrentChapterArray, request);

            lChapterList.add(lCurrentChapter);
        }

        Collections.reverse(lChapterList);
        return lChapterList;
    }

    /***
     * This helper function sets the chapters index value.
     * Parent - parseResponseToChapters();
     *
     * @param aChapterList
     * @return
     */
    private List<Chapter> setNumberForChapterList(List<Chapter> aChapterList)
    {
        Collections.reverse(aChapterList);
        for (int i = 0; i < aChapterList.size(); i++)
        {
            aChapterList.get(i).setChapterNumber(i + 1);
        }

        return aChapterList;
    }

    /***
     * This helper function constructs a chapter from the specified JSON.
     * Parent - resolveChaptersFromParsedJson();
     *
     * @param aChapterNode
     * @return
     * @throws JSONException
     */
    private Chapter constructChapterFromJSONArray(JSONArray aChapterNode, RequestWrapper request) throws JSONException
    {
        Chapter lNewChapter = new Chapter(request.getManga().getTitle(), request.getManga().getLink(), SourceKey);

        lNewChapter.setUrl("https://www.mangaeden.com/api/chapter/" + aChapterNode.getString(3) + "/");
        lNewChapter.setChapterTitle(request.getManga().getTitle() + " " + aChapterNode.getDouble(0));

        Date lDate = new Date(aChapterNode.getLong(1) * 1000);
        lNewChapter.setChapterDate(lDate.toString());

        return lNewChapter;
    }
}