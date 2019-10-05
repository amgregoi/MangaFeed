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

        return lCurrentService.getResponse(request.getChapter().url)
                              .flatMap(aResponse -> NetworkService.Companion.mapResponseToString(aResponse))
                              .flatMap(aUnparsedHtml -> Observable.just(parseResponseToImageUrls(aUnparsedHtml, request.getChapter().url)));
    }

}
