package com.eloviz.app.webRTC;

import android.util.Log;

import io.socket.emitter.Emitter;
import io.socket.client.IO;
import io.socket.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class WebRTCSocket {

    private static WebRTCSocket mWebRTCSocket;
    private Socket mSocket;

    private WebRTCSocket() {

    }

    public static synchronized WebRTCSocket getInstance() {
        if (mWebRTCSocket == null) {
            mWebRTCSocket = new WebRTCSocket();
        }
        return mWebRTCSocket;
    }

    public void createSocket() throws URISyntaxException {
        mWebRTCSocket.mSocket = IO.socket("http://api.eloviz.com/meeting");
    }

    public void connect() {
        mSocket.connect();
    }

    public void sendRemoteMessage(String event, JSONObject data, String receiver) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("event", event);
            if (data != null)
                obj.put("data", data);
            if (receiver != null)
                obj.put("receiver", receiver);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("remoteMessage", obj);
        Log.e("send remoteMessage", obj.toString());
    }

    public void sendRemoteMessage(String sender, String roomName) {
        JSONObject obj = new JSONObject();
        try {
            if (sender != null)
                obj.put("sender", sender);
            if (roomName != null)
                obj.put("roomName", roomName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("joinRoom", obj);
        Log.e("send joinRoom", obj.toString());
    }

    public void on(String message, Emitter.Listener listener) {
        mSocket.on(message, listener);
    }
}
