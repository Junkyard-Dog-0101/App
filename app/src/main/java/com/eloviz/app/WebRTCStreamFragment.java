package com.eloviz.app;


import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRenderer.I420Frame;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class WebRTCStreamFragment extends ADrawerFragment {
    private static final String LOG_TAG = "WebRTCStreamFragment";
    static Socket socket = null;
    private final PCObserver pcObserver = new PCObserver();
    List<PeerConnection.IceServer> mIceServerList = new ArrayList<PeerConnection.IceServer>();
    private VideoStreamsView vsv;
    private String idString = null;

    private static void abortUnless(boolean condition, String msg) {
        if (!condition) {
            throw new RuntimeException(msg);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Point displaySize = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(displaySize);
        vsv = new VideoStreamsView(getActivity(), displaySize);

        return vsv;//inflater.inflate(R.layout.fragment_webrtc, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e(LOG_TAG, "onAcvityCreated");
        AppRestClient.get("http://webrtc.dennajort.fr/", "iceServers.json", null, new JsonHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JSONArray responseArray = new JSONArray();
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

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });
        try {
            socket = IO.socket("http://webrtc.dennajort.fr/default/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

       /* Button button = (Button) getActivity().findViewById(R.id.btnSend);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) getActivity().findViewById(R.id.textInput);
                EditText editTextPseudo = (EditText) getActivity().findViewById(R.id.textInputPseudo);

                JSONObject obj = new JSONObject();

                try {
                    obj.put("username", editTextPseudo.getText());
                    obj.put("body", editText.getText());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socket.emit("sendMessage", obj);
                editText.setText("");
            }
        });*/


        socket.on("newPeer", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                JSONObject id = (JSONObject) args[0];

                try {
                    idString = id.getString("id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.e(LOG_TAG, "newPeer");
                Log.e(LOG_TAG, "data.id : " + idString);

                Log.e(LOG_TAG, mIceServerList.get(0).uri);
                PeerConnectionFactory.initializeAndroidGlobals(getActivity());
                PeerConnectionFactory factory = new PeerConnectionFactory();
                //PeerConnection.IceServer toto = new PeerConnection.IceServer()
                //final String finalIdString = idString;
                final PeerConnection peer = factory.createPeerConnection(mIceServerList, new MediaConstraints(), pcObserver);//(iceServers, new MediaConstraints(), pcObserver);

                Log.e(LOG_TAG, "createPeerConnection");
                // logAndToast("Creating local video source...");
                VideoCapturer capturer =
                        VideoCapturer.create("Camera 1, Facing front, Orientation 270", 1);
                if (capturer == null) {
                    capturer = VideoCapturer.create("Camera 0, Facing front, Orientation 270", 0);
                }
                //abortUnless(capturer != null, "Failed to open capturer");
                VideoSource videoSource = factory.createVideoSource(capturer, new MediaConstraints());
                MediaStream lMS = factory.createLocalMediaStream("ARDAMS");
                VideoTrack videoTrack = factory.createVideoTrack("ARDAMSv0", videoSource);
                videoTrack.addRenderer(new VideoRenderer(new VideoCallbacks(vsv, VideoStreamsView.Endpoint.LOCAL)));
                lMS.addTrack(videoTrack);
                lMS.addTrack(factory.createAudioTrack("ARDAMSa0", new AudioSource(1)));
                peer.addStream(lMS, new MediaConstraints());
                Log.e(LOG_TAG, "addStream");
                //peer.addStream();

                MediaConstraints constraints = new MediaConstraints();
                constraints.mandatory.add(new MediaConstraints.KeyValuePair(
                        "OfferToReceiveAudio", "true"));
                constraints.mandatory.add(new MediaConstraints.KeyValuePair(
                        "OfferToReceiveVideo", "true"));


                peer.createOffer(new SdpObserver() {

                    @Override
                    public void onCreateSuccess(SessionDescription sessionDescription) {
                        Log.e("in create offer", "createlocalsuccess");
                        peer.setLocalDescription(new SdpObserver() {
                            @Override
                            public void onCreateSuccess(SessionDescription sessionDescription) {

                            }

                            @Override
                            public void onSetSuccess() {
                                Log.e("in create offer", "setlocalsuccess");
                                // socket.emit("answerNewPeer", {id: data.id, offer: offer});

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
                //peer.ice
            }
        });


        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                //  socket.emit("foo", "hi");
                //socket.disconnect();

            }

        });


        socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
            }

        });

       /* socket.on("newMessage", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                final JSONObject obj = (JSONObject) args[0];

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView textView = (TextView) getActivity().findViewById(R.id.textOutput);
                        try {
                            SpannableString spanString = new SpannableString(obj.getString("username"));
                            spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
                            textView.append(spanString);
                            textView.append(" : " + obj.getString("body") + "\n");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    //textOutput
                    //Log.e("fsfs", args[0].toString());
                });
            }
        });*/


        // Receiving an object

        //socket.connect();
        //  PeerConnection();
    }


    private class PCObserver implements PeerConnection.Observer {
        @Override
        public void onIceCandidate(final IceCandidate candidate) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    JSONObject obj = new JSONObject();

                    try {
                        obj.put("id", idString);
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
        public void onSignalingChange(
                PeerConnection.SignalingState newState) {
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
                    abortUnless(stream.audioTracks.size() == 1 && stream.videoTracks.size() == 1, "Weird-looking stream: " + stream);
                    stream.videoTracks.get(0).addRenderer(new VideoRenderer(
                            new VideoCallbacks(vsv, VideoStreamsView.Endpoint.REMOTE)));
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

    private class VideoCallbacks implements VideoRenderer.Callbacks {
        private final VideoStreamsView view;
        private final VideoStreamsView.Endpoint stream;

        public VideoCallbacks(
                VideoStreamsView view, VideoStreamsView.Endpoint stream) {
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
