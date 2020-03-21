package com.amgregoire.mangafeed.Common

import com.amgregoire.mangafeed.v2.model.domain.Manga

data class SyncStatusObject(val current:Int, val total:Int, val items: List<Manga>)