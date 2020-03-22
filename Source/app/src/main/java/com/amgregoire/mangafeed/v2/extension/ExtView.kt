package com.amgregoire.mangafeed.v2.extension

import android.view.View

fun View.visible() = run { this.visibility = View.VISIBLE }
fun View.gone() = run { this.visibility = View.GONE }