package com.eloviz.app.webRTC;

import android.util.Log;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

public class SDPObserver implements SdpObserver {
    private final String LOG_TAG = this.getClass().getSimpleName();

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {

    }

    @Override
    public void onSetSuccess() {

    }

    @Override
    public void onCreateFailure(String s) {
        Log.e(LOG_TAG, s);
    }

    @Override
    public void onSetFailure(String s) {
        Log.e(LOG_TAG, s);
    }
}
