package com.amgregoire.mangafeed

import android.app.Application
import android.widget.Toast
import com.amgregoire.mangafeed.Common.MangaEnums
import com.amgregoire.mangafeed.Common.WebSources.Base.SourceBase
import com.amgregoire.mangafeed.Common.WebSources.FunManga
import com.amgregoire.mangafeed.Common.WebSources.MangaEden
import com.amgregoire.mangafeed.Common.WebSources.MangaHere
import com.amgregoire.mangafeed.Common.WebSources.ReadLight
import com.amgregoire.mangafeed.Models.Chapter
import com.amgregoire.mangafeed.Utils.MangaDB
import com.amgregoire.mangafeed.Utils.MangaLogger
import com.amgregoire.mangafeed.Utils.RxBus
import com.amgregoire.mangafeed.Utils.SharedPrefs
import com.amgregoire.mangafeed.v2.FunMangaCookiePreferences
import com.amgregoire.mangafeed.v2.UserPreferences
import com.amgregoire.mangafeed.v2.di.component.AppComponent
import com.amgregoire.mangafeed.v2.di.component.DaggerAppComponent
import com.amgregoire.mangafeed.v2.di.module.ApiModule
import com.amgregoire.mangafeed.v2.di.module.ApplicationModule
import com.amgregoire.mangafeed.v2.di.module.RoomModule
import com.bumptech.glide.request.target.ViewTarget
import com.squareup.picasso.Cache
import com.squareup.picasso.Picasso
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.*
import java.util.concurrent.Executors
import javax.inject.Inject

/**
 * Created by Andy Gregoire on 3/8/2018.
 */
val ioScope = CoroutineScope(Dispatchers.IO)
val uiScope = CoroutineScope(Dispatchers.Main)

val currentSource: SourceBase
    get() = MangaEnums.Source.valueOf(SharedPrefs.getSavedSource()).source

val appComponent: AppComponent = MangaFeed.app.appComponent

class MangaFeed : Application()
{
    private var mBus: RxBus? = null
    private var compositeDisposable = CompositeDisposable()
    private var mCurrentChapters: ArrayList<Chapter>? = null
    private var mPicasso: Picasso? = null

    val cookiePreferences by lazy { FunMangaCookiePreferences(app) }
    val userPreferences by lazy { UserPreferences(app) }

    /************************************************************************
     *
     * Dagger
     *
     ***********************************************************************/
    val appComponent: AppComponent by lazy {
        DaggerAppComponent.builder()
                .applicationModule(ApplicationModule(this))
                .roomModule(RoomModule(this))
                .apiModule(ApiModule())
                .build()
    }

    @Inject lateinit var db: MangaDB
    @Inject lateinit var prefs: SharedPrefs

    /************************************************************************
     *
     *
     *
     ***********************************************************************/

    val currentSourceType: MangaEnums.SourceType
        get() = MangaEnums.Source.valueOf(SharedPrefs.getSavedSource()).source.sourceType

    val currentSource: SourceBase
        get() = MangaEnums.Source.valueOf(SharedPrefs.getSavedSource()).source

    var currentChapters: List<Chapter>?
        get() = mCurrentChapters
        set(chapters)
        {
            mCurrentChapters = ArrayList(chapters)
            mCurrentChapters!!.reverse()
        }

    /************************************************************************
     *
     * Dagger
     *
     ***********************************************************************/

    override fun onCreate()
    {
        super.onCreate()
        mangaApp = this
        cookiePreferences.clear()

        appComponent.inject(this)

        ViewTarget.setTagId(R.id.glide_tag)
        mBus = RxBus()

        mPicasso = Picasso.Builder(this).executor(Executors.newSingleThreadExecutor()).memoryCache(Cache.NONE).indicatorsEnabled(true).build()
        mPicasso!!.setIndicatorsEnabled(true)
        Picasso.setSingletonInstance(mPicasso!!)
        updateCatalogs(false) // check if we should update local database on application open
    }

    /***
     * This function returns the application RxBus instance.
     *
     * @return
     */
    fun rxBus(): RxBus?
    {
        return mBus
    }

    /***
     * This function creates and shows a SHORT toast message.
     *
     * @param message
     */
    fun makeToastShort(message: String)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /***
     * This function creates and shows a LONG toast message.
     *
     * @param message
     */
    fun makeToastLong(message: String)
    {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    /***
     * This function returns a new source specified by the source classes TAG.
     *
     * @param tag
     * @return
     */
    fun getSourceByTag(tag: String): SourceBase
    {
        return when (tag)
        {
            FunManga.TAG -> FunManga()
            MangaEden.TAG -> MangaEden()
            MangaHere.TAG -> MangaHere()
            else -> ReadLight()
        }
    }

    /***
     * This function returns a new source specified by the source classes TAG.
     *
     * @param url
     * @return
     */
    fun getSourceByUrl(url: String): SourceBase
    {
        return when
        {
            url.contains(FunManga.URL) -> FunManga()
            url.contains(MangaEden.URL) -> MangaEden()
            url.contains(MangaHere.URL) -> MangaHere()
            else -> ReadLight()
        }
    }

    /***
     * This function updates source catalogs items on the local database, adding any missing items.
     * Currently it is set to update no more than once a week.
     *
     */
    fun updateCatalogs(isForceUpdate: Boolean)
    {
        val lWeekSeconds = 604800
        val lWeekMs = lWeekSeconds * 1000

        // Check if we updated in the last week, if we have we'll skip.
        val lLowerLimit = Date(SharedPrefs.getLastCatalogUpdate().time + lWeekMs)
        if (lLowerLimit.before(Date()) || isForceUpdate)
        {
            compositeDisposable.add(
                    Observable.create { subscriber: ObservableEmitter<SourceBase> ->
                        try
                        {
                            SharedPrefs.setLastCatalogUpdate()
                            val lSources = MangaEnums.Source.values()

                            for (source in lSources)
                            {
                                source.source.updateLocalCatalog()
                                subscriber.onNext(source.source)
                            }
                            subscriber.onComplete()
                        }
                        catch (ex: Exception)
                        {
                            subscriber.onError(ex)
                        }
                    }.subscribeOn(Schedulers.computation()).subscribe(
                            { source -> source.updateLocalCatalog() }, // onNext
                            { throwable -> MangaLogger.logError(TAG, throwable.message) } // onError
                    )
            )
        }
    }

    companion object
    {
        private var TAG = MangaFeed::class.simpleName

        private var mangaApp: MangaFeed? = null
        val app get() = checkNotNull(mangaApp)
    }
}
