package com.moskovcak.pns;

import org.json.JSONObject;

public interface NotificationEventHandler {
	void handleNotification(JSONObject args);
}
