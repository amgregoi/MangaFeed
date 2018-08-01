package com.amgregoire.mangafeed.Utils;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

import com.amgregoire.mangafeed.MangaFeed;
import com.amgregoire.mangafeed.UI.Activities.NavigationActivity;
import com.amgregoire.mangafeed.Utils.BusEvents.GoogleLoginAttemptEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.GoogleLoginSuccessEvent;
import com.amgregoire.mangafeed.Utils.BusEvents.GoogleLogoutEvent;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Task;

/**
 * Created by Andy Gregoire on 3/17/2018.
 */

public class LoginManager
{
    public final static String TAG = LoginManager.class.getSimpleName();

    private static GoogleApiClient mGoogleApiClient;

    /***
     * This function initializes the google api client.
     *
     * @param activity
     */
    public static void init(NavigationActivity activity)
    {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(activity).enableAutoManage(activity, connectionResult ->
        {
            // do nothing
        }).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();
    }

    /***
     * This function handles login/logout interactions based on whether the app has any credentials(email) from previous logins.
     *
     * @param context
     */
    public static void interact(Context context)
    {
        if (SharedPrefs.getUserEmail() == null)
        {
            MangaFeed.getInstance().makeToastShort("Attempting to login");

//            MangaFeed.getInstance().rxBus().send(new GoogleLoginAttemptEvent());

            // TODO
            // Remove this, and uncomment above line when done testing login features
            // Login only succeeds with signed apks
            MangaFeed.getInstance().rxBus().send(new GoogleLoginSuccessEvent(null));
        }
        else
        {
            AlertDialog lDialog = new AlertDialog.Builder(context).create();
            lDialog.setTitle("Logout");
            lDialog.setMessage("Are you sure you want to logout?  \nLibrary will be reset until next login.");
            lDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No", (dialogInterface, i) -> lDialog.dismiss());
            lDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok", (dialogInterface, i) ->
            {
                MangaFeed.getInstance().rxBus().send(new GoogleLogoutEvent());
            });

            lDialog.show();
        }
    }

    /***
     * This function logs the user out of their google account, and resets local application credentials.
     *
     */
    public static void logout()
    {
        SharedPrefs.setUserEmail(null);
        SharedPrefs.setUserName(null);
        SharedPrefs.setUserId(-1);
        MangaDB.getInstance().resetLibrary();

        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
    }

    /***
     * This function starts the google services login activity.
     *
     * @param activity
     */
    public static void login(NavigationActivity activity)
    {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        activity.startActivityForResult(signInIntent, 8008);
    }

    /***
     * This function handles the login results from the google login activity.
     *
     * @param data
     */
    public static void loginResult(Intent data)
    {

        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try
        {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            MangaFeed.getInstance().rxBus().send(new GoogleLoginSuccessEvent(account));
        }
        catch (ApiException e)
        {
            MangaFeed.getInstance().makeToastShort("Failed to login");
            MangaLogger.logError(TAG, "failure code: " + e.getStatusCode());

        }
    }
}
