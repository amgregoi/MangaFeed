package com.amgregoire.mangafeed.v2.ui

import android.app.Application
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider

class AppViewModelFactory(private var app: Application) : ViewModelProvider.Factory
{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T
    {
        return modelClass.getConstructor(Application::class.java).newInstance(app)
    }
}