package model;

import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;


public class GeoModel {
  private static GeoModel model = null;
  private static PrintStream log = System.out;
	
	private GeoModel() {	}
	
	public static GeoModel getInstance() {
		if (model == null) model = new GeoModel();
		return model;
	}
	
	public String getDist(String payload, String geoHost, int geoPort) {
		try (Socket geoService = new Socket(geoHost, geoPort); 
				PrintStream req   = new PrintStream(geoService.getOutputStream(), true); 
				Scanner res       = new Scanner(geoService.getInputStream());
		) {
			new PrintStream(geoService.getOutputStream(), true).println(payload);
			String dist =  res.nextLine();
			return dist;
		} catch (Exception e) {
			log.println(e);
			return "Failed to complete connection with ID";
		} finally {
			log.printf("Disconnected from ID %s:%d\n", geoHost, geoPort);
		}
	}
}