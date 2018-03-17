package com.amgregoire.mangafeed.Utils;

/**
 * Created by Andy Gregoire on 3/16/2018.
 */


import com.amgregoire.mangafeed.BuildConfig;
import com.amgregoire.mangafeed.MangaFeed;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class MangaFeedRest
{

    private static final String BASE_URL = BuildConfig.BASE_URL;

    private static AsyncHttpClient client = new AsyncHttpClient();

    /***
     * User API Calls
     */

    /***
     * This function registers/creates a new user.
     *
     * @param params http request parameters
     * @param handler response callback handler
     */
    public static void postUser(RequestParams params, AsyncHttpResponseHandler handler)
    {
        client.post(MangaFeed.getInstance(), getAbsoluteUrl("user"), params, handler);
    }

    /***
     * This function retrieves a user by their email, specified in the request parameters.
     *
     * @param params http request parameters
     * @param handler response callback handler
     */
    public static void getUser(RequestParams params, AsyncHttpResponseHandler handler)
    {
        client.get(getAbsoluteUrl("user"), params, handler);
    }

    /***
     * This function retrieves a user by their userID.
     *
     * @param userId
     * @param params http request parameters
     * @param handler response callback handler
     */
    public static void getUser(int userId, RequestParams params, AsyncHttpResponseHandler handler)
    {
        client.get(getAbsoluteUrl("user/" + String.valueOf(userId)), params, handler);
    }

    /***
     * Manga API Calls
     */

    /***
     * This function retrieves a manga object by its url, specified in the request parameters.
     *
     * @param params http request parameters
     * @param handler response callback handler
     */
    public static void getManga(RequestParams params, AsyncHttpResponseHandler handler)
    {
        client.get(getAbsoluteUrl("manga/"), params, handler);
    }


    /***
     * Follow API Calls
     */

    /***
     * This function retrieves a users library (items they have followed).
     *
     * @param userId
     * @param params http request parameters
     * @param handler response callback handler
     */
    public static void getUserLibrary(int userId, RequestParams params, AsyncHttpResponseHandler handler)
    {
        client.get(getAbsoluteUrl("follow/" + String.valueOf(userId) + "/library"), params, handler);
    }

    /***
     * This function registers a new follow item, or updates an existing follow item from a user.
     *
     * @param userId
     * @param params http request parameters
     * @param handler response callback handler
     */
    public static void postFollowedUpdate(int userId, RequestParams params, AsyncHttpResponseHandler handler)
    {
        client.post(MangaFeed.getInstance(), getAbsoluteUrl("follow/" + String.valueOf(userId) + "/update"), params, handler);
    }

    /***
     * Version API Calls
     */

    /***
     * This function retrieves the stored mobile version of the application to check if the current application is out of date / can be updated.
     *
     * @param params http request parameters
     * @param handler response callback handler
     */
    public static void getVersion(RequestParams params, AsyncHttpResponseHandler handler)
    {
        client.get(getAbsoluteUrl("version/android"), params, handler);
    }


    private static String getAbsoluteUrl(String relativeUrl)
    {
        return BASE_URL + relativeUrl;
    }
}
