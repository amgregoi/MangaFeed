package com.amgregoire.mangafeed.v2

import android.content.Context
import android.content.Context.MODE_PRIVATE
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private const val USER_PREFERENCES_NAME = "USER_PREFERENCES_NAME"
private const val PUSHER_PREFERENCES_NAME = "PUSHER_PREFERENCES_NAME"

class FunMangaCookiePreferences(context: Context)
{
    private val preferences = SharedPreferencesExtension(context.applicationContext, PUSHER_PREFERENCES_NAME)

    var cookies by preferences.stringSet("cookies")
    var expiresAt by preferences.long("expires_at")
        private set

    fun setExpiresAt() = run {
        expiresAt = Date().time + 43200000
    }

    fun clear()
    {
        preferences.clear()
    }
}

class UserPreferences(context: Context)
{

    private val preferences = SharedPreferencesExtension(context.applicationContext, USER_PREFERENCES_NAME)

    var isSignedIn by preferences.boolean("signed_in")
    var user by preferences.string("user")

    fun clear()
    {
        preferences.clear()
    }
}

private class SharedPreferencesExtension(context: Context, name: String)
{

    private val preferences = context.getSharedPreferences(name, MODE_PRIVATE)

    fun string(key: String) = object : ReadWriteProperty<Any?, String?>
    {
        override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) =
                preferences.edit().putString(key, value).apply()

        override operator fun getValue(thisRef: Any?, property: KProperty<*>): String? =
                preferences.getString(key, null)
    }

    fun stringSet(key: String) = object : ReadWriteProperty<Any?, MutableSet<String>?>
    {
        override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: MutableSet<String>?) =
                preferences.edit().putStringSet(key, value).apply()

        override operator fun getValue(thisRef: Any?, property: KProperty<*>): MutableSet<String>? =
                preferences.getStringSet(key, null)
    }

    fun boolean(key: String) = object : ReadWriteProperty<Any?, Boolean>
    {
        override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) =
                preferences.edit().putBoolean(key, value).apply()

        override operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean =
                preferences.getBoolean(key, false)
    }

    fun long(key: String) = object : ReadWriteProperty<Any?, Long>
    {
        override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Long) =
                preferences.edit().putLong(key, value).apply()

        override operator fun getValue(thisRef: Any?, property: KProperty<*>): Long =
                preferences.getLong(key, Date().time - 86400000) // Defaults to one day ago
    }

    fun clear()
    {
        preferences.edit().clear().apply()
    }
}