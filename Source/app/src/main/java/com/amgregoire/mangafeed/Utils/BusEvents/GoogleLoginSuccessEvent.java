package com.amgregoire.mangafeed.Utils.BusEvents;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

/**
 * Created by Andy Gregoire on 3/17/2018.
 */

public class GoogleLoginSuccessEvent
{
    public GoogleSignInAccount userAccount;

    public GoogleLoginSuccessEvent(GoogleSignInAccount account)
    {
        userAccount = account;
    }
}
