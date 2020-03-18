package com.amgregoire.mangafeed.v2.service

import com.amgregoire.mangafeed.Common.WebSources.FunManga
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.Utils.MangaLogger
import com.amgregoire.mangafeed.Utils.NetworkService
import com.amgregoire.mangafeed.uiScope
import com.amgregoire.mangafeed.v2.BaseCookiePreferences
import com.amgregoire.mangafeed.v2.FunMangaCookiePreferences
import com.amgregoire.mangafeed.v2.cloudflare.Cloudflare
import kotlinx.coroutines.launch
import java.net.HttpCookie
import java.util.*

class CloudFlareService
{
    fun getCFCookies(url: String, userAgent: String, cookies: (List<String>) -> Unit)
    {
        if (MangaFeed.app.currentSource !is FunManga)
        {
            cookies.invoke(listOf())
            return
        }

        val cookiePrefs = MangaFeed.app.cookiePreferences()
        if (isValidCookie(cookiePrefs))
        {
            val oldCookies = cookiePrefs.cookies!!
            cookies.invoke(oldCookies.toList())
        }
        else
        {
            getCookies(url, userAgent) { newCookies ->
                cookies.invoke(newCookies)
            }
        }
    }

    fun getCookies(url: String = MangaFeed.app.currentSource.baseUrl, userAgent: String = NetworkService.defaultUserAgent, cookies: (List<String>) -> Unit)
    {
        val cookiePrefs = MangaFeed.app.cookiePreferences()

        if (isValidCookie(cookiePrefs))
        {
            val oldCookies = cookiePrefs.cookies!!
            uiScope.launch { cookies.invoke(oldCookies.toList()) }
            return
        }

        Cloudflare(url, userAgent).apply {
            user_agent = userAgent
            getCookies(object : Cloudflare.cfCallback
            {
                override fun onSuccess(httpCookieList: MutableList<HttpCookie>?, hasNewUrl: Boolean, newUrl: String?)
                {
                    Logger.debug("Retrieved new cookies = $httpCookieList")
                    val cookieList = Cloudflare.List2Map(httpCookieList).map { (k, v) -> "$k=$v" }
                    cookiePrefs.cookies = cookieList.toMutableSet()
                    cookiePrefs.setExpiresAt()

                    uiScope.launch { cookies.invoke(cookieList) }
                }

                override fun onFail()
                {
                    MangaLogger.logError("CloudflareService", "Failed to retrieve new cookies")
                    if (!isValidCookie(cookiePrefs))
                    {
                        cookiePrefs.clear()
                        cookies.invoke(listOf())
                    }
                    else
                    {
                        val oldCookies = cookiePrefs.cookies!!
                        uiScope.launch { cookies.invoke(oldCookies.toList()) }
                    }
                }
            })
        }
    }

    private fun isValidCookie(cookiePrefs: BaseCookiePreferences): Boolean
    {
        val oldCookies = cookiePrefs.cookies

        oldCookies ?: return false
        if (oldCookies.size < 2) return false

        if (cookiePrefs.expiresAt > Date().time)
        {
            return true
        }

        Logger.error("Cookie -> \nExpires=${cookiePrefs.expiresAt} -> \ncurrent=${Date().time}")
        return false
    }

    fun verifyCookieAndDoAction(action: () -> Unit)
    {
        if (!MangaFeed.app.currentSource.requiresCloudFlare())
        {
            action.invoke()
            return
        }

        val cookiePrefs = MangaFeed.app.cookiePreferences()
        if (isValidCookie(cookiePrefs))
        {
            action.invoke()
        }
        else
        {
            getCookies { cookies ->
                action.invoke()
            }
        }
    }
}