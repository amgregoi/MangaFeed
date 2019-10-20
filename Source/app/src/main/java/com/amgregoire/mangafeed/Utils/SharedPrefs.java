package com.amgregoire.mangafeed.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.amgregoire.mangafeed.Common.MangaEnums;
import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.R;

import java.util.Date;

import javax.inject.Inject;

public class SharedPrefs
{


    @Inject
    public SharedPrefs()
    {

    }

    /***
     * This function initializes the shared prefs.
     */
    public static void initializePreferences()
    {
        Context lContext = MangaFeed.Companion.getApp();

        // Need to set storage preference per device
        SharedPreferences lSharedPrefs = PreferenceManager.getDefaultSharedPreferences(lContext);
        if (lSharedPrefs.getString(lContext.getString(R.string.PREF_STORAGE_LOCATION), null) == null)
        {
            SharedPreferences.Editor editor = lSharedPrefs.edit();
            editor.putString(lContext.getString(R.string.PREF_STORAGE_LOCATION), lContext.getFilesDir()
                                                                                         .getAbsolutePath());
            editor.commit();
        }
    }

    /***
     * This function verifies if a user is signed in.
     * @return
     */
    public static boolean isSignedIn()
    {
        if (getUserEmail().contains("Guest"))
        {
            return false;
        }
        return true;
    }

    /**
     * This function retrieves the stored user name from the device.
     *
     * @return The users name.
     */
    public static String getUserName()
    {
        Context lContext = MangaFeed.Companion.getApp();
        return PreferenceManager.getDefaultSharedPreferences(lContext)
                                .getString(lContext.getString(R.string.PREF_USER_NAME), null);
    }

    /**
     * This function saves the current users name to the device.
     *
     * @param name The users name.
     */
    public static void setUserName(String name)
    {
        Context lContext = MangaFeed.Companion.getApp();
        SharedPreferences.Editor lEditor = PreferenceManager.getDefaultSharedPreferences(lContext)
                                                            .edit();
        lEditor.putString(lContext.getString(R.string.PREF_USER_NAME), name);
        lEditor.apply();
    }

    /**
     * This function retrieves the stored user email from the device.
     *
     * @return The users email.
     */
    public static String getUserEmail()
    {
        Context lContext = MangaFeed.Companion.getApp();
        return PreferenceManager.getDefaultSharedPreferences(lContext)
                                .getString(lContext.getString(R.string.PREF_USER_EMAIL), null);
    }

    /**
     * This function saves the current users email to the device.
     *
     * @param aEmail, The users email.
     */
    public static void setUserEmail(String aEmail)
    {
        Context lContext = MangaFeed.Companion.getApp();
        SharedPreferences.Editor lEditor = PreferenceManager.getDefaultSharedPreferences(lContext)
                                                            .edit();
        lEditor.putString(lContext.getString(R.string.PREF_USER_EMAIL), aEmail);
        lEditor.apply();
    }

    /**
     * This function saves the current users id to the device.
     *
     * @param id, The users id.
     */
    public static void setUserId(int id)
    {
        Context lContext = MangaFeed.Companion.getApp();
        SharedPreferences.Editor lEditor = PreferenceManager.getDefaultSharedPreferences(lContext)
                                                            .edit();
        lEditor.putInt(lContext.getString(R.string.PREF_USER_ID), id);
        lEditor.apply();
    }

    /**
     * This function retrieves the stored user id from the device.
     *
     * @return The users id.
     */
    public static int getUserId()
    {
        Context lContext = MangaFeed.Companion.getApp();
        return PreferenceManager.getDefaultSharedPreferences(lContext)
                                .getInt(lContext.getString(R.string.PREF_USER_ID), -1);
    }



    /**
     * TODO: Might remove.
     * Get the users application layout preferences
     *
     * @return The users App layout preference
     * True = GridLayout
     * False = LinearLayout
     */
    public static boolean getLayoutFormat()
    {
        Context lContext = MangaFeed.Companion.getApp();
        return PreferenceManager.getDefaultSharedPreferences(lContext)
                                .getBoolean(lContext.getString(R.string.PREF_APP_LAYOUT_IS_GRID), true);
    }

    /**
     * TODO: Might remove.
     * Set the users application layout preference
     *
     * @param aGrid, User preference for application layout
     *               True = GridLayout
     *               False = LinearLayout
     */
    public static void setLayoutFormat(boolean aGrid)
    {
        Context lContext = MangaFeed.Companion.getApp();
        SharedPreferences.Editor lEditor = PreferenceManager.getDefaultSharedPreferences(lContext)
                                                            .edit();
        lEditor.putBoolean(lContext.getString(R.string.PREF_APP_LAYOUT_IS_GRID), aGrid);
        lEditor.apply();
    }

