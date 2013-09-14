package cz.moskovcak.pcsms;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;

public class NotificationServerBluetooth implements NotificationServer {
    private static NotificationServerBluetooth sNotificationServerBluetooth = null;
    private final static int REQUEST_ENABLE_BT = 1;
    private final static String TAG = "cz.moskovcak.pcsms";
    private final BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mDevice = null;
    private BluetoothDevice mConnectedDevice = null;
    private DataOutputStream output = null;
    private final Activity activity;

    public static NotificationServerBluetooth getServer(Activity activity) {
        if (sNotificationServerBluetooth != null)
            return sNotificationServerBluetooth;

        synchronized(NotificationServerBluetooth.class) {
            if (sNotificationServerBluetooth != null)
                return sNotificationServerBluetooth;

            sNotificationServerBluetooth = new NotificationServerBluetooth(activity);
            return sNotificationServerBluetooth;
        }
    }

    private NotificationServerBluetooth(Activity activity) {
        this.activity = activity;
        System.out.println("Starting bluetooth");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            System.err.println("bluetooth is not available");
        }
        System.out.println(mBluetoothAdapter.isEnabled());
        if (!mBluetoothAdapter.isEnabled()) {
            Log.i(TAG, "Enabling bluetooth");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        load();
        Log.i(TAG, "bluetooth seems to be enabled");
    }

    public List<BluetoothDevice> getBondedDevices() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>(pairedDevices);
        // If there are paired devices
        Log.i(TAG, "# paired devices: " + pairedDevices.size());
        return devices;
    }

    /* why don't we try to connect at this point? because it can take a long time
     * and user might change his mind, so just set the device and connect later
     *
     */
    public void setDevice(BluetoothDevice device) {
        if (device != null) {
            //Log.i(TAG, "setDevice: " + mDevice.hashCode() + "->" + device.hashCode());
            if (mDevice == null || mDevice.getAddress() != device.getAddress())
                mDevice = device;
            save();
        }
    }

    public void setDevice(String btDeviceAddress) {
        setDevice(mBluetoothAdapter.getRemoteDevice(btDeviceAddress));
    }

    private void resetBt() {
        Log.i(TAG, "Resetting bluetooth");

        //BT state changed receiver
        BluetoothStateChangedReceiver mBluetoothReceiver = new BluetoothStateChangedReceiver(this);
        IntentFilter mBluetoothIntentFilter= new IntentFilter();
        mBluetoothIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        activity.registerReceiver(mBluetoothReceiver, mBluetoothIntentFilter);
        if (mBluetoothAdapter.disable() == false) {
            if (mBluetoothAdapter.getState() != BluetoothAdapter.STATE_OFF)
                Log.e(TAG, "Can't disable the bluetooth adapter");
        }
        Log.i(TAG, "Waiting for bluetooth to settle");
    }

    public boolean connect() {
        if (mDevice == null) {
            Log.e(TAG, "No device selected");
            return false;
        }
        //final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        final UUID myUUID = UUID.fromString("1e0ca4ea-299d-4335-93eb-27fcfe7fa848");

        try {
            // have to use the device address, the object seems to be non-equal after resume!!
            if (mConnectedDevice == null || !mConnectedDevice.getAddress().matches(mDevice.getAddress())) {
                if (mConnectedDevice != null)
                    System.out.println(mConnectedDevice.hashCode() + " <> " + mDevice.hashCode());
                Log.i(TAG, "Device has changed, connecting to: " + mDevice.getName());
                if (output != null)
                    output.close();  // always close streams!
                BluetoothSocket socket = mDevice.createRfcommSocketToServiceRecord(myUUID);
                mBluetoothAdapter.cancelDiscovery();
                socket.connect();  //first need to connect, otherwise we get an error: Transport endpoint is not connected
                output = new DataOutputStream(socket.getOutputStream());
                mConnectedDevice = mDevice;
            }
            Log.i(TAG, "Connected to: " + mDevice.getAddress());
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Can't connect to: " + mDevice.getName() +" : "+e.getLocalizedMessage());
            resetBt();
        }
        // we shouldn't get here if the connection was established
        Log.e(TAG, "Connecting to: " + mDevice.getName() + " failed");
        return false;
    }

    public void reconnect() {
        try {
            output.close();  //always close output stream, the system resource is not freed by garbage collector!
        } catch (IOException e) {
            //pass
        }
        output = null;
        connect();
    }

    /* protocol:
     * | 4bytes length | data | '|' as a separator
     */
    @SuppressLint("NewApi")
    public String sendDataJSON(Map<String, String> formData) throws JSONException {
        if (output == null) {
            Log.e(TAG, "Not connected!");
            return "Not sent!";
        }
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

        byte[] responseBytes = responseJSON.toString().getBytes();
        int retries = 3;  // try to resend 3 times and then give up
        for (int trySend = retries; trySend > 0; trySend --) {
            try {
                System.out.println("Sending: " + responseBytes.length + " bytes");
                if (output == null)
                    if (!connect()) {
                        Log.e(TAG, "Can't connect");
                    }
                output.writeInt(responseBytes.length);
                output.write(responseBytes);
                return "Sent";
            } catch (IOException e) {
                Log.e(TAG, "Can't send data: " + e.getLocalizedMessage());
                Log.i(TAG, "Trying to reconnect..");
                reconnect();

            }
        }
        Log.e(TAG, "Failed to send the notification");
        return "Not sent";
    }

    public String notifySMS(String phoneNumber, String contactName, String message) {
        Map<String, String> formData = new HashMap<String, String>();
        formData.put("eventName", "newSMS");
        formData.put("callerId", phoneNumber);
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
        Map<String, String> formData = new HashMap<String, String>();
        formData.put("eventName", "incomingCall");
        formData.put("callerId", phoneNumber);
        formData.put("state", state);
        formData.put("contactName", contactName);

        try {
            return sendDataJSON(formData);
        } catch (JSONException ex) {
            Log.e(TAG, "Can't send data"+ ex.getLocalizedMessage());
            return ex.getLocalizedMessage();
        }
    }

    public void load() {
        SharedPreferences settings = activity.getPreferences(Context.MODE_PRIVATE);
        String btDeviceAddress = settings.getString("btDeviceAddress", null);

        if (btDeviceAddress != null)
            setDevice(mBluetoothAdapter.getRemoteDevice(btDeviceAddress));
    }

    public void save() {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        if (mDevice != null) {
            SharedPreferences settings = activity.getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("btDeviceAddress", mDevice.getAddress());

            // Commit the edits!
            editor.commit();
        }

    }

    @Override
    public void disconnect() {
        mConnectedDevice = null;
        if (output != null) {
            try {
                output.close();
                output = null;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

}
