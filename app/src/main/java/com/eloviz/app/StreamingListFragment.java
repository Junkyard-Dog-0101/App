package com.eloviz.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.apache.http.client.HttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.PeerConnection;

public class StreamingListFragment extends ADrawerFragment {

    JSONArray mData = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_private_streaming_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        AppRestClient.get("api/streams", null, new JsonHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.e("pute", "onsuccesARRAY");
                mData = response;
                ListView listView = (ListView) getActivity().findViewById(R.id.StreamingListView);
                listView.setAdapter(new StreamingListViewAdapter(getActivity(), response));
                listView.setOnItemClickListener(new StreamingItemClickListener());
                //          Lis    getActivity().findViewById(R.id.textViewStreamingItem);
                //   accessToken = response.getString("access_token");
                //     tokenType = response.getString("token_type");
                ///  expiresIn = response.getInt("expires_in");
                /// refreshToken = response.getString("refresh_token");
            }



            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.e("pute", "onsuccesJSON");
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
    private void selectItem(int position) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        Fragment fragment = new WebRTCStreamFragment();
        EditText editText = (EditText) getActivity().findViewById(R.id.textInputRoom);
        Bundle bundle = new Bundle();

            try {
                bundle.putString("room", ((JSONObject) mData.get(position)).getString("title"));
                Log.e("lol", ((JSONObject) mData.get(position)).getString("title"));
            } catch (JSONException e) {
                Log.e("pute", "lol");
                e.printStackTrace();
                bundle.putString("room", "simplechat");
            }
            /*  tester demain */

        fragment.setArguments(bundle);
        //fragmentManager.bac
        fragmentManager.beginTransaction().add(R.id.contentFrame, fragment).addToBackStack("fragBack").commit();
    }
    private class StreamingItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }
}