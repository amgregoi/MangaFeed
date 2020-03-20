package com.amgregoire.mangafeed.Common.WebSources;


import com.amgregoire.mangafeed.Common.MangaEnums;
import com.amgregoire.mangafeed.Common.RequestWrapper;
import com.amgregoire.mangafeed.Common.WebSources.Base.SourceManga;
import com.amgregoire.mangafeed.Models.DbChapter;
import com.amgregoire.mangafeed.Models.DbManga;
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
    public List<DbManga> parseResponseToRecentList(final String responseBody)
    {
        List<DbManga> lDbMangaList = new ArrayList<>();
        Elements lMangaElements = Jsoup.parse(responseBody).select("body > li");

        for (Element iMangaBlock : lMangaElements)
        {
            Element iUrlElement = iMangaBlock.select("div.newsManga").first();
            Element iTitleElement = iMangaBlock.select("div.manga_tooltop_header > a").first();

            String lTitle = iTitleElement.text();
            String lUrl = "https://www.mangaeden.com/api/manga/" + iUrlElement.id().substring(0, 24) + "/";

            lUrl = lUrl.replaceFirst(DbManga.Companion.getLinkRegex(), "{" + SourceKey + "}");
            DbManga lDbManga = MangaDB.getInstance().getManga(lUrl);

            if (lDbManga != null)
            {
                lDbMangaList.add(lDbManga);
            }
            else
            {
                lDbManga = new DbManga(lTitle, lUrl, SourceKey);
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

        MangaLogger.logInfo(TAG, "Finished parsing recent updates");

        if (lDbMangaList.size() == 0)
        {
            return null;
        }
        return lDbMangaList;
    }

    @Override
    public DbManga parseResponseToManga(final RequestWrapper request, final String responseBody)
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

            DbManga lNewDbManga = MangaDB.getInstance().getManga(request.getManga().getLink());

            lNewDbManga.setArtist(lParsedJsonObject.getString("artist"));
            lNewDbManga.setAuthor(lParsedJsonObject.getString("author"));
            lNewDbManga.setDescription(lParsedJsonObject.getString("description").trim());
            lNewDbManga.setGenres(lGenres);
            lNewDbManga.setImage("https://cdn.mangaeden.com/mangasimg/" + lParsedJsonObject.getString("image"));
            lNewDbManga.setInitialized(1);

            MangaDB.getInstance().putManga(lNewDbManga);
            MangaLogger.logError(TAG, "Finished creating/update manga (" + lNewDbManga.getTitle() + ")");
            return lNewDbManga;
        }
        catch (Exception aException)
        {
            MangaLogger.logError(TAG, aException.getMessage());
            return null;
        }
    }

    @Override
    public List<DbChapter> parseResponseToChapters(RequestWrapper request, String responseBody)
    {
        List<DbChapter> lDbChapterList = null;

        try
        {
            JSONObject lParsedJsonObject = new JSONObject(responseBody);

            lDbChapterList = resolveChaptersFromParsedJson(lParsedJsonObject, request);
            lDbChapterList = setNumberForChapterList(lDbChapterList);
        }
        catch (JSONException aException)
        {
            MangaLogger.logError(TAG, aException.getMessage());
        }

        MangaLogger.logInfo(TAG, "Finished parsing chapter list (" + request.getManga().getLink() + ")");
        return lDbChapterList;

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
    private List<DbChapter> resolveChaptersFromParsedJson(JSONObject aParsedJson, RequestWrapper request) throws JSONException
    {
        List<DbChapter> lDbChapterList = new ArrayList<>();

        String lMangaTitle = aParsedJson.getString("title");
        JSONArray lChapterArrayNodes = aParsedJson.getJSONArray("chapters");
        for (int i = 0; i < lChapterArrayNodes.length(); i++)
        {
            JSONArray lCurrentChapterArray = lChapterArrayNodes.getJSONArray(i);

            DbChapter lCurrentDbChapter = constructChapterFromJSONArray(lCurrentChapterArray, request);

            lDbChapterList.add(lCurrentDbChapter);
        }

        Collections.reverse(lDbChapterList);
        return lDbChapterList;
    }

    /***
     * This helper function sets the chapters index value.
     * Parent - parseResponseToChapters();
     *
     * @param aDbChapterList
     * @return
     */
    private List<DbChapter> setNumberForChapterList(List<DbChapter> aDbChapterList)
    {
        Collections.reverse(aDbChapterList);
        for (int i = 0; i < aDbChapterList.size(); i++)
        {
            aDbChapterList.get(i).setChapterNumber(i + 1);
        }

        return aDbChapterList;
    }

    /***
     * This helper function constructs a chapter from the specified JSON.
     * Parent - resolveChaptersFromParsedJson();
     *
     * @param aChapterNode
     * @return
     * @throws JSONException
     */
    private DbChapter constructChapterFromJSONArray(JSONArray aChapterNode, RequestWrapper request) throws JSONException
    {
        DbChapter lNewDbChapter = new DbChapter(request.getManga().getTitle(), request.getManga().getLink(), SourceKey);

        lNewDbChapter.setUrl("https://www.mangaeden.com/api/chapter/" + aChapterNode.getString(3) + "/");
        lNewDbChapter.setChapterTitle(request.getManga().getTitle() + " " + aChapterNode.getDouble(0));

        Date lDate = new Date(aChapterNode.getLong(1) * 1000);
        lNewDbChapter.setChapterDate(lDate.toString());

        return lNewDbChapter;
    }
}