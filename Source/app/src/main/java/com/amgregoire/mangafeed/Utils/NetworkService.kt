package com.amgregoire.mangafeed.Utils


import com.amgregoire.mangafeed.v2.service.CloudFlareService
import com.amgregoire.mangafeed.v2.service.Logger
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.TimeUnit

class NetworkService private constructor()
{

    private val mClient: OkHttpClient = OkHttpClient.Builder().readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    /***
     * This function returns an observable response tot he requested url.
     *
     * @param url
     * @return
     */
    fun getResponse(url: String): Observable<Response>
    {
        Logger.debug("Getting response for: $url")
        return Observable.create { subscriber: ObservableEmitter<Response> ->
            try
            {
                val builder = Request.Builder().url(url).header("User-Agent", defaultUserAgent)

                CloudFlareService().getCFCookies(url, defaultUserAgent) { cookies ->
                    for (cookie in cookies) builder.addHeader("Cookie", cookie)
                }

                val request = builder.build()

                subscriber.onNext(mClient.newCall(request).execute())
                subscriber.onComplete()
            }
            catch (e: Throwable)
            {
                subscriber.tryOnError(e)
            }
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }

    /***
     * This function returns an observable response to the requested url, and specified headers.
     *
     * @param url
     * @param headers
     * @return
     */
    fun getResponseCustomHeaders(url: String, headers: Headers): Observable<Response>
    {
        Logger.debug("Getting response for: $url")

        return Observable.create { subscriber: ObservableEmitter<Response> ->
            try
            {
                val builder = Request.Builder()
                        .url(url)
                        .headers(headers)

                CloudFlareService().getCFCookies(url, defaultUserAgent) { cookies ->
                    for (cookie in cookies) builder.addHeader("Cookie", cookie)
                }

                val request = builder.build()

                subscriber.onNext(mClient.newCall(request).execute())
                subscriber.onComplete()
            }
            catch (e: Throwable)
            {
                subscriber.tryOnError(e)
            }
        }.subscribeOn(Schedulers.io())
    }

    companion object
    {
        private var instance: NetworkService? = null

        val permanentInstance: NetworkService
            get() = instance ?: runBlocking {
                instance = NetworkService()
                instance!!
            }

        val temporaryInstance: NetworkService = NetworkService()

//        const val defaultUserAgent = "Mozilla/5.0 (Windows NT 6.3; WOW64)"
        const val defaultUserAgent = "Mozilla/5.0 (Linux; Android 8.0.0; SM-G960F Build/R16NW) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.84 Mobile Safari/537.36"
        /***
         * This function returns an observable string from the specified response.
         *
         * @param response
         * @return
         */
        fun mapResponseToString(response: Response): Observable<String>
        {
            return Observable.create { subscriber: ObservableEmitter<String> ->
                try
                {
                    response.body?.let { body -> subscriber.onNext(body.string()) }
                    subscriber.onComplete()
                    response.close()
                }
                catch (e: Throwable)
                {
                    subscriber.tryOnError(e)
                }
            }.subscribeOn(Schedulers.io())
        }
    }
}