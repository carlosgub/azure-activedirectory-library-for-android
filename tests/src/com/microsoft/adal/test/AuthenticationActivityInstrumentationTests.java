
package com.microsoft.adal.test;

import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.app.Instrumentation.ActivityMonitor;
import android.app.Instrumentation.ActivityResult;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.microsoft.adal.ADALError;
import com.microsoft.adal.AuthenticationActivity;
import com.microsoft.adal.AuthenticationResult.AuthenticationStatus;
import com.microsoft.adal.Logger.ILogger;
import com.microsoft.adal.Logger.LogLevel;
import com.microsoft.adal.PromptBehavior;
import com.microsoft.adal.testapp.MainActivity;
import com.microsoft.adal.testapp.R;

/**
 * This requires device to be connected to not deal with Inject_events security exception.
 * UI functional tests that enter credentials to test token processing end to end.
 * 
 * @author omercan
 */
public class AuthenticationActivityInstrumentationTests extends
        ActivityInstrumentationTestCase2<MainActivity> {

    protected final static int PAGE_LOAD_WAIT_TIME_OUT = 20000; // miliseconds

    private static final String TAG = "AuthenticationActivityInstrumentationTests";

    private MainActivity activity;

    /**
     * until page content has something about login page
     */
    private static int PAGE_LOAD_TIMEOUT_SECONDS = 6;

    public AuthenticationActivityInstrumentationTests() {
        super(MainActivity.class);
        activity = null;
    }

    public AuthenticationActivityInstrumentationTests(Class<MainActivity> activityClass) {
        super(activityClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        activity = getActivity();
    }

    public void testAcquireTokenADFS30Federated() throws Exception {
        acquireTokenAfterReset("https://login.windows-ppe.net/AdalE2ETenant1.ccsctp.net",
                "http://adalscenariohealthwebapi.azurewebsites.net/",
                "f556da69-f8b3-4058-a3f8-01d9b60d7df8", "https://login.live.com/", null,
                PromptBehavior.Auto, null, "adaluser@ade2eadfs30.com", "P@ssw0rd", true,
                "https://fs.ade2eadfs30.com");
    }

    public void testAcquireTokenADFS30() throws Exception {
        acquireTokenAfterReset("https://fs.ade2eadfs30.com/adfs",
                "urn:msft:ad:test:oauth:teamdashboard", "DE25CE3A-B772-4E6A-B431-96DCB5E7E559",
                "https://login.live.com/", null, PromptBehavior.Auto, null,
                "ade2eadfs30.com\\adaluser", "P@ssw0rd", false, null);
    }

    public void testAcquireTokenManaged() throws Exception {
        acquireTokenAfterReset("https://login.windows.net/omercantest.onmicrosoft.com",
                "https://omercantest.onmicrosoft.com/AllHandsTry",
                "650a6609-5463-4bc4-b7c6-19df7990a8bc", "http://taskapp", "", PromptBehavior.Auto,
                null, "faruk@omercantest.onmicrosoft.com", "Jink1234", false, null);
    }

    /**
     * clear tokens and then ask for token.
     * 
     * @throws Exception
     */
    private void acquireTokenAfterReset(String authority, String resource, String clientid,
            String redirect, String loginhint, PromptBehavior prompt, String extraQueryParam,
            String username, String password, boolean federated, String federatedPageUrl)
            throws Exception {

        // ACtivity runs at main thread. Test runs on different thread
        Log.v(TAG, "acquireTokenAfterReset starts for authority:" + authority);
        // add monitor to check for the auth activity
        final ActivityMonitor monitor = getInstrumentation().addMonitor(
                AuthenticationActivity.class.getName(), null, false);

        // press clear all button to clear tokens and cookies
        Button btnResetToken = (Button)activity.findViewById(R.id.buttonReset);
        Button btnGetToken = (Button)activity.findViewById(R.id.buttonGetToken);
        final TextView textViewStatus = (TextView)activity.findViewById(R.id.textViewStatus);
        EditText mAuthority, mResource, mClientId, mUserid, mPrompt, mRedirect;
        CheckBox mValidate;

        mAuthority = (EditText)activity.findViewById(R.id.editAuthority);
        mResource = (EditText)activity.findViewById(R.id.editResource);
        mClientId = (EditText)activity.findViewById(R.id.editClientid);
        mUserid = (EditText)activity.findViewById(R.id.editUserId);
        mPrompt = (EditText)activity.findViewById(R.id.editPrompt);
        mRedirect = (EditText)activity.findViewById(R.id.editRedirect);
        mValidate = (CheckBox)activity.findViewById(R.id.checkBoxValidate);

        // Buttons need to be visible on the device
        setEditText(mAuthority, authority);
        sendKeys(KeyEvent.KEYCODE_TAB);
        setEditText(mResource, resource);
        sendKeys(KeyEvent.KEYCODE_TAB);
        setEditText(mClientId, clientid);
        sendKeys(KeyEvent.KEYCODE_TAB);
        setEditText(mUserid, loginhint);
        sendKeys(KeyEvent.KEYCODE_TAB);
        setEditText(mPrompt, prompt.name());
        sendKeys(KeyEvent.KEYCODE_TAB);
        setEditText(mRedirect, redirect);

        // TouchUtils handles the sync with the main thread internally
        TouchUtils.clickView(this, btnResetToken);

        // get token
        TouchUtils.clickView(this, btnGetToken);

        Thread.sleep(1000);
        Log.v(TAG, "testAcquireTokenAfterReset status text:" + textViewStatus.getText().toString());
        assertEquals("Token action", "Getting token...", textViewStatus.getText().toString());

        // Wait 4 secs to start activity and loading the page
        AuthenticationActivity startedActivity = (AuthenticationActivity)monitor
                .waitForActivityWithTimeout(5000);
        assertNotNull(startedActivity);

        Log.v(TAG, "Sleeping until it gets login page");
        sleepUntilLoginDisplays(startedActivity);

        Log.v(TAG, "Entering credentials to login page");
        enterCredentials(startedActivity, username, password);

        if (federated) {
            // federation page redirects to login page
            Log.v(TAG, "Sleep for redirect");
            sleepUntilFederatedPageDisplays(federatedPageUrl);

            Log.v(TAG, "Sleeping until it gets login page");
            sleepUntilLoginDisplays(startedActivity);
            Log.v(TAG, "Entering credentials to login page");
            enterCredentials(startedActivity, username, password);
        }

        // wait for the page to set result
        waitUntil(new ResponseVerifier() {
            @Override
            public boolean hasCondition() throws IllegalArgumentException, NoSuchFieldException,
                    IllegalAccessException {
                ActivityResult result = monitor.getResult();
                return result != null;
            }
        });

        waitUntil(new ResponseVerifier() {
            @Override
            public boolean hasCondition() throws IllegalArgumentException, NoSuchFieldException,
                    IllegalAccessException {
                String tokenMsg = (String)textViewStatus.getText();
                return tokenMsg.contains("Status:");
            }
        });

        String tokenMsg = (String)textViewStatus.getText();
        Log.v(TAG, "Status:" + tokenMsg);
        assertTrue("Token status", tokenMsg.contains("Status:" + AuthenticationStatus.Succeeded));
        Log.v(TAG, "Shutting down activity if it is active");
        if (!activity.isFinishing()) {
            activity.finish();
        }
    }

    private void setEditText(EditText view, String text) {
        view.clearComposingText();
        TouchUtils.tapView(this, view);
        getInstrumentation().sendStringSync(text);
    }

    private void enterCredentials(AuthenticationActivity startedActivity, String username,
            String password) throws InterruptedException {

        // Get Webview to enter credentials for testing
        WebView webview = (WebView)startedActivity.findViewById(com.microsoft.adal.R.id.webView1);
        assertNotNull("Webview is not null", webview);
        webview.requestFocus();

        // Send username
        Thread.sleep(500);
        getInstrumentation().sendStringSync(username);
        Thread.sleep(1000); // wait for redirect script
        sendKeys(KeyEvent.KEYCODE_TAB);
        getInstrumentation().sendStringSync(password);
        Thread.sleep(300);
        
        // Enter event sometimes is failing to submit form.
        sendKeys(KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_ENTER);
    }

    private void sleepUntilFederatedPageDisplays(final String federatedPageUrl)
            throws IllegalArgumentException, InterruptedException, NoSuchFieldException,
            IllegalAccessException {
        Log.v(TAG, "sleepUntilFederatedPageDisplays:" + federatedPageUrl);

        final CountDownLatch signal = new CountDownLatch(1);
        final ILogger loggerCallback = new ILogger() {
            @Override
            public void Log(String tag, String message, String additionalMessage, LogLevel level,
                    ADALError errorCode) {

                Log.v(TAG, "sleepUntilFederatedPageDisplays Message playback:" + message);
                if (message.toLowerCase(Locale.US).contains("page finished:" + federatedPageUrl)) {
                    Log.v(TAG, "sleepUntilFederatedPageDisplays Page is loaded:" + federatedPageUrl);
                    signal.countDown();
                }
            }
        };

        activity.setLoggerCallback(loggerCallback);

        try {
            signal.await(PAGE_LOAD_WAIT_TIME_OUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            assertFalse("Timeout " + getName(), true);
        }
    }

    private void sleepUntilLoginDisplays(final AuthenticationActivity startedActivity)
            throws InterruptedException, IllegalArgumentException, NoSuchFieldException,
            IllegalAccessException {

        Log.v(TAG, "sleepUntilLoginDisplays");

        waitUntil(new ResponseVerifier() {
            @Override
            public boolean hasCondition() throws IllegalArgumentException, NoSuchFieldException,
                    IllegalAccessException {
                return hasLoginPage(getLoginPage(startedActivity));
            }
        });
    }

    private void waitUntil(ResponseVerifier item) throws InterruptedException,
            IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        int waitcount = 0;
        Log.v(TAG, "wait start...");
        while (waitcount < PAGE_LOAD_TIMEOUT_SECONDS) {

            if (item.hasCondition()) {
                Log.v(TAG, "waitUntil done");
                break;
            }

            Thread.sleep(1000);
            waitcount++;
        }
        Log.v(TAG, "wait ends");
    }

    interface ResponseVerifier {
        boolean hasCondition() throws IllegalArgumentException, NoSuchFieldException,
                IllegalAccessException;
    }

    /**
     * Login page content is written to the script object with javascript
     * injection
     * 
     * @param startedActivity
     * @return
     * @throws IllegalArgumentException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private String getLoginPage(AuthenticationActivity startedActivity)
            throws IllegalArgumentException, NoSuchFieldException, IllegalAccessException {
        Object scriptInterface = ReflectionUtils.getFieldValue(startedActivity, "mScriptInterface");

        Object content = ReflectionUtils.getFieldValue(scriptInterface, "mHtml");

        // skip empty page
        if (content != null
                && !content.toString().equalsIgnoreCase("<html><head></head><body></body></html>"))
            return content.toString();

        return null;
    }

    /**
     * this can change based on login page implementation
     * 
     * @param htmlContent
     * @return
     */
    private boolean hasLoginPage(String htmlContent) {
        return htmlContent != null && !htmlContent.isEmpty() && htmlContent.contains("password");
    }
}