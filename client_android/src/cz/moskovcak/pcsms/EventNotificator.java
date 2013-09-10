package cz.moskovcak.pcsms;


import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.json.JSONException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class EventNotificator extends Activity {
	private SMSreceiver mSMSreceiver;
    private IntentFilter mIntentFilter;
    private IntentFilter mPhoneIntentFilter;
    private PhoneCallReceiver mPhoneCallReceiver;
    private final String TAG = this.getClass().getSimpleName();
    
	private NotificationServer nServer = NotificationServerBluetooth.getServer(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_wifi_name);
        //nServer = new NotificationServer(getApplicationContext());

        //SMS event receiver
        mSMSreceiver = new SMSreceiver(nServer);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(mSMSreceiver, mIntentFilter);

        //Phone call receiver
        mPhoneCallReceiver = new PhoneCallReceiver(nServer);
        mPhoneIntentFilter = new IntentFilter();
        mPhoneIntentFilter.addAction(android.telephony.TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(mPhoneCallReceiver, mPhoneIntentFilter);

        //mark it final so we can use it in onItemClickListener
        final NotificationServerBluetooth btServer = NotificationServerBluetooth.getServer(this);
        List<BluetoothDevice> devices = btServer.getBondedDevices();
        DeviceListViewAdapter devAdapter = new DeviceListViewAdapter(getBaseContext(), devices);
        ListView listview = (ListView) findViewById(R.id.id_devices_list);
        listview.setAdapter(devAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @SuppressLint("NewApi")
			@Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                final BluetoothDevice device = (BluetoothDevice) parent.getItemAtPosition(position);

                Log.i(TAG, "Selected: " + device.getAddress());
                btServer.setDevice(device);
                btServer.connect();
                /* @@TODO: for testing purposes, remove */
                try {
                    btServer.sendDataJSON(new HashMap<String, String>());
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        });
    }

    @Override
	protected void onResume() {
    	Log.i(TAG, "Registering sms receiver");
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.wifi_name, menu);
        return true;
    }
    
    public void onDestroy()
    {
    	super.onDestroy();
    	Log.i(TAG, "Destroying");
    	unregisterReceiver(mSMSreceiver);
    	unregisterReceiver(mPhoneCallReceiver);
    }
    
}
