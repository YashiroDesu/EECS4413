package services;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import model.LocModel;




@WebServlet(name = "Loc", urlPatterns = { "/Loc" })
public class Loc extends HttpServlet {
  private static final long serialVersionUID = 1L;
  public Loc() {
    super();
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    PrintStream out = new PrintStream(response.getOutputStream());
    Map<String, String[]> parameters = request.getParameterMap();
    String responseText = "";
    if (!parameters.containsKey("location")) {
    		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          return;
    	  }
    else {
    	LocModel model = LocModel.getInstance();
    	String payload = request.getParameter("location");
    	responseText = model.getLoc(payload, getServletContext().getInitParameter("MapQuestAPIKey"));
    }
    out.print(responseText);
  }	
  	
  	

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }
}