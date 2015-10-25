package com.eloviz.app;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.loopj.android.http.*;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final ImageView logo = (ImageView) getActivity().findViewById(R.id.logo_eloviz);

        final EditText password = (EditText) getActivity().findViewById(R.id.password_input);
        password.setTypeface(Typeface.MONOSPACE);

        final EditText login = (EditText) getActivity().findViewById(R.id.login_input);
        login.setTypeface(Typeface.MONOSPACE);

        final Button connectionButton = (Button) getActivity().findViewById(R.id.login_button);

        DisplayMetrics displayMetrics = getActivity().getBaseContext().getResources().getDisplayMetrics();
        float heightDp = displayMetrics.heightPixels / displayMetrics.density;

        Configuration configuration = getResources().getConfiguration();
        int availableHeightDp = configuration.screenHeightDp;

        float availableDensity = (displayMetrics.density * availableHeightDp) / heightDp;
        float posMidElo = ((availableHeightDp / 2) - 60) / 2;
        float finalPos = 60 + posMidElo;

        final float pixelcount = finalPos * availableDensity;
        final TranslateAnimation animation = new TranslateAnimation(0, 0, pixelcount, pixelcount);
/*
        animation.setDuration(1500);
        animation.setAnimationListener(new TranslateAnimation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                TranslateAnimation animation2 = new TranslateAnimation(0, 0, pixelcount, 0);
                animation2.setDuration(800);
                animation2.setAnimationListener(new TranslateAnimation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        logo.setVisibility(View.VISIBLE);
                    }
                });
                getActivity().findViewById(R.id.logoEloviz).startAnimation(animation2);
            }
        });

        getActivity().findViewById(R.id.logoEloviz).startAnimation(animation);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                login.setAnimation(AnimationUtils.loadAnimation(getActivity().getBaseContext(), R.anim.fade_in));
                login.setVisibility(View.VISIBLE);

                password.setAnimation(AnimationUtils.loadAnimation(getActivity().getBaseContext(), R.anim.fade_in));
                password.setVisibility(View.VISIBLE);

                connectionButton.setVisibility(View.VISIBLE);
                connectionButton.setAnimation(AnimationUtils.loadAnimation(getActivity().getBaseContext(), R.anim.fade_in));

                skipButton.setVisibility(View.VISIBLE);
                skipButton.setAnimation(AnimationUtils.loadAnimation(getActivity().getBaseContext(), R.anim.fade_in));
                tryLogin();
            }
        }, 2600);*/

      /* skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, DrawerActivity.class);*/
        // startActivity(new Intent(this, Activity2.class));
//                overridePendingTransition(R.anim.push_down_in,R.anim.push_down_out);
               /* startActivity(i);
                overridePendingTransition(R.anim.push_up_in,R.anim.push_down_out);
                finish();
            }
        });*/
        connectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestParams params = new RequestParams();
                final String mLogin = login.getText().toString();
                params.put("username", login.getText().toString());
                params.put("grant_type", "password");
                params.put("password", password.getText().toString());

                Log.e("username", mLogin);
                Log.e("password", password.getText().toString());
                // params.put("refresh_token", "");
                //params.put("scope", "data");

                AppRestClient.post("/oauth/access_token", params, new JsonHttpResponseHandler() {

                    @Override
                    public void onStart() {
                        // called before request is started
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        // Log.e("toto", timeline.toString());
                       AMainActivity mainActivity = (AMainActivity) getActivity();
                        String accessToken = null, tokenType = null, refreshToken = null;
                        Integer expiresIn = null;
                        try {
                            accessToken = response.getString("access_token");
                            tokenType = response.getString("token_type");
                            expiresIn = response.getInt("expires_in");
                            refreshToken = response.getString("refresh_token");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (accessToken != null && tokenType != null && expiresIn != null && refreshToken != null) {
                            mainActivity.login(accessToken, tokenType, expiresIn, refreshToken, mLogin);
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                        //called when response HTTP status is "4XX" (eg. 401, 403, 404)
//                        Log.e("toto", errorResponse.toString());
                    }

                    @Override
                    public void onRetry(int retryNo) {
                        // called when request is retried
                    }
                });


            }
        });


      /*  final Button registerButton = (Button) getActivity().findViewById(R.id.btn_register);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.contentFrame, new RegisterFragment()).commit();
            }
        });*/


        /*skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ALoginActivity mainActivity = (ALoginActivity) getActivity();
                mainActivity.launchDrawer();
            }
        });*/

        //boolean silent =



    }

    void tryLogin() {
        SharedPreferences settings = getActivity().getSharedPreferences("OauthFile", 0);
        RequestParams params = new RequestParams();
        // params.put("username", login.getText());
        params.put("grant_type", "refresh_token");
        params.put("refresh_token", settings.getString("accessToken", null));
        // params.put("refresh_token", "");
        //params.put("scope", "data");

        AppRestClient.post("oauth/access_token", params, new JsonHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // Log.e("toto", timeline.toString());
                AMainActivity mainActivity = (AMainActivity) getActivity();
                String accessToken = null, tokenType = null, refreshToken = null;
                Integer expiresIn = null;
                try {
                    accessToken = response.getString("access_token");
                    tokenType = response.getString("token_type");
                    expiresIn = response.getInt("expires_in");
                    refreshToken = response.getString("refresh_token");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (accessToken != null && tokenType != null && expiresIn != null && refreshToken != null) {
                   // mainActivity.login(accessToken, tokenType, expiresIn, refreshToken);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                //called when response HTTP status is "4XX" (eg. 401, 403, 404)
              //  Log.e("toto", errorResponse.toString());
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });
    }
    //  ProgressDialog progress;




   /* public void login() throws JSONException {
        ElovizRestClient.get("login", null, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                progress.dismiss();
                // check page return
                Intent i = new Intent(LoginActivity.this, DrawerActivity.class);
                startActivity(i);
                overridePendingTransition(R.anim.push_up_in,R.anim.push_down_out);
                finish();
            }
        });
    }*/
}
