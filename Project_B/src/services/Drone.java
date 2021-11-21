package services;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.DroneModel;
@WebServlet(name = "Drone", urlPatterns = { "/Drone" })

public class Drone extends HttpServlet{
	 /**
	 * 
	 */
	private static final long serialVersionUID = 1L;



	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		    PrintStream out = new PrintStream(response.getOutputStream());
		    Map<String, String[]> parameters = request.getParameterMap();
		    String responseText = "";
		    DroneModel model = DroneModel.getInstance();
		    if (!parameters.containsKey("source") || !parameters.containsKey("destination")) {
		    		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		          return;
		    	  }
		    else {
		    	String loc1 = request.getParameter("source");
		    	String loc2 = request.getParameter("destination");
		    	String time = model.getTime(loc1, loc2, 
		    			getServletContext().getInitParameter("MapQuestAPIKey"), 
		    			getServletContext().getInitParameter("GeoServiceIP"),
		    			Integer.parseInt(getServletContext().getInitParameter("GeoServicePort")));
		    	responseText = "The estimated delivery time is: " + time + " minutes.";
		    }
		    out.print(responseText);
		  }	
		  	
		  	

		  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		    doGet(request, response);
		  }
}
