package com.amgregoire.mangafeed.v2.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Named


@Module
class AppContextModule(
        @get:Provides
        @get:Named("ApplicationContext")
        val context: Context
)

@Module
class ContextModule(
        @get:Provides
        @get:Named("ActivityContext")
        val context: Context
)