package cz.moskovcak.pcsms;

import android.app.Activity;

public abstract class NotificationServer {
    protected final Activity activity;

    protected NotificationServer(Activity activity) {
        this.activity = activity;
    }

    abstract public String notifySMS(String phoneNumber, String contactName, String message);
    abstract public String notifyPhoneCall(String phoneNumber, String contactName, String state);
}
