package com.eloviz.app;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.eloviz.app.webRTC.WebRTCStreamFragment;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;


import cz.msebera.android.httpclient.Header;
import org.json.JSONException;
import org.json.JSONObject;

public class ConfigStreamFragment extends ADrawerFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_config_stream, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
   //             int stackHeight = getActivity().getSupportFragmentManager().getBackStackEntryCount();
                //   if (stackHeight > 0) { // if we have something on the stack (doesn't include the current shown fragment)
                //     getActivity().getActionBar().setHomeButtonEnabled(true);
                //   getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
                // } else {
                //   getActivity().getActionBar().setDisplayHomeAsUpEnabled(false);
                //  getActivity().getActionBar().setHomeButtonEnabled(false);
                //}
            }

        });
        //get list
        Button b = (Button) getActivity().findViewById(R.id.validateConfButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                final Fragment fragment = new WebRTCStreamFragment();
                EditText editText = (EditText) getActivity().findViewById(R.id.textInputRoom);
                final Bundle bundle = new Bundle();
                if (editText.getText().toString().equals("")) {

                } else {

                    Log.e("lol", editText.getText().toString());/*  tester demain */
                }

                RequestParams params = new RequestParams();
                params.add("title", editText.getText().toString());

                AppRestClient.post("/streams", params, new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        Log.e("pute", "onsuccesJSON");
                        try {
                            bundle.putString("room", response.getString("title"));
                        } catch (JSONException e) {
                            bundle.putString("room", "simplechat");
                            e.printStackTrace();
                        }
                        View view = getActivity().getCurrentFocus();
                        if (view != null) {
                            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        }
                        fragment.setArguments(bundle);
                        //fragmentManager.bac
                        fragmentManager.beginTransaction().add(R.id.contentFrame, fragment).addToBackStack("fragBack").commit();
                        //   accessToken = response.getString("access_token");
                        //   tokenType = response.getString("token_type");
                        ///   expiresIn = response.getInt("expires_in");
                        ///   refreshToken = response.getString("refresh_token");
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String string, Throwable exception) {
                        Log.e("statusCode", String.valueOf(statusCode));
                        Log.e(string, string);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                        //called when response HTTP status is "4XX" (eg. 401, 403, 404)
                        Log.e("toto", String.valueOf(statusCode));
//                HttpResponseException hre = (HttpResponseException) e;
                        //              int statusCode = hre.getStatusCode();
                    }

                    @Override
                    public void onRetry(int retryNo) {
                        // called when request is retried
                    }
                });


            }
        });

        //mDrawerList.setItemChecked(position, true);
        //setTitle(mDrawerFragmentList.get(position).getName());
        // mDrawerLayout.closeDrawer(mDrawerList);

    }
}
