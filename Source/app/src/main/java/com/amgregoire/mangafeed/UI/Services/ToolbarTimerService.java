package com.amgregoire.mangafeed.UI.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.amgregoire.mangafeed.Utils.MangaLogger;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Andy Gregoire on 4/5/2018.
 */
public class ToolbarTimerService extends Service
{
    public final static String TAG = ToolbarTimerService.class.getSimpleName();

    private final IBinder mBinder = new LocalBinder();
    private ReaderTimerListener mListener;

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder
    {
        public ToolbarTimerService getService()
        {
            return ToolbarTimerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    public void setToolbarListener(ReaderTimerListener aListener)
    {
        if(aListener == null) return;
        mListener = aListener;
    }

    Observable<Long> mTimer = Observable.timer(6, TimeUnit.SECONDS, Schedulers.io());
    Observable<Long> mSystemUI = Observable.timer(6, TimeUnit.SECONDS, Schedulers.io());
    Disposable mTimerSub;
    Disposable mSystemUISub;

    public void startTimer()
    {
        if (mTimerSub != null)
        {
            mTimerSub.dispose();
            mTimerSub = null;
        }

        mTimerSub = mTimer.subscribeOn(Schedulers.io())
                          .observeOn(AndroidSchedulers.mainThread())
                          .subscribe(aComplete ->
                          {
                              mListener.hideToolbar();
                              mTimerSub.dispose();
                              mTimerSub = null;
                              MangaLogger.logInfo(TAG, "ToolBar Service >> Hide Toolbar");
                          }, throwable ->
                          {
                              MangaLogger.logError(TAG, throwable.getMessage());
                          });

    }

    public void stopTimer()
    {
        if (mTimerSub != null)
        {
            mTimerSub.dispose();
            mTimerSub = null;
            restartSystemUiTimer();
        }
    }

    public void restartTimer()
    {
        stopTimer();
        startTimer();
    }

    private void restartSystemUiTimer()
    {
        if (mSystemUISub != null)
        {
            mSystemUISub.dispose();
            mSystemUISub = null;
        }

        mSystemUISub = mSystemUI.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(aComplete -> mListener.hideSystemUi(),
                                        throwable -> MangaLogger.logError(TAG, throwable.getMessage()));

    }

    public interface ReaderTimerListener{
        void hideToolbar();
        void hideSystemUi();
    }
}
