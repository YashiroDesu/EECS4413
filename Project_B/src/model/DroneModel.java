package model;

import model.LocModel;
import model.GeoModel;

import java.net.MalformedURLException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class DroneModel {
	private static DroneModel model = null;

	public static DroneModel getInstance() {
		if (model == null)
			model = new DroneModel();
		return model;
	}

	public String getTime(String loc1, String loc2, String key, String geoServiceIP, int GeoServicePort) {
		GeoModel geo = GeoModel.getInstance();
		LocModel loc = LocModel.getInstance();

		try {
			String dist = "";
			JsonObject coord1 = new JsonParser().parse(loc.getLoc(loc1, key)).getAsJsonObject();
			JsonObject coord2 = new JsonParser().parse(loc.getLoc(loc2, key)).getAsJsonObject();
			
			  String lat1 = coord1.get("lat").getAsString(); 
			  String lng1 = coord1.get("lng").getAsString(); 
			  String lat2 = coord2.get("lat").getAsString(); 
			  String lng2 =coord2.get("lng").getAsString(); 
			  String geoPayload = lat1 + " " + lng1 + " " + lat2 + " " + lng2; 
			  dist = geo.getDist(geoPayload, geoServiceIP, GeoServicePort); 
			  double time = Double.parseDouble(dist) / 150.0 * 60;			 
			return Double.toString(time);
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";
	}
}
