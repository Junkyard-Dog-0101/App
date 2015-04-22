package com.eloviz.app;


import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.StatsObserver;
import org.webrtc.StatsReport;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRenderer.I420Frame;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WebRTCStreamFragment extends ADrawerFragment {
    private static final String LOG_TAG = "WebRTCStreamFragment";
    static Socket socket = null;
    private final Boolean[] quit = new Boolean[]{false};
    List<PeerConnection.IceServer> mIceServerList = new LinkedList<>();
    Map<String, PeerConnection> mPeersMap = new HashMap<>();
    MediaStream lMS;
    PeerConnectionFactory factory;


    private VideoStreamsView vsv;

    private static void abortUnless(boolean condition, String msg) {
        if (!condition) {
            throw new RuntimeException(msg);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //getActivity().getWindowManager().getDefaultDisplay().getSize(displaySize);
        //vsv = new VideoStreamsView(getActivity(), displaySize);

        return inflater.inflate(R.layout.fragment_webrtc, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e(LOG_TAG, "onAcvityCreated");


        Point displaySize = new Point();
        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.vsvLayout);
        Log.e(LOG_TAG, "width" + layout.getMeasuredWidth());
        getActivity().getWindowManager().getDefaultDisplay().getSize(displaySize);
        displaySize.y = 600;

        vsv = new VideoStreamsView(getActivity(), displaySize);
        //  vsv.setEGLConfigChooser(8 , 8, 8, 8, 16, 0);
        layout.addView(vsv);


       /* AppRestClient.get("http://webrtc.dennajort.fr/", "iceServers.json", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JSONArray responseArray = null;
                try {
                    responseArray = new JSONArray("{ \"iceServers\": [ {\"url\": \"stun:stun01.sipphone.com\"}, {\"url\": \"stun:stun.ekiga.net\"}, {\"url\": \"stun:stun.fwdnet.net\"}, {\"url\": \"stun:stun.ideasip.com\"}, {\"url\": \"stun:stun.iptel.org\"}, {\"url\": \"stun:stun.rixtelecom.se\"}, {\"url\": \"stun:stun.schlund.de\"}, {\"url\": \"stun:stun.l.google.com:19302\"}, {\"url\": \"stun:stun1.l.google.com:19302\"}, {\"url\": \"stun:stun2.l.google.com:19302\"}, {\"url\": \"stun:stun3.l.google.com:19302\"}, {\"url\": \"stun:stun4.l.google.com:19302\"}, {\"url\": \"stun:stunserver.org\"}, {\"url\": \"stun:stun.softjoys.com\"}, {\"url\": \"stun:stun.voiparound.com\"}, {\"url\": \"stun:stun.voipbuster.com\"}, {\"url\": \"stun:stun.voipstunt.com\"}, {\"url\": \"stun:stun.voxgratia.org\"}, {\"url\": \"stun:stun.xten.com\"}, {\"url\": \"turn:numb.viagenie.ca\", \"credential\": \"muazkh\", \"username\": \"webrtc@live.com\"}, {\"url\": \"turn:192.158.29.39:3478?transport=udp\", \"credential\": \"JZEOEt2V3Qb0y27GRntt2u2PAYA=\", \"username\": \"28224511:1379330808\"}, {\"url\": \"turn:192.158.29.39:3478?transport=tcp\", \"credential\": \"JZEOEt2V3Qb0y27GRntt2u2PAYA=\", \"username\": \"28224511:1379330808\"} ] }");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    responseArray = (JSONArray) response.get("iceServers");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < responseArray.length(); i++) {
                    try {
                        mIceServerList.add(new PeerConnection.IceServer(responseArray.getJSONObject(i).getString("url")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                socket.connect();
                Log.e(LOG_TAG, "startStream");
                socket.emit("startStream");

            }
        });*/


        try {
            socket = IO.socket("http://eloviz.com:3000/simplechat");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

      /*  JSONObject response = new JSONObject();
        try {
            response.getJSONObject("{" +
                    "  \"iceServers\": [" +
                    "    {\"url\": \"stun:stun01.sipphone.com\"}," +
                    "    {\"url\": \"stun:stun.ekiga.net\"}," +
                    "    {\"url\": \"stun:stun.fwdnet.net\"}," +
                    "    {\"url\": \"stun:stun.ideasip.com\"}," +
                    "    {\"url\": \"stun:stun.iptel.org\"}," +

                    "    {\"url\": \"stun:stun.rixtelecom.se\"}," +
                    "    {\"url\": \"stun:stun.schlund.de\"}," +
                    "    {\"url\": \"stun:stun.l.google.com:19302\"}," +
                    "    {\"url\": \"stun:stun1.l.google.com:19302\"}," +
                    "    {\"url\": \"stun:stun2.l.google.com:19302\"}," +
                    "    {\"url\": \"stun:stun3.l.google.com:19302\"}," +
                    "    {\"url\": \"stun:stun4.l.google.com:19302\"}," +
                    "    {\"url\": \"stun:stunserver.org\"}," +
                    "    {\"url\": \"stun:stun.softjoys.com\"}," +
                    "    {\"url\": \"stun:stun.voiparound.com\"}," +
                    "    {\"url\": \"stun:stun.voipbuster.com\"}," +
                    "    {\"url\": \"stun:stun.voipstunt.com\"}," +
                    "    {\"url\": \"stun:stun.voxgratia.org\"}," +
                    "    {\"url\": \"stun:stun.xten.com\"}," +




                    "    {\"url\": \"turn:numb.viagenie.ca\", \"credential\": \"muazkh\", \"username\": \"webrtc@live.com\"}," +
                    "    {\"url\": \"turn:192.158.29.39:3478?transport=udp\", \"credential\": \"JZEOEt2V3Qb0y27GRntt2u2PAYA=\", \"username\": \"28224511:1379330808\"}," +
                    "    {\"url\": \"turn:192.158.29.39:3478?transport=tcp\", \"credential\": \"JZEOEt2V3Qb0y27GRntt2u2PAYA=\", \"username\": \"28224511:1379330808\"}" +
                    "  ]" +
                    "}");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //response.

        Log.e("ssfs", "sfsfsf");

        JSONArray responseArray = new JSONArray();

        try {
            responseArray = (JSONArray) response.get("iceServers");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            Log.e("sefsef", "sfsfsf");
            Log.e(LOG_TAG, responseArray.getJSONObject(0).getString("url").toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        finally {
            Log.e("finaly", "sfsfsf");
        }
*/

        mIceServerList.add(new PeerConnection.IceServer("stun:stun01.sipphone.com"));
        mIceServerList.add(new PeerConnection.IceServer("stun:stun.ekiga.net"));
        mIceServerList.add(new PeerConnection.IceServer("stun:stun.fwdnet.net"));
        mIceServerList.add(new PeerConnection.IceServer("stun:stun.ideasip.com"));
        mIceServerList.add(new PeerConnection.IceServer("stun:stun.iptel.org"));
        mIceServerList.add(new PeerConnection.IceServer("stun:stun.rixtelecom.se"));
        mIceServerList.add(new PeerConnection.IceServer("stun:stun.schlund.de"));
        mIceServerList.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
        mIceServerList.add(new PeerConnection.IceServer("stun:stun1.l.google.com:19302"));
        mIceServerList.add(new PeerConnection.IceServer("stun:stun2.l.google.com:19302"));
        mIceServerList.add(new PeerConnection.IceServer("stun:stun3.l.google.com:19302"));
        mIceServerList.add(new PeerConnection.IceServer("stun:stun4.l.google.com:19302"));
        mIceServerList.add(new PeerConnection.IceServer("stun:stunserver.org"));
        mIceServerList.add(new PeerConnection.IceServer("stun:stun.softjoys.com"));
        mIceServerList.add(new PeerConnection.IceServer("stun:stun.voiparound.com"));
        mIceServerList.add(new PeerConnection.IceServer("stun:stun.voipbuster.com"));
        mIceServerList.add(new PeerConnection.IceServer("stun:stun.voipstunt.com"));
        mIceServerList.add(new PeerConnection.IceServer("stun:stun.voxgratia.org"));
        mIceServerList.add(new PeerConnection.IceServer("stun:stun.xten.com"));
        mIceServerList.add(new PeerConnection.IceServer("stun:stun.voipstunt.com"));



        PeerConnectionFactory.initializeAndroidGlobals(getActivity());
        factory = new PeerConnectionFactory();





        Log.e(LOG_TAG, "Creating local video source...");

        VideoCapturer capturer = VideoCapturer.create("Camera 1, Facing front, Orientation 270", 0);
        if (capturer == null) {
            capturer = VideoCapturer.create("Camera 0, Facing front, Orientation 270", 0);
        }
        // abortUnless(capturer != null, "Failed to open capturer");
        VideoSource videoSource = factory.createVideoSource(capturer, new MediaConstraints());
        lMS = factory.createLocalMediaStream("ARDAMS");
        VideoTrack videoTrack = factory.createVideoTrack("ARDAMSv0", videoSource);
        videoTrack.addRenderer(new VideoRenderer(new VideoCallbacks(vsv, VideoStreamsView.Endpoint.LOCAL)));
        lMS.addTrack(videoTrack);
        //lMS.addTrack(factory.createAudioTrack("ARDAMSa0", new AudioSource(0)));



        socket.on("newPeer", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                JSONObject id = (JSONObject) args[0];
                String buf;
                try {
                    buf = id.getString("id");
                } catch (JSONException e) {
                    buf = null;
                    e.printStackTrace();
                }
                final String idString = buf;
                Log.e(LOG_TAG, "newPeer");
                Log.e(LOG_TAG, "data.id : " + idString);
                Log.e(LOG_TAG, mIceServerList.get(0).uri);


//                PCObserver pcObserver = new PCObserver(idString);
                final PeerConnection peer;
                peer = factory.createPeerConnection(mIceServerList, new MediaConstraints(),
                        new PeerConnection.Observer() {
                            @Override
                            public void onIceCandidate(final IceCandidate candidate) {
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        Log.e(LOG_TAG, "onIceCandiate");
                                        JSONObject obj = new JSONObject();
                                        try {
                                            obj.put("to", idString);
                                            obj.put("ice", candidate);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        socket.emit("sendIce", obj);
                                    }
                                });
                            }

                            @Override
                            public void onError() {
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        throw new RuntimeException("PeerConnection error!");
                                    }
                                });
                            }

                            @Override
                            public void onSignalingChange(PeerConnection.SignalingState newState) {
                            }

                            @Override
                            public void onIceConnectionChange(
                                    PeerConnection.IceConnectionState newState) {
                            }

                            @Override
                            public void onIceGatheringChange(
                                    PeerConnection.IceGatheringState newState) {
                            }

                            @Override
                            public void onAddStream(final MediaStream stream) {
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        Log.e(LOG_TAG, "onAddStream");
                                        stream.videoTracks.get(0).addRenderer(new VideoRenderer(new VideoCallbacks(vsv, VideoStreamsView.Endpoint.REMOTE)));
                                    }
                                });
                            }

                            @Override
                            public void onRemoveStream(final MediaStream stream) {
                               getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        stream.videoTracks.get(0).dispose();
                                    }
                                });
                            }

                            @Override
                            public void onDataChannel(DataChannel dataChannel) {

                            }

                            @Override
                            public void onRenegotiationNeeded() {

                            }
                        }
                );

                Log.e(LOG_TAG, "addStream");
                peer.addStream(lMS, new MediaConstraints());

                MediaConstraints constraints = new MediaConstraints();
                constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"));
                constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));


                // SDPObserver sdpObserver = new SDPObserver(idString);
                //peer.addStream(lMS)
                peer.createOffer(new SdpObserver() {
                    @Override
                    public void onCreateSuccess(final SessionDescription sessionDescription) {
                        peer.setLocalDescription(new SdpObserver() {
                            @Override
                            public void onCreateSuccess(SessionDescription sessionDescription) {

                            }

                            @Override
                            public void onSetSuccess() {
                                Log.e(LOG_TAG, "Successfully setted to %s" + idString);

                                JSONObject obj = new JSONObject();
                                try {
                                    obj.put("to", idString);
                                    obj.put("offer", sessionDescription);
                                    Log.e(LOG_TAG, sessionDescription.description);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                socket.emit("sendOffer", obj);

                            }

                            @Override
                            public void onCreateFailure(String s) {

                            }

                            @Override
                            public void onSetFailure(String s) {

                            }
                        }, sessionDescription);

                    }

                    @Override
                    public void onSetSuccess() {

                    }

                    @Override
                    public void onCreateFailure(String s) {

                    }

                    @Override
                    public void onSetFailure(String s) {

                    }
                }, constraints);

                Log.e(LOG_TAG, "createOffer");

                mPeersMap.put(idString, peer);
            }
        });


        socket.on(("getOffer"), new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                JSONObject jsonMessage = (JSONObject) args[0];
                String buf;
                String buf2;
                String buf3;
                try {
                    buf = jsonMessage.getString("from");
                    buf2 = jsonMessage.getString("offer");
                    JSONObject offerMessage = new JSONObject(buf2);
                    buf3 = offerMessage.getString("sdp");

                } catch (JSONException e) {
                    buf = null;
                    buf2 = null;
                    buf3 = null;
                    e.printStackTrace();
                }

                final String idString = buf;
                final String offerString = buf2;
                final String sdpString = buf3;
                Log.e(LOG_TAG, "getoffer");
               // Log.e(LOG_TAG, args[0].toString());
                Log.e(LOG_TAG, "data.from : " + idString);
                Log.e(LOG_TAG, "data.offer : " + offerString);
              //  Log.e(LOG_TAG, "data.offer.sdp : " + sdpString);

                final PeerConnection peer = mPeersMap.get(idString);

                if (peer != null) {
                    SessionDescription answer = new SessionDescription(SessionDescription.Type.fromCanonicalForm("offer"), sdpString);
                    peer.setRemoteDescription(new SdpObserver() {
                        @Override
                        public void onCreateSuccess(SessionDescription sessionDescription) {

                        }

                        @Override
                        public void onSetSuccess() {
                            Log.e(LOG_TAG, "Successfully connected to %s" + idString);

                        }

                        @Override
                        public void onCreateFailure(String s) {

                        }

                        @Override
                        public void onSetFailure(String s) {

                        }
                    }, answer);
                } else {
                    Log.e(LOG_TAG, "getOffer - peer is undefined");

                    MediaConstraints mc = new MediaConstraints();
                    MediaConstraints.KeyValuePair dtls = new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true");
                    mc.mandatory.add(dtls);

                    final PeerConnection newPeer = factory.createPeerConnection(mIceServerList, mc, new PeerConnection.Observer() {
                        @Override
                        public void onIceCandidate(final IceCandidate candidate) {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    Log.e(LOG_TAG, "onIceCandiate getOffer");
                                    JSONObject obj = new JSONObject();
                                    JSONObject obj2 = new JSONObject();
                                    try {
                                        obj2.put("sdpMLineIndex", candidate.sdpMLineIndex);
                                        obj2.put("sdpMid", candidate.sdpMid);
                                        obj2.put("candidate", candidate.sdp);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    try {
                                        obj.put("to", idString);
                                        obj.put("ice", obj2);
                                        //LOG_TAG

                                      //  {"sdpMLineIndex":1,"sdpMid":"video","candidate":"candidate:158869380 1 udp 1685855999 206.225.132.111 54288 typ srflx raddr 198.18.80.138 rport 54288 generation 0
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    socket.emit("sendIce", obj);
                                }
                            });
                        }

                        @Override
                        public void onError() {
                            getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    throw new RuntimeException("PeerConnection error!");
                                }
                            });
                        }

                        @Override
                        public void onSignalingChange(PeerConnection.SignalingState newState)
                        {
                        }

                        @Override
                        public void onIceConnectionChange(PeerConnection.IceConnectionState newState)
                        {
                        }

                        @Override
                        public void onIceGatheringChange(PeerConnection.IceGatheringState newState)
                        {
                        }

                        @Override
                        public void onAddStream(final MediaStream stream)
                        {
                            getActivity().runOnUiThread(new Runnable()
                            {
                                public void run() {
                                    Log.e(LOG_TAG, "onAddStream getOffer");
                                    //abortUnless(stream.audioTracks.size() == 1 && stream.videoTracks.size() == 1, "Weird-looking stream: " + stream);
                                    stream.videoTracks.get(0).addRenderer(new VideoRenderer(new VideoCallbacks(vsv, VideoStreamsView.Endpoint.REMOTE)));
                                }
                            });
                        }

                        @Override
                        public void onRemoveStream(final MediaStream stream) {
                               getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        stream.videoTracks.get(0).dispose();
                                    }
                                });
                        }

                        @Override
                        public void onDataChannel(DataChannel dataChannel) {

                        }

                        @Override
                        public void onRenegotiationNeeded() {

                        }
                    });







                    newPeer.addStream(lMS, new MediaConstraints());




                  //  Log.e(LOG_TAG, sdpString.replace("\r","").replace("\\",""));

                    final SessionDescription answer = new SessionDescription(SessionDescription.Type.OFFER, sdpString);
                    newPeer.setRemoteDescription(new SdpObserver() {
                        @Override
                        public void onCreateSuccess(SessionDescription sessionDescription) {

                        }

                        @Override
                        public void onSetSuccess() {
                            MediaConstraints constraints = new MediaConstraints();
                            constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
                            constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
                            Log.e("onSetSuccess", "answer");
                            newPeer.createAnswer(new SdpObserver() {
                                @Override
                                public void onCreateSuccess(final SessionDescription sessionDescription) {
                                    newPeer.setLocalDescription(new SdpObserver() {
                                        @Override
                                        public void onCreateSuccess(SessionDescription sessionDescription) {

                                        }

                                        @Override
                                        public void onSetSuccess() {
                                            JSONObject obj = new JSONObject();
                                            JSONObject obj2 = new JSONObject();
                                            try {
                                                obj2.put("sdp", sessionDescription.description);
                                                obj2.put("type", "answer");
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            try {
                                                obj.put("to", idString);
                                                obj.put("offer", obj2);

                                               // Log.e(LOG_TAG, sessionDescription.description);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

                                            Log.e(LOG_TAG, obj.toString());
                                           // bj.getString("offerString");


                                            socket.emit("sendOffer", obj);
                                            Log.e(LOG_TAG, "succes send offer");

                                        }

                                        @Override
                                        public void onCreateFailure(String s) {

                                        }

                                        @Override
                                        public void onSetFailure(String s) {

                                        }
                                    }, sessionDescription);

                                }

                                @Override
                                public void onSetSuccess() {

                                }

                                @Override
                                public void onCreateFailure(String s) {

                                }

                                @Override
                                public void onSetFailure(String s) {

                                }
                            }, constraints);

                        }

                        @Override
                        public void onCreateFailure(String s) {

                        }

                        @Override
                        public void onSetFailure(String s) {
                            Log.e(LOG_TAG, "setFailed getOffer"+ s);
                        }
                    }, answer);
                    //newPeer.addStream(lMS, new MediaConstraints());





                    mPeersMap.put(idString, newPeer);
                }
            }
        });

        socket.on("getIce", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                JSONObject jsonMessage = (JSONObject) args[0];
                String id = null;
                String candidate = null;
                String sdpMid = null;
                String sdpMLineIndex = null;
                String sdp = null;
                try {
                    id = jsonMessage.getString("from");
                    candidate = jsonMessage.getString("ice");
                    JSONObject jsonCandidate = new JSONObject(candidate);
                    sdpMid = jsonCandidate.getString("sdpMid");
                    sdpMLineIndex = jsonCandidate.getString("sdpMLineIndex");
                    sdp = jsonCandidate.getString("candidate");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
               // Log.e(LOG_TAG, "getIce");
               // Log.e(LOG_TAG, "data.from : " + id);
               // Log.e(LOG_TAG, "data.ice : " + candidate);
                PeerConnection peer = mPeersMap.get(id);
                if (peer != null) {
                    peer.addIceCandidate(new IceCandidate(sdpMid, Integer.valueOf(sdpMLineIndex), sdp));
                }
            }
        });

        JSONObject obj = new JSONObject();
        try {
            obj.put("name", "simplechat");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        socket.connect();
        Log.e(LOG_TAG, "socket connect");
        socket.emit("joinRoom", obj);
    }

    private class VideoCallbacks implements VideoRenderer.Callbacks {
        private final VideoStreamsView view;
        private final VideoStreamsView.Endpoint stream;

        public VideoCallbacks(VideoStreamsView view, VideoStreamsView.Endpoint stream) {
            this.view = view;
            this.stream = stream;
        }

        @Override
        public void setSize(final int width, final int height) {
            view.queueEvent(new Runnable() {
                public void run() {
                    view.setSize(stream, width, height);
                }
            });
        }

        @Override
        public void renderFrame(I420Frame frame) {
            view.queueFrame(stream, frame);
        }
    }

}
