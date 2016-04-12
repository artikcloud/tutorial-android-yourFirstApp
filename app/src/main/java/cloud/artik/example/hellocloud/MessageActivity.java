package cloud.artik.example.hellocloud;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;

import cloud.artik.api.MessagesApi;
import cloud.artik.api.UsersApi;
import cloud.artik.model.MessageAction;
import cloud.artik.model.MessageIDEnvelope;
import cloud.artik.model.NormalizedMessagesEnvelope;
import cloud.artik.model.UserEnvelope;
import cloud.artik.client.ApiClient;

public class MessageActivity extends Activity {
    private static final String TAG = "MessageActivity";

    public static final String KEY_ACCESS_TOKEN = "Access_Token";
    private static final String DEVICE_ID = "xxxx";

    private ApiClient mApiClient = null;
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
        new CallUsersApiInBackground().execute(mUsersApi);

        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    Log.v(TAG, ": send button is clicked.");
                    mSendResponse.setText("Response:");
                    new PostMsgInBackground().execute(mMessagesApi);
                } catch (Exception e) {
                    Log.v(TAG, "Run into Exception");
                    e.printStackTrace();
                }
            }
        });

        getLatestMsgBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    Log.v(TAG, ": get latest message button is clicked.");
                    
                    // Reset UI
                    mGetLatestResponseId.setText("id:");
                    mGetLatestResponseData.setText("data:");
            
                    // Now get the message
                    new GetLatestMsgInBackground().execute(mMessagesApi);
                } catch (Exception e) {
                    Log.v(TAG, "Run into Exception");
                    e.printStackTrace();
                }
            }
        });
    }

    private void setupArtikCloudApi() {
        mApiClient = new ApiClient();
        //You can override the API endpoint if needed by mApiClient.setBasePath("https://api.artik.cloud/v1.1");
        mApiClient.setAccessToken(mAccessToken);
        mApiClient.setDebugging(true);

        mUsersApi = new UsersApi(mApiClient);
        mMessagesApi = new MessagesApi(mApiClient);
    }
    
    class CallUsersApiInBackground extends AsyncTask<UsersApi, Void, UserEnvelope> {
        final static String TAG = "CallUsersApiInBackground";
        @Override
        protected UserEnvelope doInBackground(UsersApi... apis) {
            UserEnvelope retVal = null;
            try {
                retVal= apis[0].getSelf();
            } catch (Exception e) {
                Log.v(TAG, "::doInBackground run into Exception");
                e.printStackTrace();
            }

            return retVal;
        }
        
        @Override
        protected void onPostExecute(UserEnvelope result) {
            Log.v(TAG, "::setupArtikCloudApi self name = " + result.getData().getFullName());
            mWelcome.setText("Welcome " + result.getData().getFullName());
        }
    }

    class GetLatestMsgInBackground extends AsyncTask<MessagesApi, Void, NormalizedMessagesEnvelope> {
        final static String TAG = "GetLatestMsgInBackground";
        @Override
        protected NormalizedMessagesEnvelope doInBackground(MessagesApi... apis) {
            NormalizedMessagesEnvelope retVal = null;
            try {
                int messageCount = 1;
                retVal= apis[0].getLastNormalizedMessages(messageCount, DEVICE_ID, null);
            } catch (Exception e) {
                Log.v(TAG, "::doInBackground run into Exception");
                e.printStackTrace();
            }
            return retVal;
            
        }
        
        @Override
        protected void onPostExecute(NormalizedMessagesEnvelope result) {
          if (result == null || result.getData() == null 
                    || result.getData().size() == 0) {
               Log.v(TAG, "::onPostExecute result or data is null or data is empty!");
               mGetLatestResponseId.setText("id:" + " null");
               mGetLatestResponseData.setText("data:" + " null");
               return;
           }
           Log.v(TAG, "::onPostExecute latestMessage = " + result.getData().toString());
           mGetLatestResponseId.setText("id:" + result.getData().get(0).getMid());
           mGetLatestResponseData.setText("data:" + result.getData().get(0).getData().toString());
        }
    }

    class PostMsgInBackground extends AsyncTask<MessagesApi, Void, MessageIDEnvelope> {
        final static String TAG = " PostMsgInBackground";
        @Override
        protected MessageIDEnvelope doInBackground(MessagesApi... apis) {
            MessageIDEnvelope retVal = null;
            try {
                HashMap<String, Object> data = new HashMap<String, Object>();
                data.put("stepCount", 4393);
                data.put("heartRate", 110);
                data.put("description", "Run");
                data.put("activity", 2);

                MessageAction msg = new MessageAction();
                msg.setSdid(DEVICE_ID);
                msg.setData(data);
                retVal= apis[0].sendMessageAction(msg);
            } catch (Exception e) {
                Log.v(TAG, "::doInBackground run into Exception");
                e.printStackTrace();
            }

            return retVal;
        }
        
        @Override
        protected void onPostExecute(MessageIDEnvelope result) {
            if (result == null) {
               Log.v(TAG, "::onPostExecute result is null!");
               return;
            }
           Log.v(TAG, "::onPostExecute response to sending message = " + result.getData().toString());
           mSendResponse.setText("Response: " + result.getData().toString());
        }
    }
    
} //MessageActivity

