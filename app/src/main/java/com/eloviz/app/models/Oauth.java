package com.eloviz.app.models;

import android.app.Activity;
import android.content.SharedPreferences;

public class Oauth {
    String accessToken;
    String tokenType;
    Integer expiresIn;
    String refreshToken;

    public Oauth(String newAccessToken, String newTokenType, Integer newExpiresIn, String newRefreshToken) {
        accessToken = newAccessToken;
        tokenType = newTokenType;
        expiresIn = newExpiresIn;
        refreshToken = newRefreshToken;
    }

    public void save(Activity activity) {
        SharedPreferences settings = activity.getSharedPreferences("OauthFile", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("accessToken", accessToken);
        editor.putString("tokenType", tokenType);
        editor.putInt("expiresIn", expiresIn);
        editor.putString("refreshToken", refreshToken);
        editor.apply();
        //editor.commit();
    }

    public void delete(Activity activity) {
        SharedPreferences settings = activity.getSharedPreferences("OauthFile", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.apply();
    }
}
