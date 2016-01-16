package com.eloviz.app.webRTC;

import android.app.Activity;
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

import com.eloviz.app.ADrawerFragment;
import com.eloviz.app.AppRestClient;
import com.eloviz.app.R;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import cz.msebera.android.httpclient.Header;
import io.socket.emitter.Emitter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;
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


    private static final int STAT_CALLBACK_PERIOD = 1000;
    // Local preview screen position before call is connected.
    private static final int LOCAL_X_CONNECTING = 0;
    private static final int LOCAL_Y_CONNECTING = 0;
    private static final int LOCAL_WIDTH_CONNECTING = 100;
    private static final int LOCAL_HEIGHT_CONNECTING = 100;
    // Local preview screen position after call is connected.
    private static final int LOCAL_X_CONNECTED = 72;
    private static final int LOCAL_Y_CONNECTED = 72;
    private static final int LOCAL_WIDTH_CONNECTED = 25;
    private static final int LOCAL_HEIGHT_CONNECTED = 25;
    // Remote video screen position
    private static final int REMOTE_X = 0;
    private static final int REMOTE_Y = 0;
    private static final int REMOTE_WIDTH = 100;
    private static final int REMOTE_HEIGHT = 100;
    private RendererCommon.ScalingType scalingType;
    private VideoTrack localVideoTrack;
    private VideoTrack remoteVideoTrack;

    private static final String LOG_TAG = "WebRTCStreamFragment";
    private List<PeerConnection.IceServer> mIceServerList = new LinkedList<>();
    private Map<String, PeerConnection> mPeersMap = new HashMap<>();
    private MediaStream lMS;
    private PeerConnectionFactory factory;
    private SurfaceViewRenderer remoteRender;
    private SurfaceViewRenderer localRender;
    private PercentFrameLayout localRenderLayout;
    private PercentFrameLayout remoteRenderLayout;
    private String roomName = null;
    private PeerConnection mPc;
    private EglBase rootEglBase;
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
        int numberOfCameras;
        numberOfCameras = CameraEnumerationAndroid.getDeviceCount();

        String cameraDeviceName = CameraEnumerationAndroid.getDeviceName(0);
        String frontCameraDeviceName = CameraEnumerationAndroid.getNameOfFrontFacingDevice();
        if (numberOfCameras > 1 && frontCameraDeviceName != null) {
            cameraDeviceName = frontCameraDeviceName;
        }
        //  Log.d(TAG, "Opening camera: " + cameraDeviceName);
        VideoCapturer capturer;
        capturer = VideoCapturerAndroid.create(cameraDeviceName, null);
        if (capturer == null) {
            //    reportError("Failed to open camera");
            return null;
        }
        return capturer;
       /* String[] cameraFacing={"front","back"};
        int[] cameraIndex={0,1};
        int[] cameraOrientation={0,90,180,270};
        for (String facing : cameraFacing) {
            for (int index : cameraIndex) {
                for (int orientation : cameraOrientation) {
                    String name="Camera " + index + ", Facing "+ facing+ ", Orientation "+ orientation;
                    VideoCapturer capturer = VideoCapturer.create(name);
                    if (capturer != null) {
                        Log.e("Using camera: ", name);
                        return capturer;
                    }
                }
            }
        }
        throw new RuntimeException("Failed to open capturer");*/
    }

    private void loadSocket() {
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

        PeerConnectionFactory.initializeAndroidGlobals(getActivity(), true, true, true);
        factory = new PeerConnectionFactory();

        Log.i(LOG_TAG, "Creating local video source...");

        VideoCapturer capturer = getVideoCapturer();
        mVideoSource = factory.createVideoSource(capturer, new MediaConstraints());
        lMS = factory.createLocalMediaStream("ARDAMS");
        VideoTrack videoTrack = factory.createVideoTrack("ARDAMSv0", mVideoSource);
        videoTrack.addRenderer(new VideoRenderer(localRender));
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
                        case "textMessage":
                            onMessage(data);
                            break;
                    }
            }
        });
        mSocket.connect();
        mSocket.sendRemoteMessage(UUID.randomUUID().toString(), roomName);
        mSocket.sendRemoteMessage("hello", null, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        scalingType = RendererCommon.ScalingType.SCALE_ASPECT_FILL;

        Point displaySize = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(displaySize);
        displaySize.y = 600;
        localRender = (SurfaceViewRenderer) getActivity().findViewById(R.id.local_video_view);
        remoteRender = (SurfaceViewRenderer) getActivity().findViewById(R.id.remote_video_view);
        localRenderLayout = (PercentFrameLayout) getActivity().findViewById(R.id.local_video_layout);
        remoteRenderLayout = (PercentFrameLayout) getActivity().findViewById(R.id.remote_video_layout);
        rootEglBase = new EglBase();
        localRender.init(rootEglBase.getContext(), null);
        remoteRender.init(rootEglBase.getContext(), null);
        localRender.setZOrderMediaOverlay(true);

        remoteRenderLayout.setPosition(REMOTE_X, REMOTE_Y, REMOTE_WIDTH, REMOTE_HEIGHT);
        remoteRender.setScalingType(scalingType);
        remoteRender.setMirror(false);
        localVideoTrack = null;
        remoteVideoTrack = null;
        localRenderLayout.setPosition(LOCAL_X_CONNECTING, LOCAL_Y_CONNECTING, LOCAL_WIDTH_CONNECTING, LOCAL_HEIGHT_CONNECTING);
        localRender.setScalingType(scalingType);
        localRender.setMirror(true);

        localRender.requestLayout();
        remoteRender.requestLayout();
        AppRestClient.get("/iceServers.json", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                String url;
                String username;
                String credential;
                for (int i = 0; i < response.length(); ++i) {
                    url = null;
                    username = null;
                    credential = null;
                    JSONObject server = null;
                    try {
                        server = response.getJSONObject(i);
                        url = server.getString("url");
                        username = server.getString("username");
                        credential = server.getString("credential");
                        mIceServerList.add(new PeerConnection.IceServer(url, username, credential));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (url != null) {
                            mIceServerList.add(new PeerConnection.IceServer(url));
                        }
                    }
                }
                loadSocket();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.e(LOG_TAG, "mPc");
        if (mPc != null) {
            mPc.dispose();
            mPc = null;
            // mPc.dispose();
        }
        Log.e(LOG_TAG, "mVideoSource");
        if (mVideoSource != null) {
            mVideoSource.stop();
            // mVideoSource.dispose();
            mVideoSource = null;
            // mVideoSource.dispose();
        }
        Log.e(LOG_TAG, "factory");
        if (factory != null) {
            //factory.dispose();
            factory = null;
        }
       /* if (factory != null)
            factory.dispose();
        if (mVideoSource != null) {
            mVideoSource.stop();
         // mVideoSource.dispose();
        }*/
        Log.e(LOG_TAG, "lMS");

       /* if (lMS != null) {
            lMS.dispose();
        }*/
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

        mPc = factory.createPeerConnection(mIceServerList, mc, new PCObserver(idString, getActivity()));
        Log.e(LOG_TAG, "newPeer " + idString);
        mPc.addStream(lMS);
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
        final PeerConnection pc = factory.createPeerConnection(mIceServerList, mc, new PCObserver(idString, getActivity()));
        pc.addStream(lMS);
        Log.e("fdssf", "answer");
        final SessionDescription answer = new SessionDescription(SessionDescription.Type.OFFER, sdpString);
        Log.e("dftrrf", answer.description);
        pc.setRemoteDescription(new SDPObserver() {
            @Override
            public void onSetSuccess() {
                Log.e("fsdfs", "test3");
                MediaConstraints constraints = new MediaConstraints();
                constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"));
                constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
                pc.createAnswer(new SDPObserver() {
                    @Override
                    public void onCreateSuccess(final SessionDescription sessionDescription) {
                        Log.e("fsdfs", "test2");
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
        final PeerConnection newPeer = factory.createPeerConnection(mIceServerList, mc, new PCObserver(idString, getActivity()));

        newPeer.addStream(lMS);

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


    public void onMessage(JSONObject data) {
        Log.e("from", data.toString());
        JSONObject buf = null;
        String buf2 = null;
        Date d = new Date();
        final CharSequence s = DateFormat.format("kk:mm", d.getTime());
        try {
            //JSONObject buf = data.getString("data");
            buf = data.getJSONObject("data");
            buf2 = buf.getString("message");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String message = buf2;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView textView = (TextView) getActivity().findViewById(R.id.textOutput);
                textView.append(message + "\n");
            }
        });
    }


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
<<<<<<< HEAD:app/src/main/java/com/eloviz/app/webRTC/WebRTCStreamFragment.java
        });*/

    public class PCObserver implements PeerConnection.Observer {

        private String mId;
        private Activity mActivity;

        PCObserver(String id, Activity activity) {
            mId = id;
            mActivity = activity;
        }

        @Override
        public void onSignalingChange(PeerConnection.SignalingState newState) {
            Log.d(LOG_TAG, "SignalingState: " + newState);
        }

        @Override
        public void onIceConnectionChange(final PeerConnection.IceConnectionState newState) {

       /* executor.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(LOG_TAG, "IceConnectionState: " + newState);
                if (newState == PeerConnection.IceConnectionState.CONNECTED) {
                    events.onIceConnected();
                } else if (newState == PeerConnection.IceConnectionState.DISCONNECTED) {
                    events.onIceDisconnected();
                } else if (newState == PeerConnection.IceConnectionState.FAILED) {
                    reportError("ICE connection failed.");
                }
            }
        });*/
        }

        @Override
        public void onIceConnectionReceivingChange(boolean receiving) {
            Log.d(LOG_TAG, "IceConnectionReceiving changed to " + receiving);
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState newState) {
            Log.d(LOG_TAG, "IceGatheringState: " + newState);
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
        public void onAddStream(final MediaStream stream) {
            mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    // if (peerConnection == null || isError) {
                    //    return;
                    // }
                    if (stream.audioTracks.size() > 1 || stream.videoTracks.size() > 1) {
                        //   reportError("Weird-looking stream: " + stream);
                        return;
                    }
                    if (stream.videoTracks.size() == 1) {
                        remoteVideoTrack = stream.videoTracks.get(0);
                        remoteVideoTrack.setEnabled(true);
                        remoteVideoTrack.addRenderer(new VideoRenderer(remoteRender));
                    }
                }
//mediaStream.videoTracks.get(0).addRenderer(new VideoRenderer(new VideoCallbacks(mVideoStreamsView, VideoStreamsView.Endpoint.REMOTE)));
            });
        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {
            mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    remoteVideoTrack = null;
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
}
