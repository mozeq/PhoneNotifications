package cz.moskovcak.pcsms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

class PhoneCallReceiver extends BroadcastReceiver
{
    private final String TAG = this.getClass().getSimpleName();
    private NotificationServer nServer = null;

    public PhoneCallReceiver(NotificationServer server) {
    	this.nServer = server;
    	
    }
    
    @Override
    public void onReceive(Context context, Intent intent)
    {
    	Bundle extras = intent.getExtras();
    	String callState = extras.getString(android.telephony.TelephonyManager.EXTRA_STATE);
    	String incomingNumber = extras.getString(android.telephony.TelephonyManager.EXTRA_INCOMING_NUMBER);
    	Log.i(TAG, "Ringing state: " + callState);
    	Log.i(TAG, "incomingNumber: " + incomingNumber);
    	Log.i(TAG, "Received phone call!");
    	this.nServer.notifyPhoneCall(incomingNumber, callState);
    }

}
