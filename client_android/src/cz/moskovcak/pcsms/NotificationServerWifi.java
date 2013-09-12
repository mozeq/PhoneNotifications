package cz.moskovcak.pcsms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NotificationServerWifi implements NotificationServer {
    private static final String TAG = "PCSMS";
    private static final String ENCODING = "UTF-8";
    private HttpURLConnection urlConnection = null;
    private Map<String, String> formData = null;
    private final Activity activity;

    NotificationServerWifi(Activity activity) {
        this.activity = activity;
        this.connect();
    }

    public boolean connect() {
        URL url = null;
        try {
            url = new URL("http://192.168.1.148:8000/notification/");
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            this.urlConnection = (HttpURLConnection) url.openConnection();
            this.urlConnection.setRequestMethod("POST");
            this.urlConnection.setDoInput(true);
            this.urlConnection.setDoOutput(true);
            this.urlConnection.setUseCaches(false);
            return true;
        }
        catch (ProtocolException e) {
            Log.v(TAG, "Can't open http connection: ", e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return false;
    }


    public Boolean isAvailable() {
        ConnectivityManager connMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = connMgr.getActiveNetworkInfo();

        if (netinfo != null && netinfo.isConnected())
            return true;

        return false;
    }

    private String sendDataJSON(Map<String, String> formData) throws JSONException {
        connect();

        JSONObject responseJSON = new JSONObject();
        responseJSON.put("methodName", formData.remove("eventName"));

        if (formData.size() > 0) // do we have some args?
            responseJSON.put("args", new JSONObject());

        for (String key: formData.keySet()) {
            try {
                ((JSONObject )responseJSON.get("args")).put(key, formData.get(key));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }


        //System.out.println("Sending: " + responseJSON.toString());
        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        urlConnection.setRequestProperty("Content-Length", ""+ responseJSON.toString().getBytes().length);

        DataOutputStream outStream = null;
        DataInputStream inStream = null;

        // Create I/O streams
        try {
            outStream = new DataOutputStream(urlConnection.getOutputStream());
        } catch (IOException e) {
            Log.e(TAG, "Can't get output stream", e);
            return null;
        }

        // Send request
        try {
            /* have to write the body before opening the instream, otherwise I get
             * an Exception:
             * promised XY bytes, but the actual length is 0;
             */

            //System.out.println("Sending: "+ new String(responseJSON.toString().getBytes()));
            outStream.write(responseJSON.toString().getBytes());
            outStream.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, "Can't send request: ", e);
        }


        String response = null;
        if (urlConnection.getContentLength() > 0) {
            // Get Response
            try {
                byte buffer[] = new byte[urlConnection.getContentLength()];
                inStream = new DataInputStream(urlConnection.getInputStream());
                if (inStream.read(buffer, 0, urlConnection.getContentLength()) != -1) {
                    response = new String(buffer, ENCODING);
                    Log.i(TAG, "Got response: " + response);
                }
            } catch (IOException e) {
                Log.e(TAG, "Can't read response: ", e);
            }
        }
        else {
            Log.e(TAG, "Empty response!");
        }

        //after successful send, clear the data for next usage, just for sure
        formData.clear();

        // Close I/O streams
        try {
            if (inStream != null)
                inStream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        /* have to the connection, otherwise we get:
         *  Cannot set request property after connection is made
         */
        urlConnection.disconnect();
        return response;

    }

    private void initFormData() {
        if (formData == null)
            formData = new HashMap<String, String>();
    }

    public String notifySMS(String phoneNumber, String contactName, String message) {
        initFormData();
        formData.put("eventName", "newSMS");
        formData.put("from", phoneNumber);
        formData.put("message", message);
        formData.put("contactName", contactName);
        try {
            return sendDataJSON(formData);
        } catch (JSONException ex) {
            Log.e(TAG, "Can't get the eventName"+ ex.getLocalizedMessage());
            return ex.getLocalizedMessage();
        }
    }

    public String notifyPhoneCall(String phoneNumber, String contactName, String state) {
        initFormData();
        formData.put("eventName", "incomingCall");
        formData.put("from", phoneNumber);
        formData.put("state", state);
        formData.put("contactName", contactName);

        try {
            return sendDataJSON(formData);
        } catch (JSONException ex) {
            Log.e(TAG, "Can't send data"+ ex.getLocalizedMessage());
            return ex.getLocalizedMessage();
        }
    }

    public void save() {
        ;
    }

    public void load(){}

    @Override
    public void disconnect() {
        ;
    };

}
