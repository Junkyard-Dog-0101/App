package com.eloviz.app;

import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRenderer.I420Frame;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WebRTCStreamFragment extends ADrawerFragment {
    private static final String LOG_TAG = "WebRTCStreamFragment";
    private List<PeerConnection.IceServer> mIceServerList = new LinkedList<>();
    private Map<String, PeerConnection> mPeersMap = new HashMap<>();
    private Socket socket;
    private MediaStream lMS;
    private PeerConnectionFactory factory;
    private VideoStreamsView vsv;
    private String roomName = null;
    private PeerConnection mPc;
    private VideoSource mVideoSource = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        roomName = bundle.getString("room", "simplechat");
        Log.e(LOG_TAG, roomName);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_webrtc, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Point displaySize = new Point();
        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.vsvLayout);
        getActivity().getWindowManager().getDefaultDisplay().getSize(displaySize);
        displaySize.y = 600;

        vsv = new VideoStreamsView(getActivity(), displaySize);
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
                Log.i(LOG_TAG, "startStream");
                socket.emit("startStream");

            }
        });*/


        try {
            String finalRoom = "http://eloviz.com/meeting";// + roomName;
            Log.e(LOG_TAG, finalRoom);
            socket = IO.socket(finalRoom);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        mIceServerList.add(new PeerConnection.IceServer("stun:eloviz.com"));
        mIceServerList.add(new PeerConnection.IceServer("turn:eloviz.com", "eloviz", "eloviz"));
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

        mIceServerList.add(new PeerConnection.IceServer("turn:numb.viagenie.ca", "webrtc@live.com", "muazkh"));
        mIceServerList.add(new PeerConnection.IceServer("turn:192.158.29.39:3478?transport=udp", "28224511:1379330808", "JZEOEt2V3Qb0y27GRntt2u2PAYA="));
        mIceServerList.add(new PeerConnection.IceServer("turn:192.158.29.39:3478?transport=tcp", "28224511:1379330808", "JZEOEt2V3Qb0y27GRntt2u2PAYA="));

        PeerConnectionFactory.initializeAndroidGlobals(getActivity());
        factory = new PeerConnectionFactory();

        Log.i(LOG_TAG, "Creating local video source...");

        VideoCapturer capturer = VideoCapturer.create("Camera 1, Facing front, Orientation 270", 0);
        if (capturer == null) {
            capturer = VideoCapturer.create("Camera 0, Facing front, Orientation 270", 0);
        }
        mVideoSource = factory.createVideoSource(capturer, new MediaConstraints());
        lMS = factory.createLocalMediaStream("ARDAMS");
        VideoTrack videoTrack = factory.createVideoTrack("ARDAMSv0", mVideoSource);
        videoTrack.addRenderer(new VideoRenderer(new VideoCallbacks(vsv, VideoStreamsView.Endpoint.LOCAL)));
        lMS.addTrack(videoTrack);
        //lMS.addTrack(factory.createAudioTrack("ARDAMSa0", new AudioSource(0)));

        socket.on("remoteMessage", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                JSONObject data = (JSONObject) args[0];
                String buf;
                try {
                    buf = data.getString("event");
                } catch (JSONException e) {
                    buf = null;
                    e.printStackTrace();
                }
                Log.e("toror", data.toString());
                if (buf != null)
                    switch (buf) {
                        case "hello":
                            hello(data);
                            break;
                        case "helloBack":
                            helloBack(data);
                            break;
                        case "helloFinish":
                            helloFinish(data);
                            break;
                        case "iceCandidate":
                            iceCandidate(data);
                            break;
                        case "renegotiate":
                            renegotiate(data);
                            break;
                        case "renegotiateBack":
                            renegotiateBack(data);
                            break;
                    }
            }
        });

        socket.on("newMessage", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                JSONObject jsonMessage = (JSONObject) args[0];
                String buf = "username";
                String buf2 = null;
                Date d = new Date();
                final CharSequence s = DateFormat.format("kk:mm", d.getTime());
                try {
                    buf = jsonMessage.getString("from");
                    buf2 = jsonMessage.getString("message");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                final String from = buf;
                final String message = buf2;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView textView = (TextView) getActivity().findViewById(R.id.textOutput);
                        SpannableString spanString = new SpannableString(from);
                        spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
                        textView.append(s + " ");
                        textView.append(spanString);
                        textView.append(" : " + message + "\n");
                    }
                });
            }
        });


        socket.on(("leavingPeer"), new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                Log.i(LOG_TAG, "leavingPeer");
                JSONObject jsonMessage = (JSONObject) args[0];
                String id;
                try {
                    id = jsonMessage.getString("id");
                } catch (JSONException e) {
                    id = null;
                    e.printStackTrace();
                }
                final PeerConnection peer = mPeersMap.get(id);
                if (peer != null) {
                    peer.close();
                    mPeersMap.remove(id);
                    Log.i(LOG_TAG, "peer leave " + id);
                }
            }
        });

        JSONObject obj = new JSONObject();
        try {
            UUID idOne = UUID.randomUUID();
            obj.put("roomName", roomName);
            obj.put("sender", idOne.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.connect();
        Log.i(LOG_TAG, "Socket connected");
        Log.e("joinRoom", obj.toString());
        socket.emit("joinRoom", obj);
        try {
            JSONObject helloObj = new JSONObject();
            helloObj.put("event", "hello");
            Log.e("remoteMessage1", helloObj.toString());
            socket.emit("remoteMessage", helloObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPc != null)
            mPc.dispose();
        if (lMS != null)
            lMS.dispose();
        if (mVideoSource != null)
            mVideoSource.dispose();
        if (factory != null)
            factory.dispose();
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

    private class PCObserver implements PeerConnection.Observer {
        private String mId;

        PCObserver(String id) {
            mId = id;
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
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    Log.e(LOG_TAG, "onIceCandiate");
                    JSONObject obj = new JSONObject();
                    JSONObject obj2 = new JSONObject();
                    try {
                        obj2.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
                        obj2.put("sdpMid", iceCandidate.sdpMid);
                        obj2.put("candidate", iceCandidate.sdp);
                       // obj.put("to", mId);
                        obj.put("ice", obj2);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    sendRemoteMessage("iceCandidate", obj, mId);
                }
            });
        }

        @Override
        public void onError() {

        }

        @Override
        public void onAddStream(final MediaStream mediaStream) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    Log.i(LOG_TAG, "onAddStream");
                    mediaStream.videoTracks.get(0).addRenderer(new VideoRenderer(new VideoCallbacks(vsv, VideoStreamsView.Endpoint.REMOTE)));
                }
            });
        }

        @Override
        public void onRemoveStream(final MediaStream mediaStream) {
            getActivity().runOnUiThread(new Runnable() {
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

        }
    }

    private class SDPObserver implements SdpObserver {
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

    private void hello(JSONObject data) {
        Log.i(LOG_TAG, "newPeer");
        //data = (JSONObject) args[0];
        String buf;
        try {
            buf = data.getString("sender");
        } catch (JSONException e) {
            buf = null;
            e.printStackTrace();
        }
        final String idString = buf;

        MediaConstraints mc = new MediaConstraints();
        MediaConstraints.KeyValuePair dtls = new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true");
        mc.mandatory.add(dtls);
        //mc.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"));
        //mc.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));

        mPc = factory.createPeerConnection(mIceServerList, mc, new PCObserver(idString));
        Log.e(LOG_TAG, "newPeer " + idString);
        mPc.addStream(lMS, new MediaConstraints());
        Log.i(LOG_TAG, "addStream");

        MediaConstraints constraints = new MediaConstraints();
        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"));
        constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));

        mPc.createOffer(new SDPObserver() {
            @Override
            public void onCreateSuccess(final SessionDescription sessionDescription) {
                mPc.setLocalDescription(new SDPObserver() {
                    @Override
                    public void onSetSuccess() {
                        Log.i(LOG_TAG, "Successfully set to " + idString);
                        JSONObject obj = new JSONObject();
                        JSONObject obj2 = new JSONObject();
                        try {
                            obj2.put("sdp", sessionDescription.description);
                            obj2.put("type", "offer");
                           // obj.put("to", idString);
                            obj.put("offer", obj2);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        sendRemoteMessage("helloBack", obj, idString);
                        Log.i(LOG_TAG, "sendOffer");
                    }
                }, sessionDescription);
            }
        }, constraints);
        mPeersMap.put(idString, mPc);
    }

    private void helloBack(JSONObject data) {
        Log.i(LOG_TAG, "getOffer");
        String buf;
        String buf2;
        String buf3;
        try {
            buf = data.getString("sender");
            buf3 = data.getJSONObject("data").getJSONObject("offer").getString("sdp");
//            buf2 = data.getString("offer");
//            JSONObject offerMessage = new JSONObject(buf2);
//            buf3 = offerMessage.getString("sdp");
  //          buf3 = offerMessage.getString("offer");
        } catch (JSONException e) {
            buf = null;
            buf2 = null;
            buf3 = null;
            e.printStackTrace();
        }
        final String idString = buf;
       // final String offerString = buf2;
        final String sdpString = buf3;
        //              final PeerConnection peer = mPeersMap.get(idString);
                /*if (peer != null) {
                    SessionDescription answer = new SessionDescription(SessionDescription.Type.fromCanonicalForm("answer"), sdpString);    changer par answer pour rgl ler problem de newpee
                    peer.setRemoteDescription(new SDPObserver() {
                        @Override
                        public void onSetSuccess() {
                            Log.i(LOG_TAG, "Successfully connected to " + idString);
                        }
                    }, answer);
                } else {*/
        Log.i(LOG_TAG, "Peer is null");

        MediaConstraints mc = new MediaConstraints();
        MediaConstraints.KeyValuePair dtls = new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true");
        mc.mandatory.add(dtls);

        final PeerConnection newPeer = factory.createPeerConnection(mIceServerList, mc, new PCObserver(idString));

        newPeer.addStream(lMS, new MediaConstraints());

        final SessionDescription answer = new SessionDescription(SessionDescription.Type.OFFER, sdpString);
        newPeer.setRemoteDescription(new SDPObserver() {
            @Override
            public void onSetSuccess() {
                MediaConstraints constraints = new MediaConstraints();
                constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"));
                constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
                newPeer.createAnswer(new SDPObserver() {
                    @Override
                    public void onCreateSuccess(final SessionDescription sessionDescription) {
                        newPeer.setLocalDescription(new SDPObserver() {
                            @Override
                            public void onSetSuccess() {
                                JSONObject obj = new JSONObject();
                                JSONObject obj2 = new JSONObject();
                                try {
                                    obj2.put("sdp", sessionDescription.description);
                                    obj2.put("type", "answer");
//                                    obj.put("to", idString);
                                    obj.put("offer", obj2);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                sendRemoteMessage("helloFinish", obj, idString);
                                Log.i(LOG_TAG, "sendOffer");
                            }
                        }, sessionDescription);
                    }
                }, constraints);
            }
        }, answer);
        mPeersMap.put(idString, newPeer);
    }

    private void helloFinish(JSONObject data) {
        Log.i(LOG_TAG, "getOffer");
        String buf;
        String buf2;
        String buf3;
        try {
            buf = data.getString("sender");
            buf2 = data.getString("data");
            JSONObject offerMessage = new JSONObject(buf2);
            buf3 = offerMessage.getString("offer");
        } catch (JSONException e) {
            buf = null;
            buf2 = null;
            buf3 = null;
            e.printStackTrace();
        }
        final String idString = buf;
        final String offerString = buf2;
        final String sdpString = buf3;
        final PeerConnection peer = mPeersMap.get(idString);
        if (peer != null) {
            SessionDescription answer = new SessionDescription(SessionDescription.Type.fromCanonicalForm("answer"), sdpString); /*   changer par answer pour rgl ler problem de newpeer*/
            peer.setRemoteDescription(new SDPObserver() {
                @Override
                public void onSetSuccess() {
                    Log.i(LOG_TAG, "Successfully connected to " + idString);
                }
            }, answer);
        }
    }

    private void iceCandidate(JSONObject data) {
        Log.i(LOG_TAG, "getIce");
        String id = null;
        String candidate = null;
        String sdpMid = null;
        String sdpMLineIndex = null;
        String sdp = null;
        try {
            id = data.getString("sender");
            JSONObject candidateData = data.getJSONObject("data");
            candidate = candidateData.getString("ice");
            JSONObject jsonCandidate = new JSONObject(candidate);
            sdpMid = jsonCandidate.getString("sdpMid");
            sdpMLineIndex = jsonCandidate.getString("sdpMLineIndex");
            sdp = jsonCandidate.getString("candidate");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        PeerConnection peer = mPeersMap.get(id);
        if (peer != null) {
            peer.addIceCandidate(new IceCandidate(sdpMid, Integer.valueOf(sdpMLineIndex), sdp));
        }
    }

    private void sendRemoteMessage(String event, JSONObject data, String receiver) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("event", event);
            obj.put("data", data);
            obj.put("receiver", receiver);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        socket.emit("remoteMessage", obj);
        Log.e("remoteMessage", obj.toString());
    }

    private void renegotiate(JSONObject data) {
        Log.i(LOG_TAG, "renegociate");
        String buf;
        String buf2;
        String buf3;
        try {
            buf = data.getString("sender");
            buf3 = data.getJSONObject("data").getJSONObject("offer").getString("sdp");
//            buf2 = data.getString("offer");
//            JSONObject offerMessage = new JSONObject(buf2);
//            buf3 = offerMessage.getString("sdp");
            //          buf3 = offerMessage.getString("offer");
        } catch (JSONException e) {
            buf = null;
            buf2 = null;
            buf3 = null;
            e.printStackTrace();
        }
        final String idString = buf;
        // final String offerString = buf2;
        final String sdpString = buf3;
        //              final PeerConnection peer = mPeersMap.get(idString);
                /*if (peer != null) {
                    SessionDescription answer = new SessionDescription(SessionDescription.Type.fromCanonicalForm("answer"), sdpString);    changer par answer pour rgl ler problem de newpee
                    peer.setRemoteDescription(new SDPObserver() {
                        @Override
                        public void onSetSuccess() {
                            Log.i(LOG_TAG, "Successfully connected to " + idString);
                        }
                    }, answer);
                } else {*/
        Log.i(LOG_TAG, "Peer is null");

        MediaConstraints mc = new MediaConstraints();
        MediaConstraints.KeyValuePair dtls = new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true");
        mc.mandatory.add(dtls);

        final PeerConnection newPeer = factory.createPeerConnection(mIceServerList, mc, new PCObserver(idString));

        newPeer.addStream(lMS, new MediaConstraints());

        final SessionDescription answer = new SessionDescription(SessionDescription.Type.OFFER, sdpString);
        newPeer.setRemoteDescription(new SDPObserver() {
            @Override
            public void onSetSuccess() {
                MediaConstraints constraints = new MediaConstraints();
                constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"));
                constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
                newPeer.createAnswer(new SDPObserver() {
                    @Override
                    public void onCreateSuccess(final SessionDescription sessionDescription) {
                        newPeer.setLocalDescription(new SDPObserver() {
                            @Override
                            public void onSetSuccess() {
                                JSONObject obj = new JSONObject();
                                JSONObject obj2 = new JSONObject();
                                try {
                                    obj2.put("sdp", sessionDescription.description);
                                    obj2.put("type", "answer");
//                                    obj.put("to", idString);
                                    obj.put("offer", obj2);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                sendRemoteMessage("helloFinish", obj, idString);
                                Log.i(LOG_TAG, "sendOffer");
                            }
                        }, sessionDescription);
                    }
                }, constraints);
            }
        }, answer);
        mPeersMap.put(idString, newPeer);
    }

    private void renegotiateBack(JSONObject data) {
        Log.i(LOG_TAG, "getOffer");
        String buf;
        String buf2;
        String buf3;
        try {
            buf = data.getString("sender");
            buf2 = data.getString("data");
            JSONObject offerMessage = new JSONObject(buf2);
            buf3 = offerMessage.getString("offer");
        } catch (JSONException e) {
            buf = null;
            buf2 = null;
            buf3 = null;
            e.printStackTrace();
        }
        final String idString = buf;
        final String offerString = buf2;
        final String sdpString = buf3;
        final PeerConnection peer = mPeersMap.get(idString);
        if (peer != null) {
            SessionDescription answer = new SessionDescription(SessionDescription.Type.fromCanonicalForm("answer"), sdpString); /*   changer par answer pour rgl ler problem de newpeer*/
            peer.setRemoteDescription(new SDPObserver() {
                @Override
                public void onSetSuccess() {
                    Log.i(LOG_TAG, "Successfully connected to " + idString);
                }
            }, answer);
        }
    }
}