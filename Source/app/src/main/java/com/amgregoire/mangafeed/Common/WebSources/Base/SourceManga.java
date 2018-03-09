package com.amgregoire.mangafeed.Common.WebSources.Base;

import com.amgregoire.mangafeed.Utils.NetworkService;
import com.amgregoire.mangafeed.Common.RequestWrapper;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by Andy Gregoire on 3/8/2018.
 */

public abstract class SourceManga extends SourceBase
{
    public Observable<String> getChapterImageListObservable(final RequestWrapper aRequest)
    {
        final List<String> lTemporaryCachedImageUrls = new ArrayList<>();
        final NetworkService lCurrentService = NetworkService.getTemporaryInstance();

        return lCurrentService.getResponse(aRequest.getChapterUrl())
                              .flatMap(aResponse -> NetworkService.mapResponseToString(aResponse))
                              .flatMap(aUnparsedHtml -> Observable.just(parseResponseToPageUrls(aUnparsedHtml)))
                              .flatMap(aPageUrls -> Observable.from(aPageUrls.toArray(new String[aPageUrls.size()])))
                              .buffer(10)
                              .concatMap(batchedPageUrls ->
                              {
                                  List<Observable<String>> lImageUrlObservables = new ArrayList<>();
                                  for (String iPageUrl : batchedPageUrls)
                                  {
                                      Observable<String> lTemporaryObservable = lCurrentService.getResponse(iPageUrl)
                                                                                               .subscribeOn(Schedulers.io())
                                                                                               .flatMap(NetworkService::mapResponseToString)
                                                                                               .flatMap(unparsedHtml -> Observable
                                                                                                       .just(parseResponseToImageUrls(unparsedHtml, iPageUrl)));
                                      lImageUrlObservables.add(lTemporaryObservable);
                                  }

                                  return Observable.zip(lImageUrlObservables, args ->
                                  {
                                      List<String> lImageUrls = new ArrayList<>();
                                      for (Object iUncastImageUrl : args)
                                      {
                                          lImageUrls.add(String.valueOf(iUncastImageUrl));
                                      }
                                      return lImageUrls;
                                  });
                              })
                              .concatMap(batchedImageUrls -> Observable.from(batchedImageUrls.toArray(new String[batchedImageUrls.size()])))
                              .doOnNext(lTemporaryCachedImageUrls::add)
                              .onBackpressureBuffer();
    }
}
