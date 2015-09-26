package com.eloviz.app;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;

public abstract class ALoginActivity extends AppCompatActivity {
    public abstract void launchDrawer(String accessToken, String tokenType, Integer expiresIn, String refreshToken);
    public abstract void launchDrawer();
    public abstract Boolean amILogin();
}
