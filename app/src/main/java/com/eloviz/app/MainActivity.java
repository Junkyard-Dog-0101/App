package com.eloviz.app;


import android.content.res.Configuration;
import android.graphics.Color;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends ALoginActivity {
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private ArrayList<ADrawerFragment> mDrawerFragmentList = new ArrayList<>();
    private boolean isLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.contentFrame, new LoginFragment()).commit();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mToolbar != null && mDrawerToggle != null)
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
        if (id == R.id.action_settings) {
            return true;
        }
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        mDrawerList.setAdapter(new DrawerListAdapter(this, mDrawerFragmentList));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
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
    public void setTitle(CharSequence title) {
        mTitle = title;
        mToolbar.setTitle(mTitle);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        for (int i = 0; i < fragmentManager.getBackStackEntryCount(); ++i) {
            fragmentManager.popBackStack();
        }
        fragmentManager.beginTransaction().replace(R.id.contentFrame, mDrawerFragmentList.get(position)).commit();
        mDrawerList.setItemChecked(position, true);
        setTitle(mDrawerFragmentList.get(position).getName());
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    public void onLoginSuccess()
    {
        mTitle = mDrawerTitle = getTitle();
        ADrawerFragment toto = new StreamingListFragment();
        toto.setName("Accueil");
        toto.setIcon(R.drawable.home);
        mDrawerFragmentList.add(toto);

        ADrawerFragment tio = new StreamingListFragment();
        tio.setName("Mes Abonnements");
        tio.setIcon(R.drawable.streaming_list);
        mDrawerFragmentList.add(tio);

       /* StreamingFragment tiot = new StreamingFragment();
        tiot.setName("Mon Streaming");
        tiot.setIcon(R.drawable.streaming_user);
        mDrawerFragmentList.add(tiot);*/



        isLogin = true;
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.mainLayout);
        View view = LayoutInflater.from(this).inflate(R.layout.toolbar, null);
/*        <translate android:fromXDelta="0" android:toXDelta="-100%p" android:duration="300"/>*/
        Animation fadeIn = new AlphaAnimation(0, 1);
       // fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
        fadeIn.setDuration(1000);
        AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(fadeIn);
        view.setAnimation(animation);
        linearLayout.addView(view, 0);
        mToolbar = (Toolbar) view;
        initView();
        mToolbar.setTitle(getTitle());
        mToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mToolbar);
        initDrawer();
        mDrawerToggle.syncState();
        //FragmentManager fragmentManager = getSupportFragmentManager();
        //fragmentManager.beginTransaction().replace(R.id.contentFrame, new LoginFragment()).commit();
        /* faut pas que j'oublie de penser a regarde le problème sur le left drawer, il marche pas dans le relative layout */
        selectItem(0);
    }
}