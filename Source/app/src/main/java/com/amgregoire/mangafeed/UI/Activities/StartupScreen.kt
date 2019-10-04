package com.amgregoire.mangafeed.UI.Activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.amgregoire.mangafeed.Common.WebSources.FunManga
import com.amgregoire.mangafeed.Utils.NetworkService
import com.amgregoire.mangafeed.v2.service.CloudflareService

class StartupScreen : Activity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        CloudflareService().getCookies(FunManga.URL, NetworkService.defaultUserAgent){
            val intent = Intent(this, NavigationActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) //This will clear the backstack
            startActivity(intent)
        }

    }
}