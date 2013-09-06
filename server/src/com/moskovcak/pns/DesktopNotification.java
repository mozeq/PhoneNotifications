package com.moskovcak.pns;

import java.util.logging.Logger;

import org.gnome.gtk.Gtk;
import org.gnome.notify.Notify;
import org.gnome.notify.Notification;

public class DesktopNotification extends EventNotification {
	private final static String TAG = "com.moskovcak.pns";
	private static Logger log = Logger.getLogger(TAG);
	
	public DesktopNotification() {
		Gtk.init(null);
		Notify.init(TAG);
	}
	
	@Override
	public void show(String subject, String message, String type) {
		// TODO Auto-generated method stub
        Notification Hello = new Notification(subject, message, null);
        Hello.show();
        log.info("Notification out");
	}

}
