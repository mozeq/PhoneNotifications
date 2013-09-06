package com.moskovcak.pns;

import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

/* expects:
 * {
 *  "from": "<string>",
 *  "message" : "<string>
 * }
 */

public class NewSMSHandler implements NotificationEventHandler {
	static private NewSMSHandler smsHandler = null;
	private static Logger log = Logger.getLogger("com.moskovcak.pns");
	
	static NewSMSHandler getInstance() {
		if (smsHandler != null)
			return smsHandler;
		
		synchronized(NewSMSHandler.class) {
			if (smsHandler == null)
				smsHandler = new NewSMSHandler();
			
			return smsHandler;
		}
	}
	
	private NewSMSHandler() {
	};
	
	
	@Override
	public void handleNotification(JSONObject args) {
		log.info("Got new SMS!");
		String from = null;
		String messageBody = null;
		String contactName = null;
		try {
			from = args.getString("from");
			messageBody = args.getString("message");
			contactName = args.getString("contactName");
		} catch (JSONException e) {
			log.severe("Can't read required argumetns from JSON data: " + e.getLocalizedMessage());
		}
		
		EventNotification notification = NotificationFactory.getNotification("libnotify");
		notification.show("New message from: " + contactName + "("+ from +")", messageBody, "SMS");
		
	}

}