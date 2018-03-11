package com.amgregoire.mangafeed.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.amgregoire.mangafeed.Common.MangaEnums;
import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.R;

public class SharedPrefs
{


    /***
     * This function initializes the shared prefs.
     */
    public static void initializePreferences()
    {
        Context lContext = MangaFeed.getInstance();

        // Need to set storage preference per device
        SharedPreferences lSharedPrefs = PreferenceManager.getDefaultSharedPreferences(lContext);
        if (lSharedPrefs.getString(lContext.getString(R.string.PREF_STORAGE_LOCATION), null) == null)
        {
            SharedPreferences.Editor editor = lSharedPrefs.edit();
            editor.putString(lContext.getString(R.string.PREF_STORAGE_LOCATION), lContext.getFilesDir().getAbsolutePath());
            editor.commit();
        }
    }

    /***
     * This function verifies if a user is signed into google.
     * @return
     */
    public static boolean isSignedIn()
    {
        if (getGoogleEmail().contains("Guest")) return false;
        return true;
    }

    /**
     * Get the users Google email
     *
     * @return The users Google Email
     */
    public static String getGoogleEmail()
    {
        Context lContext = MangaFeed.getInstance();
        return PreferenceManager.getDefaultSharedPreferences(lContext)
                                .getString(lContext.getString(R.string.PREF_GOOGLE_EMAIL), "Guest (Sign in)");
    }

    /**
     * Sets the users MyAnimeList(MAL) login credentials for authorized API calls
     *
     * @param aEmail, The users Google email
     */
    public static void setGoogleEmail(String aEmail)
    {
        Context lContext = MangaFeed.getInstance();
        SharedPreferences.Editor lEditor = PreferenceManager.getDefaultSharedPreferences(lContext).edit();
        lEditor.putString(lContext.getString(R.string.PREF_GOOGLE_EMAIL), aEmail);
        lEditor.apply();
    }

    /**
     * Get the users application layout preferences
     *
     * @return The users App layout preference
     * True = GridLayout
     * False = LinearLayout
     */
    public static boolean getLayoutFormat()
    {
        Context lContext = MangaFeed.getInstance();
        return PreferenceManager.getDefaultSharedPreferences(lContext)
                                .getBoolean(lContext.getString(R.string.PREF_APP_LAYOUT_IS_GRID), true);
    }

    /**
     * Set the users application layout preference
     *
     * @param aGrid, User preference for application layout
     *               True = GridLayout
     *               False = LinearLayout
     */
    public static void setLayoutFormat(boolean aGrid)
    {
        Context lContext = MangaFeed.getInstance();
        SharedPreferences.Editor lEditor = PreferenceManager.getDefaultSharedPreferences(lContext).edit();
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
    public static boolean getLayoutTheme()
    {
        Context lContext = MangaFeed.getInstance();
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
        Context lContext = MangaFeed.getInstance();
        SharedPreferences.Editor lEditor = PreferenceManager.getDefaultSharedPreferences(lContext).edit();
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
        Context lContext = MangaFeed.getInstance();
        return PreferenceManager.getDefaultSharedPreferences(lContext)
                                .getString(lContext.getString(R.string.PREF_USER_SOURCE), MangaEnums.Source.FunManga.name());
    }

    /***
     * This function sets the current source.
     *
     * @param aSource
     */
    public static void setSavedSource(String aSource)
    {
        Context lContext = MangaFeed.getInstance();
        SharedPreferences.Editor lEditor = PreferenceManager.getDefaultSharedPreferences(lContext).edit();
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
        Context lContext = MangaFeed.getInstance();
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
        Context lContext = MangaFeed.getInstance();
        SharedPreferences.Editor lEditor = PreferenceManager.getDefaultSharedPreferences(lContext).edit();
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
        Context lContext = MangaFeed.getInstance();
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
        Context lContext = MangaFeed.getInstance();
        SharedPreferences.Editor lEditor = PreferenceManager.getDefaultSharedPreferences(lContext).edit();
        lEditor.putBoolean(lContext.getString(R.string.PREF_CHAPTER_SCREEN_ORIENTATION), aLandscape);
        lEditor.apply();
    }

}
