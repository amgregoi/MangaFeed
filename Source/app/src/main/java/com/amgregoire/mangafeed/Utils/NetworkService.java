package com.amgregoire.mangafeed.Utils;


import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

public class NetworkService
{
    private static NetworkService sInstance;

    private OkHttpClient mClient;

    private NetworkService()
    {
        mClient = new OkHttpClient();
        mClient.setConnectTimeout(10, TimeUnit.SECONDS);
        mClient.setWriteTimeout(10, TimeUnit.SECONDS);
        mClient.setReadTimeout(30, TimeUnit.SECONDS);
    }

    public static NetworkService getPermanentInstance()
    {
        if (sInstance == null)
        {
            sInstance = new NetworkService();
        }

        return sInstance;
    }

    public static NetworkService getTemporaryInstance()
    {
        return new NetworkService();
    }

    /***
     * This function returns an observable response tot he requested url.
     *
     * @param url
     * @return
     */
    public Observable<Response> getResponse(final String url)
    {
        return Observable.create((Observable.OnSubscribe<Response>) subscriber ->
        {
            try
            {
                Request request = new Request.Builder()
                        .url(url)
                        .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                        .build();

                subscriber.onNext(mClient.newCall(request).execute());
                subscriber.onCompleted();
            }
            catch (Throwable e)
            {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io());
    }

    /***
     * This function returns an observable response to the requested url, and specified headers.
     *
     * @param url
     * @param headers
     * @return
     */
    public Observable<Response> getResponseCustomHeaders(final String url, final Headers headers)
    {
        return Observable.create((Observable.OnSubscribe<Response>) subscriber ->
        {
            try
            {
                Request request = new Request.Builder()
                        .url(url)
                        .headers(headers)
                        .build();

                subscriber.onNext(mClient.newCall(request).execute());
                subscriber.onCompleted();
            }
            catch (Throwable e)
            {
                subscriber.onError(e);
            }
        })
                         .subscribeOn(Schedulers.io());
    }

    /***
     * This function returns an observable string from the specified response.
     *
     * @param response
     * @return
     */
    public static Observable<String> mapResponseToString(final Response response)
    {
        return Observable.create((Observable.OnSubscribe<String>) subscriber ->
        {
            try
            {
                subscriber.onNext(response.body().string());
                subscriber.onCompleted();
            }
            catch (Throwable e)
            {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.io());
    }
}
