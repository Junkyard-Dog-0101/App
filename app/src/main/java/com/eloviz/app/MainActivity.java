package com.eloviz.app;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eloviz.app.models.Oauth;
import io.socket.client.Socket;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import cz.msebera.android.httpclient.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AMainActivity {
    static Socket socket = null;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;
    private RelativeLayout mDrawerRelativeLayout;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private ArrayList<ADrawerFragment> mDrawerFragmentList = new ArrayList<>();
    private boolean isLogin = false;
    private Oauth token = null;

    public void login(String accessToken, String tokenType, Integer expiresIn, String refreshToken, String login) {
        token = new Oauth(accessToken, tokenType, expiresIn, refreshToken);
        final TextView textView = (TextView) findViewById(R.id.textLogin);
        RequestParams params;
        AppRestClient.setOAuthHeader(accessToken);
        AppRestClient.get("/me", new JsonHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                // Log.e("toto", timeline.toString());
                Log.e("toto", response.toString());
                try {
                    textView.setText("Bienvenue " + response.get("username"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //  AMainActivity mainActivity = (AMainActivity) getActivity();
                /*String accessToken = null, tokenType = null, refreshToken = null;
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
                }*/
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
        token.save(this);
        isLogin = true;
    }

    public void login() {
        isLogin = false;
    }

 /*   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        if (mToolbar != null) {
            mToolbar.setTitle("Navigation Drawer");
            setSupportActionBar(toolbar);
        }
        initDrawer();
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        if (mToolbar != null) {
            mToolbar.setTitle(getTitle());
            mToolbar.setTitleTextColor(Color.WHITE);
            setSupportActionBar(mToolbar);
        }
        initDrawer();
       // mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        //mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        selectItem(0);
        //FragmentManager fragmentManager = getSupportFragmentManager();
        //fragmentManager.beginTransaction().replace(R.id.contentFrame, new LoginFragment()).commit();
        final Button registerButton = (Button) findViewById(R.id.btn_register);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.contentFrame, new RegisterFragment()).commit();
                setTitle(R.string.register);
                mDrawerLayout.closeDrawer(mDrawerRelativeLayout);
            }
        });

        final Button loginButton = (Button) findViewById(R.id.btn_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.contentFrame, new LoginFragment()).commit();
                setTitle(R.string.login);
                mDrawerLayout.closeDrawer(mDrawerRelativeLayout);
            }
        });
    }


    private void initView() {

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
//        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        loadDrawerData();
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new DrawerListAdapter(this, mDrawerFragmentList));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerRelativeLayout = (RelativeLayout) findViewById(R.id.left_drawer_relative_layout);
        // mToolbar.setDisplayHomeAsUpEnabled(true);
        //mToolbar.setHomeButtonEnabled(true);
    }

    private void initDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                mToolbar.setTitle(mTitle);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                mToolbar.setTitle(mDrawerTitle);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return id == R.id.action_settings || mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        mToolbar.setTitle(mTitle);
    }

    private void selectItem(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        for (int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i) {
            fragmentManager.popBackStack();
        }
        fragmentManager.beginTransaction().replace(R.id.contentFrame, mDrawerFragmentList.get(position)).commit();
        mDrawerList.setItemChecked(position, true);
        setTitle(mDrawerFragmentList.get(position).getName());
        mDrawerLayout.closeDrawer(mDrawerRelativeLayout);
    }

    private void loadDrawerData() {
        mTitle = mDrawerTitle = getTitle();
        ADrawerFragment toto = new StreamingListFragment();
        toto.setName("Accueil");
        toto.setIcon(R.drawable.home_pink);
        mDrawerFragmentList.add(toto);

       // ADrawerFragment tia = new WebRTCStreamFragment();
        ADrawerFragment tia = new ConfigStreamFragment();
        tia.setName("Configuration de Salle");
        tia.setIcon(R.drawable.home_pink);
        mDrawerFragmentList.add(tia);

    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }
}