package model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class LocModel {
	private static LocModel model = null;

	public String getLoc(String payload, String key) throws MalformedURLException {
		String responseText = "";
		payload = payload.replace(' ', '+');
    	URL url = new URL ("http://www.mapquestapi.com/geocoding/v1/address?key=" + key + "&location=" + payload);
    	try(Scanner rs = new Scanner(url.openStream())){
    		while(rs.hasNext()) {
    			responseText = rs.nextLine();
    		}
    		JsonObject res = new JsonParser().parse(responseText).getAsJsonObject();
    		responseText = res.getAsJsonObject().getAsJsonArray("results").get(0).getAsJsonObject().getAsJsonArray("locations").get(0).getAsJsonObject().getAsJsonObject("latLng").toString();
    		return responseText;
    	}catch(Exception e) {
    		return e.toString();
    	}
	}
	
	public static LocModel getInstance() {
		if (model == null) model = new LocModel();
		return model;
	}
	
}
