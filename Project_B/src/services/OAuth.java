package services;

import java.io.IOException;
import java.io.PrintStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "OAuth", urlPatterns = { "/OAuth" })
public class OAuth extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(true);
		PrintStream out = new PrintStream(response.getOutputStream());
		String responseText = "";
		if (session.getAttribute("name") == null || session.getAttribute("user") == null) {
			session.setAttribute("name" , "");
			session.setAttribute("user" , "");
			response.sendRedirect("https://www.eecs.yorku.ca/~roumani/servers/auth/oauth.cgi?back=" + request.getRequestURL());
		} else {
			responseText = "Hello, <" + request.getParameter("name") + ">. You are logged in as <"
					+ request.getParameter("user") + ">.";
			out.print(responseText);
		}

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}