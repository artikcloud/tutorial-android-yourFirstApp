package cloud.artik.example.hellocloud;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cloud.artik.api.MessagesApi;
import cloud.artik.api.UsersApi;
import cloud.artik.client.ApiCallback;
import cloud.artik.client.ApiClient;
import cloud.artik.client.ApiException;
import cloud.artik.model.MessageAction;
import cloud.artik.model.MessageIDEnvelope;
import cloud.artik.model.NormalizedMessagesEnvelope;
import cloud.artik.model.UserEnvelope;

public class MessageActivity extends Activity {
    private static final String TAG = "MessageActivity";

    private static final String DEVICE_ID = "<YOUR DEVICE ID>";
    public static final String KEY_ACCESS_TOKEN = "Access_Token";

    private UsersApi mUsersApi = null;
    private MessagesApi mMessagesApi = null;
    
    private String mAccessToken;
    private TextView mWelcome;
    private TextView mSendResponse;
    private TextView mGetLatestResponseId;
    private TextView mGetLatestResponseData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        
        mAccessToken = getIntent().getStringExtra(KEY_ACCESS_TOKEN);
        Log.v(TAG, "::onCreate get access token = " + mAccessToken);

        Button sendMsgBtn = (Button)findViewById(R.id.send_btn);
        Button getLatestMsgBtn = (Button)findViewById(R.id.getlatest_btn);
        mWelcome = (TextView)findViewById(R.id.welcome);
        mSendResponse = (TextView)findViewById(R.id.sendmsg_response);
        mGetLatestResponseId = (TextView)findViewById(R.id.getlatest_response_mid);
        mGetLatestResponseData = (TextView)findViewById(R.id.getlatest_response_mdata);
        
        setupArtikCloudApi();

        getUserInfo();

        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v(TAG, ": send button is clicked.");

                // Reset UI
                mSendResponse.setText("Response:");

                postMsg();
            }
        });

        getLatestMsgBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v(TAG, ": get latest message button is clicked.");

                // Reset UI
                mGetLatestResponseId.setText("id:");
                mGetLatestResponseData.setText("data:");

                // Now get the message
                getLatestMsg();
            }
        });
    }

    private void setupArtikCloudApi() {
        ApiClient mApiClient = new ApiClient();
        mApiClient.setAccessToken(mAccessToken);
        mApiClient.setDebugging(true);

        mUsersApi = new UsersApi(mApiClient);
        mMessagesApi = new MessagesApi(mApiClient);
    }

    private void getUserInfo()
    {
        final String tag = TAG + " getSelfAsync";
        try {
            mUsersApi.getSelfAsync(new ApiCallback<UserEnvelope>() {
                @Override
                public void onFailure(ApiException exc, int statusCode, Map<String, List<String>> map) {
                    processFailure(tag, exc);
                }

                @Override
                public void onSuccess(UserEnvelope result, int statusCode, Map<String, List<String>> map) {
                    Log.v(TAG, "getSelfAsync::setupArtikCloudApi self name = " + result.getData().getFullName());
                    updateWelcomeViewOnUIThread("Welcome " + result.getData().getFullName());
                }

                @Override
                public void onUploadProgress(long bytes, long contentLen, boolean done) {
                }

                @Override
                public void onDownloadProgress(long bytes, long contentLen, boolean done) {
                }
            });
        } catch (ApiException exc) {
            processFailure(tag, exc);
        }
    }

    private void getLatestMsg() {
        final String tag = TAG + " getLastNormalizedMessagesAsync";
        try {
            int messageCount = 1;
            mMessagesApi.getLastNormalizedMessagesAsync(messageCount, DEVICE_ID, null,
                    new ApiCallback<NormalizedMessagesEnvelope>() {
                        @Override
                        public void onFailure(ApiException exc, int i, Map<String, List<String>> stringListMap) {
                            processFailure(tag, exc);
                        }

                        @Override
                        public void onSuccess(NormalizedMessagesEnvelope result, int i, Map<String, List<String>> stringListMap) {
                            Log.v(tag, " onSuccess latestMessage = " + result.getData().toString());
                            String mid = "";
                            String data = "";
                            if (!result.getData().isEmpty()) {
                                mid = result.getData().get(0).getMid();
                                data = result.getData().get(0).getData().toString();
                            }
                            updateGetResponseOnUIThread(mid, data);
                        }

                        @Override
                        public void onUploadProgress(long bytes, long contentLen, boolean done) {
                        }

                        @Override
                        public void onDownloadProgress(long bytes, long contentLen, boolean done) {
                        }
                    });

        } catch (ApiException exc) {
            processFailure(tag, exc);
        }
    }

    private void postMsg() {
        final String tag = TAG + " sendMessageActionAsync";

        HashMap<String, Object> data = new HashMap<String, Object>();
        data.put("stepCount", 4393);
        data.put("heartRate", 110);
        data.put("description", "Run");
        data.put("activity", 2);

        MessageAction msg = new MessageAction();
        msg.setSdid(DEVICE_ID);
        msg.setData(data);

        try {
            mMessagesApi.sendMessageActionAsync(msg, new ApiCallback<MessageIDEnvelope>() {
                @Override
                public void onFailure(ApiException exc, int i, Map<String, List<String>> stringListMap) {
                    processFailure(tag, exc);
                }

                @Override
                public void onSuccess(MessageIDEnvelope result, int i, Map<String, List<String>> stringListMap) {
                    Log.v(tag, " onSuccess response to sending message = " + result.getData().toString());
                    updateSendResponseOnUIThread(result.getData().toString());
                }

                @Override
                public void onUploadProgress(long bytes, long contentLen, boolean done) {
                }

                @Override
                public void onDownloadProgress(long bytes, long contentLen, boolean done) {
                }
            });
        } catch (ApiException exc) {
            processFailure(tag, exc);
        }
    }


    static void showErrorOnUIThread(final String text, final Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(activity.getApplicationContext(), text, duration);
                toast.show();
            }
        });
    }

    private void updateWelcomeViewOnUIThread(final String text) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWelcome.setText(text);
            }
        });
    }

    private void updateGetResponseOnUIThread(final String mid, final String msgData) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mGetLatestResponseId.setText("id:" + mid);
                mGetLatestResponseData.setText("data:" + msgData);
            }
        });
    }

    private void updateSendResponseOnUIThread(final String response) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSendResponse.setText("Response: " + response);
            }
        });
    }

    private void processFailure(final String context, ApiException exc) {
        String errorDetail = " onFailure with exception" + exc;
        Log.w(context, errorDetail);
        exc.printStackTrace();
        showErrorOnUIThread(context+errorDetail, MessageActivity.this);
    }

} //MessageActivity

