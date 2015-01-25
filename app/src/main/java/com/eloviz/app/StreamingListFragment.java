package com.eloviz.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class StreamingListFragment extends ADrawerFragment {

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

        //get list

        super.onActivityCreated(savedInstanceState);
    }
}