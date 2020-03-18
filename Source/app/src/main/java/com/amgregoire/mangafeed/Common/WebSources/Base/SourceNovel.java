package com.amgregoire.mangafeed.Common.WebSources.Base;

import com.amgregoire.mangafeed.Common.RequestWrapper;
import com.amgregoire.mangafeed.Utils.NetworkService;

import io.reactivex.Observable;


/**
 * Created by Andy Gregoire 3/8/2018.
 */

public abstract class SourceNovel extends SourceBase
{
    @Override
    public boolean requiresCloudFlare()
    {
        return false;
    }

    public Observable<String> getChapterImageListObservable(final RequestWrapper request)
    {
        final NetworkService lCurrentService = NetworkService.Companion.getTemporaryInstance();

        return lCurrentService.getResponseCustomHeaders(request.getChapter().getUrl(), constructRequestHeaders())
                              .flatMap(response -> NetworkService.Companion.mapResponseToString(response))
                              .flatMap(unParsedHtml -> Observable.just(parseResponseToImageUrls(unParsedHtml, request.getChapter().getUrl())));
    }

}
