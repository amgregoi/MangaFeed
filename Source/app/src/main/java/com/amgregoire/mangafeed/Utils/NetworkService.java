package com.amgregoire.mangafeed.Utils;


import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkService
{
    private static NetworkService sInstance;

    private OkHttpClient mClient;

    private NetworkService()
    {
        mClient = new OkHttpClient.Builder().readTimeout(10, TimeUnit.SECONDS)
                                            .writeTimeout(10, TimeUnit.SECONDS)
                                            .readTimeout(30, TimeUnit.SECONDS)
                                            .build();
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
        return Observable.create((ObservableEmitter<Response> subscriber) ->
        {
            try
            {
                Request request = new Request.Builder()
                        .url(url)
                        .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21")
                        .build();

                subscriber.onNext(mClient.newCall(request).execute());
                subscriber.onComplete();
            }
            catch (Throwable e)
            {
                subscriber.tryOnError(e);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
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
        return Observable.create((ObservableEmitter<Response> subscriber) ->
        {
            try
            {
                Request request = new Request.Builder()
                        .url(url)
                        .headers(headers)
                        .build();

                subscriber.onNext(mClient.newCall(request).execute());
                subscriber.onComplete();
            }
            catch (Throwable e)
            {
                subscriber.tryOnError(e);
            }
        }).subscribeOn(Schedulers.io());
    }

    /***
     * This function returns an observable string from the specified response.
     *
     * @param response
     * @return
     */
    public static Observable<String> mapResponseToString(final Response response)
    {
        return Observable.create((ObservableEmitter<String> subscriber) ->
        {
            try
            {
                subscriber.onNext(response.body().string());
                subscriber.onComplete();
            }
            catch (Throwable e)
            {
                subscriber.tryOnError(e);
            }
        }).subscribeOn(Schedulers.io());
    }
}
