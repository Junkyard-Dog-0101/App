package com.eloviz.app.webRTC;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.eloviz.app.ADrawerFragment;
import com.eloviz.app.R;
import com.eloviz.app.VideoStreamsView;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WebRTCStreamFragment extends ADrawerFragment {
    private static final String LOG_TAG = "WebRTCStreamFragment";
    private List<PeerConnection.IceServer> mIceServerList = new LinkedList<>();
    private Map<String, PeerConnection> mPeersMap = new HashMap<>();
    private MediaStream lMS;
    private PeerConnectionFactory factory;
    private VideoStreamsView vsv;
    private String roomName = null;
    private PeerConnection mPc;
    private VideoSource mVideoSource = null;
    private WebRTCSocket mSocket = WebRTCSocket.getInstance();

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

    public VideoCapturer getVideoCapturer() {
        String[] cameraFacing={"front","back"};
        int[] cameraIndex={0,1};
        int[] cameraOrientation={0,90,180,270};
        for (String facing : cameraFacing) {
            for (int index : cameraIndex) {
                for (int orientation : cameraOrientation) {
                    String name="Camera " + index + ", Facing "+ facing+ ", Orientation "+ orientation;
                    VideoCapturer capturer = VideoCapturer.create(name, orientation);
                    if (capturer != null) {
                        Log.e("Using camera: ", name);
                        return capturer;
                    }
                }
            }
        }
        throw new RuntimeException("Failed to open capturer");
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
            mSocket.createSocket();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
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

        VideoCapturer capturer = getVideoCapturer();
        mVideoSource = factory.createVideoSource(capturer, new MediaConstraints());
        lMS = factory.createLocalMediaStream("ARDAMS");
        VideoTrack videoTrack = factory.createVideoTrack("ARDAMSv0", mVideoSource);
        videoTrack.addRenderer(new VideoRenderer(new VideoCallbacks(vsv, VideoStreamsView.Endpoint.LOCAL)));
        lMS.addTrack(videoTrack);
        //lMS.addTrack(factory.createAudioTrack("ARDAMSa0", new AudioSource(0)));

        mSocket.on("remoteMessage", new Emitter.Listener() {
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
                Log.e("receive remoteMessage", data.toString());
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
        mSocket.connect();
        mSocket.sendRemoteMessage(UUID.randomUUID().toString(), roomName);
        mSocket.sendRemoteMessage("hello", null, null);
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

    private void hello(JSONObject data) {
        Log.i(LOG_TAG, "hello");
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

        mPc = factory.createPeerConnection(mIceServerList, mc, new PCObserver(idString, getActivity(), vsv));
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
                            obj.put("offer", obj2);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        WebRTCSocket.getInstance().sendRemoteMessage("helloBack", obj, idString);
                        Log.i("remoteMessageSend", "sendOffer");
                    }
                }, sessionDescription);
            }
        }, constraints);
        mPeersMap.put(idString, mPc);
    }

    private void helloBack(JSONObject data) {
        Log.i("helloBack", data.toString());
        String buf;
        String buf3;
        try {
            buf = data.getString("sender");
            buf3 = data.getJSONObject("data").getJSONObject("offer").getString("sdp");
        } catch (JSONException e) {
            buf = null;
            buf3 = null;
            e.printStackTrace();
        }
        final String idString = buf;
        final String sdpString = buf3;
        Log.e("sdp", sdpString);
        MediaConstraints mc = new MediaConstraints();
        MediaConstraints.KeyValuePair dtls = new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true");
        mc.mandatory.add(dtls);
        Log.e("fdssf", "create newPeer");
        final PeerConnection pc = factory.createPeerConnection(mIceServerList, mc, new PCObserver(idString, getActivity(), vsv));
        pc.addStream(lMS, new MediaConstraints());
        Log.e("fdssf", "answer");
        final SessionDescription answer = new SessionDescription(SessionDescription.Type.OFFER, sdpString);
        Log.e("dftrrf", answer.description);
        pc.setRemoteDescription(new SDPObserver() {
            @Override
            public void onSetSuccess() {
                Log.e("fsdfs","test3");
                MediaConstraints constraints = new MediaConstraints();
                constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"));
                constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
                pc.createAnswer(new SDPObserver() {
                    @Override
                    public void onCreateSuccess(final SessionDescription sessionDescription) {
                        Log.e("fsdfs","test2");
                        pc.setLocalDescription(new SDPObserver() {
                            @Override
                            public void onSetSuccess() {

                                JSONObject obj = new JSONObject();
                                JSONObject obj2 = new JSONObject();
                                try {
                                    obj2.put("sdp", sessionDescription.description);
                                    obj2.put("type", "answer");
                                    obj.put("offer", obj2);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                WebRTCSocket.getInstance().sendRemoteMessage("helloFinish", obj, idString);
                            }
                        }, sessionDescription);
                    }
                }, constraints);
            }
        }, answer);
        mPeersMap.put(idString, pc);
    }

    private void helloFinish(JSONObject data) {
        Log.i(LOG_TAG, "helloFinish");
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
            buf3 = null;
            e.printStackTrace();
        }
        final String idString = buf;
        final String sdpString = buf3;
        final PeerConnection peer = mPeersMap.get(idString);
        if (peer != null) {
            SessionDescription answer = new SessionDescription(SessionDescription.Type.fromCanonicalForm("answer"), sdpString);
            peer.setRemoteDescription(new SDPObserver() {
                @Override
                public void onSetSuccess() {
                    Log.i(LOG_TAG, "Successfully connected to " + idString);
                }
            }, answer);
        }
    }

    private void iceCandidate(JSONObject data) {
        Log.e(LOG_TAG, "iceCandidate");
        String id = null;
        String candidate;
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
  /*  sdp: "v=0
            ↵o=- 7568057190010547481 2 IN IP4 127.0.0.1
            ↵s=-
            ↵t=0 0
            ↵a=group:BUNDLE audio
    ↵a=msid-semantic: WMS aWxsPOzRU0bIfzwhpcPfTZWo8IQvVDk8yVBx
    ↵m=audio 9 UDP/TLS/RTP/SAVPF 111 103 104 9 0 8 106 105 13 126
            ↵c=IN IP4 0.0.0.0
            ↵a=rtcp:9 IN IP4 0.0.0.0
            ↵a=ice-ufrag:UkLFUs1RqcjHXyCT
    ↵a=ice-pwd:83eo/AqBVdllWSCREPogk/yL
    ↵a=fingerprint:sha-256 6D:68:BC:04:44:A9:47:99:70:2C:6B:AC:D1:75:02:27:5E:B9:DD:96:6B:E8:68:7D:79:CF:CE:41:52:47:76:AD
    ↵a=setup:actpass
    ↵a=mid:audio
    ↵a=extmap:1 urn:ietf:params:rtp-hdrext:ssrc-audio-level
    ↵a=extmap:3 http://www.webrtc.org/experiments/rtp-hdrext/abs-send-time
            ↵a=sendrecv
    ↵a=rtcp-mux
    ↵a=rtpmap:111 opus/48000/2
            ↵a=fmtp:111 minptime=10; useinbandfec=1
            ↵a=rtpmap:103 ISAC/16000
            ↵a=rtpmap:104 ISAC/32000
            ↵a=rtpmap:9 G722/8000
            ↵a=rtpmap:0 PCMU/8000
            ↵a=rtpmap:8 PCMA/8000
            ↵a=rtpmap:106 CN/32000
            ↵a=rtpmap:105 CN/16000
            ↵a=rtpmap:13 CN/8000
            ↵a=rtpmap:126 telephone-event/8000
            ↵a=maxptime:60
            ↵a=ssrc:1233041855 cname:DLIZ4mEGPzMzIHAS
    ↵a=ssrc:1233041855 msid:aWxsPOzRU0bIfzwhpcPfTZWo8IQvVDk8yVBx fee8f1f0-e1ca-4dc5-8d8c-9ca3646f58ff
    ↵a=ssrc:1233041855 mslabel:aWxsPOzRU0bIfzwhpcPfTZWo8IQvVDk8yVBx
    ↵a=ssrc:1233041855 label:fee8f1f0-e1ca-4dc5-8d8c-9ca3646f58ff
    ↵"
*/
    private void renegotiate(JSONObject data) {
        Log.i(LOG_TAG, "renegociate");
        String buf;
        String buf3;
        try {
            buf = data.getString("sender");
            buf3 = data.getJSONObject("data").getJSONObject("offer").getString("sdp");
        } catch (JSONException e) {
            buf = null;
            buf3 = null;
            e.printStackTrace();
        }
        final String idString = buf;
        final String sdpString = buf3;

        MediaConstraints mc = new MediaConstraints();
        MediaConstraints.KeyValuePair dtls = new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true");
        mc.mandatory.add(dtls);

        final PeerConnection newPeer = factory.createPeerConnection(mIceServerList, mc, new PCObserver(idString, getActivity(), vsv));

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
                                    obj.put("offer", obj2);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                WebRTCSocket.getInstance().sendRemoteMessage("renegotiateBack", obj, idString);
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
        Log.i(LOG_TAG, "renegociateBack");
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
            buf3 = null;
            e.printStackTrace();
        }
        final String idString = buf;
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

/*
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
*/

       /* socket.on(("leavingPeer"), new Emitter.Listener() {
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
        });*/