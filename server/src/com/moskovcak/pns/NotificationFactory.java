package com.moskovcak.pns;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class NotificationFactory {
	private static final Map<String, EventNotification> notificationMap;
    static {
        Map<String, EventNotification> aMap = new HashMap<String, EventNotification>();
        aMap.put("libnotify", new DesktopNotification());
        notificationMap = Collections.unmodifiableMap(aMap);
    }
	
	public static EventNotification getNotification(String type) {
		return notificationMap.get(type);
	}
}
