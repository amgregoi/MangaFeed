package com.amgregoire.mangafeed.v2.service

import android.util.Log
import com.amgregoire.mangafeed.Common.WebSources.FunManga
import com.amgregoire.mangafeed.MangaFeed
import com.amgregoire.mangafeed.Utils.MangaLogger
import com.amgregoire.mangafeed.v2.cloudflare.Cloudflare
import java.net.HttpCookie
import java.util.*

class CloudflareService
{
    fun getCFCookies(url: String, userAgent: String, cookies: (List<String>) -> Unit)
    {
        if(MangaFeed.app.currentSource !is FunManga)
        {
            cookies.invoke(listOf())
            return
        }

        val cookiePrefs = MangaFeed.app.cookiePreferences
        val oldCookies = cookiePrefs.cookies
        if (oldCookies != null && cookiePrefs.expiresAt > Date().time)
        {
            MangaLogger.logError("CloudflareService", "Using old cookies")
            cookies.invoke(oldCookies.toList())
            return
        }

        MangaLogger.logError("CloudflareService", "fetching new cookies")
    }

    fun getCookies(url: String, userAgent: String, cookies: (List<String>) -> Unit)
    {
        Cloudflare(url).apply {
            user_agent = userAgent
            getCookies(object : Cloudflare.cfCallback
            {
                override fun onSuccess(httpCookieList: List<HttpCookie>)
                {
                    MangaLogger.logError("CloudflareService", "Retrieved new cookies", "${httpCookieList}")

                    var cookieList = Cloudflare.List2Map(httpCookieList).map { (k, v) -> "$k=$v" }
                    //cookieList = cookieList.plus("User-Agent=$userAgent")

                    val cookiePrefs = MangaFeed.app.cookiePreferences

                    cookiePrefs.cookies = cookieList.toMutableSet()
                    cookiePrefs.setExpiresAt()
                    cookies.invoke(cookieList)
                }

                override fun onFail()
                {
                    MangaLogger.logError("CloudflareService", "Failed to retrieve new cookies")
                    cookies.invoke(listOf())
                }
            })
        }
    }
}