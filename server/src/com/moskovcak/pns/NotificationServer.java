package com.moskovcak.pns;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class NotificationServer {
	private static final String ENCODING = "UTF-8";
	private static Logger log = Logger.getLogger("com.moskovcak.pns");
	
    public static void main(String[] args) throws Exception {
    	Configuration config = ConfigLoader.getConfiguration("server.conf");
    	String hostname = config.getString("hostname");
    	int port = config.getInt("port");
    	log.info("Listening on: " + hostname +":" + port);
        HttpServer server = HttpServer.create(new InetSocketAddress(hostname, 8000), 0);
        server.createContext("/notification", new RequestDispatch());
        server.setExecutor(null); // creates a default executor
        server.start();
    }
    
    static class RequestDispatch implements HttpHandler {
    	private final String METHOD_NAME = "methodName";
    	private Map<String, NotificationEventHandler> methodDispatch = new HashMap<String, NotificationEventHandler>();
    	
    	
    	public void handle(HttpExchange t) throws IOException {
    		/* method table */
    		methodDispatch.put("newSMS", NewSMSHandler.getInstance());
    		methodDispatch.put("incommingCall", new IncommingPhonecallHandler());
    		
        	InputStream input = t.getRequestBody();
        	int contentLength = Integer.parseInt(t.getRequestHeaders().get("Content-length").get(0));
        	byte buffer[] = new byte[contentLength]; 
        	if (input.read(buffer, 0, contentLength) != contentLength)
        		log.warning("Read Incomplete!");
        	
        	input.close();
        	String jsonRequestString = new String(buffer, ENCODING);
        	//System.out.println(jsonRequestString);
        	JSONObject jsonRequest = null;
        	String methodName = null;
        	try {
        		jsonRequest = new JSONObject(new String(buffer, ENCODING));
        		methodName = jsonRequest.getString(METHOD_NAME);
        		//System.out.println("Method name: " + methodName);
        		JSONObject args = jsonRequest.getJSONObject("args");
				NotificationEventHandler evHandler = methodDispatch.get(methodName);
				evHandler.handleNotification(args);
        	
        	} catch (JSONException ex) {
        		log.severe("Can't parse JSON from: '"+ jsonRequestString +"' : " + ex);
        		methodName = "Unknown";
        	}
        	
        	/* Send response */
            String response;
            response = methodName;
            t.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
    	}
    	
    }

}