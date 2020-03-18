package com.amgregoire.mangafeed.v2.extension

import com.google.gson.Gson

fun Any.toJson() = Gson().toJson(this)