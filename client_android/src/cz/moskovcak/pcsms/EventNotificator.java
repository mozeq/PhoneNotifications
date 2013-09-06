package cz.moskovcak.pcsms;

import android.app.Activity;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class EventNotificator extends Activity {
	private SMSreceiver mSMSreceiver;
    private IntentFilter mIntentFilter;
    private IntentFilter mPhoneIntentFilter;
    private PhoneCallReceiver mPhoneCallReceiver;
    private final String TAG = this.getClass().getSimpleName();
    
	private NotificationServer nServer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_wifi_name);
        nServer = new NotificationServer(getApplicationContext());
        
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
    
    public void sendMessage(View view) {
    	EditText target = (EditText) findViewById(R.id.editText1);
    	EditText message = (EditText) findViewById(R.id.message);
    	String messageStr = message.getText().toString(); 
    	
        String response = nServer.notifySMS("123456", "nakej contact", messageStr);
    	
    	target.setText(response);
    }
    
    public void notifyPhoneCall(View view) {
    	EditText phoneCall = (EditText) findViewById(R.id.editText2);
    	
        String response = nServer.notifyPhoneCall(phoneCall.getText().toString(), "fake", "ringing");
    	EditText target = (EditText) findViewById(R.id.editText1);
    	target.setText(response);
    }
    
    public void onDestroy()
    {
    	super.onDestroy();
    	Log.i(TAG, "Destroying");
    	unregisterReceiver(mSMSreceiver);
    	unregisterReceiver(mPhoneCallReceiver);
    }
    
}
