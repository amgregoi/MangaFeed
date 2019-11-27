package com.amgregoire.mangafeed.Common

import com.amgregoire.mangafeed.Models.Manga

data class SyncStatusObject(val current:Int, val total:Int, val items:List<Manga>)