package com.moskovcak.pns;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.logging.Logger;;

public class IncommingPhonecallHandler implements NotificationEventHandler {
	private static Logger log = Logger.getLogger("com.moskovcak.pns");
	private final static String RINGING = "RINGING";
	private final static String IDLE = "IDLE";
	/* expects:
	 * {
	 *  "from": "<string>",
	 * }
	 */
	@Override
	public void handleNotification(JSONObject args) {
		log.info("Incomming phone call!");
		String from = null;
		String state = null;
		String contactName = null;
		try {
			from = args.getString("from");
			state = args.getString("state");
			contactName = args.getString("contactName");
		} catch (JSONException e) {
			log.severe("Can't read required arguments from JSON data: " + e.getLocalizedMessage());
		}
		EventNotification notification = NotificationFactory.getNotification("libnotify");
		if (state.equals(RINGING))
			notification.show("Incoming call from: " + contactName + "("+ from +")", null, "PhoneCall");
		else if (state.equals(IDLE))
			notification.show("Call ended: " + contactName + "("+ from +")", null, "PhoneCall");
	}

}
