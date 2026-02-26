package uk.co.epicuri.waiter;

import java.util.HashMap;
import java.util.Map;

public class HelpFiles {
	private static Map<String, String> classToUrlLookup = new HashMap<>();
	
	static {
		classToUrlLookup.put("HubActivity", "http://epicuri.co.uk/help/HubView");
		classToUrlLookup.put("EndOfDayActivity", "http://epicuri.co.uk/help/CloseService");
		classToUrlLookup.put("SessionDetailActivity", "http://epicuri.co.uk/help/Session");
//		
	}
	
	public static String getUrlForClass(String className){
		if(classToUrlLookup.containsKey(className)){
			return classToUrlLookup.get(className);
		}
		return String.format("http://epicuri.co.uk/help/%s", className);
	}
}
