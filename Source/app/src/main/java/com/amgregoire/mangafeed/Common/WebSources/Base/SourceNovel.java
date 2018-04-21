package com.amgregoire.mangafeed.Common.WebSources.Base;

import com.amgregoire.mangafeed.Common.RequestWrapper;
import com.amgregoire.mangafeed.Utils.NetworkService;

import io.reactivex.Observable;


/**
 * Created by Andy Gregoire 3/8/2018.
 */

public abstract class SourceNovel extends SourceBase
{
    public Observable<String> getChapterImageListObservable(final RequestWrapper request)
    {
        final NetworkService lCurrentService = NetworkService.getTemporaryInstance();

        return lCurrentService.getResponse(request.getChapterUrl())
                              .flatMap(aResponse -> NetworkService.mapResponseToString(aResponse))
                              .flatMap(aUnparsedHtml -> Observable.just(parseResponseToImageUrls(aUnparsedHtml, request.getChapterUrl())));
    }

}