    /**
     * Get the users application theme preference
     *
     * @return The users  application theme preference
     * True = Light theme
     * False = Dark theme
     */
    public static boolean isLightTheme()
    {
        Context lContext = MangaFeed.Companion.getApp();
        return PreferenceManager.getDefaultSharedPreferences(lContext)
                                .getBoolean(lContext.getString(R.string.PREF_APP_THEME_IS_LIGHT), false);
    }

    /**
     * Set the users application theme preference
     *
     * @param aLightTheme, User preference for application theme
     *                     True = Light theme
     *                     False = Dark theme
     */
    public static void setLayoutTheme(boolean aLightTheme)
    {
        Context lContext = MangaFeed.Companion.getApp();
        SharedPreferences.Editor lEditor = PreferenceManager.getDefaultSharedPreferences(lContext)
                                                            .edit();
        lEditor.putBoolean(lContext.getString(R.string.PREF_APP_THEME_IS_LIGHT), aLightTheme);
        lEditor.apply();
    }

    /***
     * This function retrieves the current source.
     *
     * @return
     */
    public static String getSavedSource()
    {
        Context lContext = MangaFeed.Companion.getApp();
        return PreferenceManager.getDefaultSharedPreferences(lContext)
                                .getString(lContext.getString(R.string.PREF_USER_SOURCE), MangaEnums.Source.FunManga
                                        .name());
    }

    /***
     * This function sets the current source.
     *
     * @param aSource
     */
    public static void setSavedSource(String aSource)
    {
        Context lContext = MangaFeed.Companion.getApp();
        SharedPreferences.Editor lEditor = PreferenceManager.getDefaultSharedPreferences(lContext)
                                                            .edit();
        lEditor.putString(lContext.getString(R.string.PREF_USER_SOURCE), aSource);
        lEditor.apply();
    }

    /***
     * This function returns the chapter vertical scroll setting.
     *
     * @return
     */
    public static boolean getChapterScrollVertical()
    {
        Context lContext = MangaFeed.Companion.getApp();
        return PreferenceManager.getDefaultSharedPreferences(lContext)
                                .getBoolean(lContext.getString(R.string.PREF_CHAPTER_SCROLL_VERTICAL), false);
    }

    /***
     * This function sets the chapter vertical scroll setting.
     *
     * @param aVertical
     */
    public static void setChapterScrollVertical(boolean aVertical)
    {
        Context lContext = MangaFeed.Companion.getApp();
        SharedPreferences.Editor lEditor = PreferenceManager.getDefaultSharedPreferences(lContext)
                                                            .edit();
        lEditor.putBoolean(lContext.getString(R.string.PREF_CHAPTER_SCROLL_VERTICAL), aVertical);
        lEditor.apply();
    }

    /***
     * This function returns the chapter screen orientation setting.
     *
     * @return true if LandScape, false otherwise
     */
    public static boolean getChapterScreenOrientation()
    {
        Context lContext = MangaFeed.Companion.getApp();
        return PreferenceManager.getDefaultSharedPreferences(lContext)
                                .getBoolean(lContext.getString(R.string.PREF_CHAPTER_SCREEN_ORIENTATION), false);
    }

    /***
     * This function sets the chapter screen orientation setting.
     *
     * @param aLandscape
     */
    public static void setChapterScreenOrientation(boolean aLandscape)
    {
        Context lContext = MangaFeed.Companion.getApp();
        SharedPreferences.Editor lEditor = PreferenceManager.getDefaultSharedPreferences(lContext)
                                                            .edit();
        lEditor.putBoolean(lContext.getString(R.string.PREF_CHAPTER_SCREEN_ORIENTATION), aLandscape);
        lEditor.apply();
    }

    /***
     * This function retrieves the date of the last catalog update that was run.
     *
     * @return date of catalog update.
     */
    public static Date getLastCatalogUpdate()
    {
        Context lContext = MangaFeed.Companion.getApp();
        return new Date(PreferenceManager.getDefaultSharedPreferences(lContext).getLong(lContext.getString(R.string.PREF_LAST_CATALOG_UPDATE), 0));
    }

    /***
     * This function sets the current date for the last catalog update.
     *
     */
    public static void setLastCatalogUpdate()
    {
        Context lContext = MangaFeed.Companion.getApp();
        SharedPreferences.Editor lEditor = PreferenceManager.getDefaultSharedPreferences(lContext)
                                                            .edit();
        lEditor.putLong(lContext.getString(R.string.PREF_LAST_CATALOG_UPDATE), new Date().getTime());
        lEditor.apply();
    }

}
