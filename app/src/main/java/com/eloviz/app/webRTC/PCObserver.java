package com.eloviz.app.webRTC;

import android.app.Activity;
import android.util.Log;


import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.VideoRenderer;

public class PCObserver  {

    /*private final String LOG_TAG = this.getClass().getSimpleName();
    private String mId;
    private Activity mActivity;
    private VideoStreamsView mVideoStreamsView;

    PCObserver(String id, Activity activity, VideoStreamsView videoStreamsView) {
        mId = id;
        mActivity = activity;
        mVideoStreamsView = videoStreamsView;
    }

    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {

    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {

    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {

    }

    @Override
    public void onIceCandidate(final IceCandidate iceCandidate) {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                Log.e(LOG_TAG, "onIceCandiate");
                JSONObject obj = new JSONObject();
                JSONObject obj2 = new JSONObject();
                try {
                    obj2.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
                    obj2.put("sdpMid", iceCandidate.sdpMid);
                    obj2.put("candidate", iceCandidate.sdp);
                    obj.put("ice", obj2);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                WebRTCSocket.getInstance().sendRemoteMessage("iceCandidate", obj, mId);
            }
        });
    }

    @Override
    public void onError() {

    }

    @Override
    public void onAddStream(final MediaStream mediaStream) {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                Log.e(LOG_TAG, "onAddStream");
                try {
                    mediaStream.videoTracks.get(0).addRenderer(new VideoRenderer(new VideoCallbacks(mVideoStreamsView, VideoStreamsView.Endpoint.REMOTE)));
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onRemoveStream(final MediaStream mediaStream) {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mediaStream.videoTracks.get(0).dispose();
            }
        });
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {

    }

    @Override
    public void onRenegotiationNeeded() {

    }*/

}
