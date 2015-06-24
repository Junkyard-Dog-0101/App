package com.eloviz.app;

import android.support.v7.app.ActionBarActivity;

public abstract class ALoginActivity extends ActionBarActivity {
    public abstract void launchDrawer(String accessToken, String tokenType, Integer expiresIn, String refreshToken);
    public abstract void launchDrawer();
    public abstract Boolean amILogin();
}
