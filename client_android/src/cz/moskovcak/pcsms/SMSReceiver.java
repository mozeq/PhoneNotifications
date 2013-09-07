package cz.moskovcak.pcsms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

class SMSreceiver extends BroadcastReceiver
{
    private final String TAG = this.getClass().getSimpleName();
    private NotificationServer nServer = null;
    		
    public SMSreceiver(NotificationServer server) {
    	this.nServer = server;
    }

	/* { "methodName": "newSMS",
	 *   "args" : {
	 *   	"from": "val1",
	 *   	"message": "val2"
	 *   }
	 * }
	 * 
	 */
    @Override
    public void onReceive(Context context, Intent intent)
    {
    	System.out.println("Received sms!");
    	Bundle extras = intent.getExtras();

        String strMessage = "";

        if ( extras != null )
        {
            Object[] smsextras = (Object[]) extras.get( "pdus" );

            for ( int i = 0; i < smsextras.length; i++ )
            {
                SmsMessage smsmsg = SmsMessage.createFromPdu((byte[])smsextras[i]);

                String strMsgBody = smsmsg.getMessageBody().toString();
                String strMsgSrc = smsmsg.getOriginatingAddress();
                
                /* let's try to retrieve contact name for the number */
                String contactName = ContactInfoProvider.getNameForNumber(context, strMsgSrc);
                
                nServer.notifySMS(strMsgSrc, contactName, strMsgBody);
                strMessage += "SMS from " + contactName + "("+strMsgSrc + ") : " + strMsgBody;                    

                Log.i(TAG, strMessage);
            }

        }

    }

}
