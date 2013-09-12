package cz.moskovcak.pcsms;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BluetoothStateChangedReceiver extends BroadcastReceiver {
    private final String TAG = this.getClass().getSimpleName();
    private final NotificationServerBluetooth btServer;

    public BluetoothStateChangedReceiver(NotificationServerBluetooth btServer) {
        this.btServer = btServer;
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //we use STATE_OFF as a fallback and just give up if it's off
        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
        System.out.println("Bluetooth state changed to: " + state);
        if (state == BluetoothAdapter.STATE_ON) {
            Log.i(TAG, "Bluetooth is settled, trying to connect");
            btServer.connect();
        }
        else if (state == BluetoothAdapter.STATE_OFF) {
            Log.i(TAG, "Bluetooth is disabled, disconnecting");
            BluetoothAdapter.getDefaultAdapter().enable();
            //btServer.disconnect();
        }
    }

}
