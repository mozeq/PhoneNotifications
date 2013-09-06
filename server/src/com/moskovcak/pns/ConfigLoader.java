package com.moskovcak.pns;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ConfigLoader {

	public static Configuration getConfiguration(String configFile) {
		Configuration config = new Configuration();
		try {
			BufferedReader in = new BufferedReader(new FileReader(configFile));
		    String str;
		    while ((str = in.readLine()) != null) {
		    		String[] parsedLine = str.split("=");

		    		//System.out.println(str);
		    		//System.out.println(parsedLine);

		    		//for (String s: parsedLine)
		    		//	System.out.println(s);

		    		if (parsedLine == null || parsedLine.length != 2)
		    			continue;

		    		//System.out.printf("'%s' = '%s'", parsedLine[0], parsedLine[1]);
		    		config.put(parsedLine[0].trim(), parsedLine[1].trim());
		    }
		    in.close();
		} catch (IOException e) {
			System.err.println("Can't read config file: " + configFile);
			return null;
		}

		return config;
	}
}
