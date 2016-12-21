package cloud.artik.example.hellocloud;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class MainActivity extends Activity {
    static final String TAG = "MainActivity";
    private static final String ARTIK_CLOUD_AUTH_BASE_URL = "https://accounts.artik.cloud";
    private static final String CLIENT_ID = "<YOUR CLIENT ID>";// AKA application id
    private static final String REDIRECT_URL = "http://localhost:8000/acdemo/index.php";

    private View mLoginView;
    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWebView = (WebView)findViewById(R.id.webview);
        mWebView.setVisibility(View.GONE);
        mLoginView = findViewById(R.id.ask_for_login);
        mLoginView.setVisibility(View.VISIBLE);
        Button button = (Button)findViewById(R.id.btn);

        Log.v(TAG, "::onCreate");
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    Log.v(TAG, ": button is clicked.");
                    loadWebView();
                } catch (Exception e) {
                    Log.v(TAG, "Run into Exception");
                    e.printStackTrace();
                }
            }
        });

    }

    @SuppressLint("SetJavaScriptEnabled")
    private void loadWebView() {
        Log.v(TAG, "::loadWebView");
        mLoginView.setVisibility(View.GONE);
        mWebView.setVisibility(View.VISIBLE);
        mWebView.getSettings().setJavaScriptEnabled(true);
        
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String uri) {
                if ( uri.startsWith(REDIRECT_URL) ) {
                    // Login succeed or back to login after signup

                    if (uri.contains("access_token=")) { //login succeed
                        // Redirect URL has format http://localhost:8000/acdemo/index.php#expires_in=1209600&token_type=bearer&access_token=xxxx
                        // Extract OAuth2 access_token in URL
                        String[] sArray = uri.split("access_token=");
                        String strHavingAccessToken = sArray[1];
                        String accessToken = strHavingAccessToken.split("&")[0];
                        startMessageActivity(accessToken);
                    } else { // No access token available. Signup finishes and user clicks "Back to login"
                        // Example of uri: http://localhost:8000/acdemo/index.php?origin=signup&status=login_request
                        //
                        eraseAuthThenLogin();
                    }
                    return true;
                }
                // Load the web page from URL (login and grant access)
                return super.shouldOverrideUrlLoading(view, uri);
            }
        });
        
        String url = getAuthorizationRequestUri();
        Log.v(TAG, "webview loading url: " + url);
        mWebView.loadUrl(url);
    }
    
    public String getAuthorizationRequestUri() {
        //https://accounts.artik.cloud/authorize?client=mobile&client_id=xxxx&response_type=token&redirect_uri=http://localhost:8000/acdemo/index.php
        return ARTIK_CLOUD_AUTH_BASE_URL + "/authorize?client=mobile&response_type=token&" +
                     "client_id=" + CLIENT_ID + "&redirect_uri=" + REDIRECT_URL;
    }
    
    private void startMessageActivity(String accessToken) {
        Intent msgActivityIntent = new Intent(this, MessageActivity.class);
        msgActivityIntent.putExtra(MessageActivity.KEY_ACCESS_TOKEN, accessToken);
        startActivity(msgActivityIntent);
    }

    private void eraseAuthThenLogin() {
        CookieManager.getInstance().removeAllCookie();
        mWebView.loadUrl(getAuthorizationRequestUri());
    }
}
