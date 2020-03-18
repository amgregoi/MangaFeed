package com.amgregoire.mangafeed.v2.extension

import android.util.Base64
import com.google.gson.Gson
import java.util.*

fun String.removeWhiteSpace() = this.replace("\\s".toRegex(), "")

fun String.base64Encode(flag: Int = Base64.DEFAULT) = Base64.encodeToString(this.toByteArray(), flag)
fun String.base64Decode(flag: Int = Base64.DEFAULT) = String(Base64.decode(this.toByteArray(), flag))

fun String.stringFormat(vararg input: Any) = String.format(Locale.getDefault(), this, *input)

inline fun <reified T : Any> T.toJson(): String = Gson().toJson(this, T::class.java)

inline fun <reified T : Any> String.fromJson(): T = Gson().fromJson(this, T::class.java)
