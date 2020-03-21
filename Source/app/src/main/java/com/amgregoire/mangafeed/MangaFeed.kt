package com.amgregoire.mangafeed

import android.app.Application
import android.widget.Toast
import com.amgregoire.mangafeed.Common.MangaEnums
import com.amgregoire.mangafeed.Common.WebSources.Base.SourceBase
import com.amgregoire.mangafeed.Common.WebSources.FunManga
import com.amgregoire.mangafeed.Common.WebSources.MangaEden
import com.amgregoire.mangafeed.Common.WebSources.MangaHere
import com.amgregoire.mangafeed.Common.WebSources.ReadLight
import com.amgregoire.mangafeed.Models.DbChapter
import com.amgregoire.mangafeed.Utils.MangaDB
import com.amgregoire.mangafeed.Utils.RxBus
import com.amgregoire.mangafeed.Utils.SharedPrefs
import com.amgregoire.mangafeed.v2.FunMangaCookiePreferences
import com.amgregoire.mangafeed.v2.ReadLightCookiePreferences
import com.amgregoire.mangafeed.v2.UserPreferences
import com.amgregoire.mangafeed.v2.di.component.AppComponent
import com.amgregoire.mangafeed.v2.di.component.DaggerAppComponent
import com.amgregoire.mangafeed.v2.di.module.ApiModule
import com.amgregoire.mangafeed.v2.di.module.ApplicationModule
import com.amgregoire.mangafeed.v2.di.module.RoomModule
import com.amgregoire.mangafeed.v2.extension.fromJson
import com.amgregoire.mangafeed.v2.extension.toJson
import com.amgregoire.mangafeed.v2.model.domain.User
import com.amgregoire.mangafeed.v2.model.domain.UserLibrary
import com.amgregoire.mangafeed.v2.repository.local.LocalMangaRepository
import com.amgregoire.mangafeed.v2.service.Logger
import com.amgregoire.mangafeed.v2.ui.LoginActivity
import com.amgregoire.mangafeed.v2.usecase.GetUserUseCase
import com.amgregoire.mangafeed.v2.usecase.SignOutUseCase
import com.bumptech.glide.request.target.ViewTarget
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
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
    private var compositeDisposable = CompositeDisposable()
    private val userPreferences by lazy { UserPreferences(app) }

    @Deprecated("v1")
    private var mCurrentDbChapters: ArrayList<DbChapter>? = null

    var user: User?
        get()
        {
            return userPreferences.user?.fromJson()
        }
        set(value)
        {
            if (value != null)
            {
                userPreferences.user = value.toJson()
                isSignedIn = true
            }
            else userPreferences.clear()
        }

    var isSignedIn: Boolean
        get()
        {
            return userPreferences.isSignedIn
        }
        set(value)
        {
            userPreferences.isSignedIn = value
        }

    var userLibrary: UserLibrary? = null
        set(value)
        {
            ioScope.launch {
                val localMangaRepository = LocalMangaRepository()
                value?.library?.forEach {
                    localMangaRepository.getManga(it.link, it.source)?.apply { this.id = it.id }
                            ?.let {
                                localMangaRepository.putManga(it)
                            }
                }
            }
            field = value
        }

    fun cookiePreferences() = when (currentSource.sourceName)
    {
        ReadLight.SourceKey -> ReadLightCookiePreferences(app)
        else -> FunMangaCookiePreferences(app)
    }


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

    @Deprecated("v1")
    var currentDbChapters: List<DbChapter>?
        get() = mCurrentDbChapters
        set(chapters)
        {
            mCurrentDbChapters = ArrayList(chapters)
            mCurrentDbChapters!!.reverse()
        }

    @Deprecated("v1")
    private var mBus: RxBus? = null

    @Deprecated("v1")
    fun rxBus(): RxBus?
    {
        return mBus
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
        cookiePreferences().clear()

        appComponent.inject(this)

        ViewTarget.setTagId(R.id.glide_tag)

        mBus = RxBus()

        updateUser()
    }

    fun logout()
    {
        user?.let {
            SignOutUseCase().signOut(
                    accessToken = it.accessToken,
                    result = { Logger.debug("Logout success") },
                    error = { Logger.debug("Logout failed") }
            )
        }

        this.user = null
        this.userLibrary = null
        startActivity(LoginActivity.newInstance(this))
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

    private fun updateUser()
    {
        user ?: return
        GetUserUseCase().user { result ->
            result ?: return@user // TODO :: Determine what to do if failed to get user
            user = result.user
            // TODO :: Use case to sync local database?
            // Maybe have a pop up if there are inconsistencies ?
        }
    }

    companion object
    {
        private var TAG: String = MangaFeed::class.java.simpleName
        private var mangaApp: MangaFeed? = null
        val app get() = checkNotNull(mangaApp)
    }
}
