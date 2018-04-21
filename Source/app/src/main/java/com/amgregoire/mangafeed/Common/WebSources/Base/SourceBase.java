package com.amgregoire.mangafeed.Common.WebSources.Base;

import android.graphics.drawable.Drawable;

import com.amgregoire.mangafeed.Common.MangaEnums;
import com.amgregoire.mangafeed.Common.RequestWrapper;
import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.Models.Chapter;
import com.amgregoire.mangafeed.Models.Manga;
import com.amgregoire.mangafeed.Utils.NetworkService;
import com.amgregoire.mangafeed.Utils.SharedPrefs;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Headers;

public abstract class SourceBase
{
    private String TAG = SourceBase.class.getSimpleName();

    public abstract String getSourceName();

    /***
     * This function returns the type of content the source provides.
     * @return
     */
    public abstract MangaEnums.SourceType getSourceType();

    /***
     * This function retrieves the sources recent update page url.
     * @return
     */
    public abstract String getRecentUpdatesUrl();

    /***
     * This function retrieves the array of genres a source supports.
     *
     * @return
     */
    public abstract String[] getGenres();

    /***
     * This function pareses the response body and builds the Recent Manga list.
     * @param responseBody
     * @return
     */
    public abstract List<Manga> parseResponseToRecentList(final String responseBody);

    /***
     * This function pareses the response body to a manga object specified by the request.
     *
     * @param request
     * @param responseBody
     * @return
     */
    public abstract Manga parseResponseToManga(final RequestWrapper request, final String responseBody);

    /***
     * This function pareses the response body and builds a list of chapters from the specified request.
     * @param request
     * @param responseBody
     * @return
     */
    public abstract List<Chapter> parseResponseToChapters(RequestWrapper request, String responseBody);

    /***
     * This function pareses the response body and returns a list of page urls for the images
     *
     * @param responseBody
     * @return
     */
    public abstract List<String> parseResponseToPageUrls(final String responseBody);

    /***
     * This function pareses the response body and returns the image url of its page.
     *
     * @param responseBody
     * @param responseUrl
     * @return
     */
    public abstract String parseResponseToImageUrls(final String responseBody, final String responseUrl);

    /***
     * This function parses the repsonse body and compares the list of links with the local database, adding anything new.
     *
     */
    public void updateLocalCatalog() { }

    /***
     * This function retrieves a list of recent updates from the current source.
     *
     * @return
     */
    public Observable<List<Manga>> getRecentMangaObservable()
    {
        return NetworkService.getTemporaryInstance()
                             .getResponse(getRecentUpdatesUrl())
                             .flatMap(aResponse -> NetworkService.mapResponseToString(aResponse))
                             .flatMap(aHtml -> Observable.just(parseResponseToRecentList(aHtml)))
                             .observeOn(AndroidSchedulers.mainThread())
                             .retry(3);
    }

    /***
     * This function retrieves a list of chapter image urls from the specified request.
     *
     * @param aRequest
     * @return
     */
    public abstract Observable<String> getChapterImageListObservable(final RequestWrapper aRequest);

    /***
     * This function retrieves a list of chapters from the specified request.
     *
     * @param request
     * @return
     */
    public Observable<List<Chapter>> getChapterListObservable(final RequestWrapper request)
    {
        return NetworkService.getPermanentInstance()
                             .getResponseCustomHeaders(request.getMangaUrl(), constructRequestHeaders())
                             .flatMap(aResponse -> NetworkService.mapResponseToString(aResponse))
                             .flatMap(aResponseBody -> Observable.just(parseResponseToChapters(request, aResponseBody)))
                             .subscribeOn(Schedulers.computation())
                             .observeOn(AndroidSchedulers.mainThread());
    }

    /***
     * Adds new Manga and
     * gets missing manga information and updates local database
     *
     * @param request
     * @return
     */
    public Observable<Manga> updateMangaObservable(final RequestWrapper request)
    {
        return NetworkService.getPermanentInstance()
                             .getResponseCustomHeaders(request.getMangaUrl(), constructRequestHeaders())
                             .flatMap(aResponse -> NetworkService.mapResponseToString(aResponse))
                             .flatMap(aResponseBody -> Observable.just(parseResponseToManga(request, aResponseBody)))
                             .subscribeOn(Schedulers.computation())
                             .observeOn(AndroidSchedulers.mainThread());
    }

    /***
     * This function updates the local database with manga objects found at the specified link.
     *
     * @param link
     * @return
     */
    public Observable updateCatalogObservable(String link)
    {
        return NetworkService.getPermanentInstance()
                             .getResponseCustomHeaders(link, constructRequestHeaders())
                             .flatMap(aResponse -> NetworkService.mapResponseToString(aResponse))
                             .subscribeOn(Schedulers.computation());
    }

    /***
     * This function returns the current source.
     *
     * @return
     */
    public MangaEnums.Source getCurrentSource()
    {
        return MangaEnums.Source.valueOf(SharedPrefs.getSavedSource());
    }

    /***
     * This function sets the current source.
     *
     * @param source
     */
    public void setCurrentSource(MangaEnums.Source source)
    {
        SharedPrefs.setSavedSource(source.name());
    }

    /***
     * This function returns a source specified by its position in the source enum.
     *
     * @param position
     * @return
     */
    public MangaEnums.Source getSourceByPosition(int position)
    {
        MangaEnums.Source[] lSources = MangaEnums.Source.values();
        if (position < lSources.length)
        {
            return lSources[position];
        }
        return lSources[1];
    }

    /***
     * This function caches images.
     *
     * @param imageUrls
     * @return
     */
    public Observable<Drawable> cacheFromImagesOfSize(final List<String> imageUrls)
    {
        return Observable.create((ObservableEmitter<Drawable> subscriber) ->
        {
            try
            {
                for (String imageUrl : imageUrls)
                {
                    if (!subscriber.isDisposed())
                    {
                        RequestOptions lOptions = new RequestOptions();
                        lOptions.skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);

                        FutureTarget<Drawable> cacheFuture = Glide.with(MangaFeed.getInstance())
                                                                  .load(imageUrl)
                                                                  .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);

                        subscriber.onNext(cacheFuture.get(30, TimeUnit.SECONDS));
                    }
                }
                subscriber.onComplete();
            }
            catch (Throwable e)
            {
                subscriber.onError(e);
            }
        }).subscribeOn(Schedulers.newThread());
    }

    /***
     * This function constructs a custom http header for various sources
     *
     * @return
     */
    private Headers constructRequestHeaders()
    {
        Headers.Builder headerBuilder = new Headers.Builder();
        headerBuilder.add("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64)");
        headerBuilder.add("Cookie", "lang_option=English");

        return headerBuilder.build();
    }
}
