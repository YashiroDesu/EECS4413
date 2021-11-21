package services;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.GeoModel;

@WebServlet(name = "GeoWeb", urlPatterns = { "/GeoWeb" })
public class GeoWeb extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public GeoWeb() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintStream out = new PrintStream(response.getOutputStream());
		HttpSession session = request.getSession(true);
		String responseText = "";

		Map<String, String[]> parameters = request.getParameterMap();

		if (!parameters.containsKey("lat") || !parameters.containsKey("lng")) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		GeoModel model = GeoModel.getInstance();
			String curLat = request.getParameter("lat");
			String curLng = request.getParameter("lng");
			if (session.getAttribute("lat") == null && session.getAttribute("lng") == null) {
				session.setAttribute("lat", curLat);
				session.setAttribute("lng", curLat);
				responseText = "RECEIVED";
			}
			else {
			String preLat = (String) session.getAttribute("lat");
			String preLng = (String) session.getAttribute("lng");
			String payload = preLat + " " + preLng + " " + curLat + " " + curLng;
			String dist = model.getDist(payload, getServletContext().getInitParameter("GeoServiceIP"), Integer.parseInt(getServletContext().getInitParameter("GeoServicePort")));
			responseText = "The distance from (" + preLat + "," + preLng + ") to (" + curLat + "," + curLng + ") is: "
					+ dist + " km\n";
			session.setAttribute("lat", curLat);
			session.setAttribute("lng", curLat);
			}
		out.print(responseText);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}