package com.amgregoire.mangafeed.Utils;

import com.jakewharton.rxrelay2.PublishRelay;
import com.jakewharton.rxrelay2.Relay;

import io.reactivex.Observable;


/**
 * Created by Andy Gregoire on 3/10/2018.
 */

public class RxBus
{
    private final Relay<Object> bus = PublishRelay.create().toSerialized();

    public void send(Object event) {
        bus.accept(event);
    }

    public Observable<Object> toObservable() {
        return bus;
    }

    public boolean hasObservers() {
        return bus.hasObservers();
    }
}