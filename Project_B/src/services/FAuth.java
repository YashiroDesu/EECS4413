package services;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "FAuth", urlPatterns = { "/FAuth" })
public class FAuth extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public FAuth() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		PrintStream out = new PrintStream(response.getOutputStream());

		Map<String, String[]> parameters = request.getParameterMap();
		String responseText;
		if (!parameters.containsKey("username") || !parameters.containsKey("password")) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String payload = username + " " + password;
		try (Socket authSocket = new Socket(getServletConfig().getInitParameter("serviceAddress"),
				Integer.parseInt(getServletConfig().getInitParameter("servicePort")));
				Scanner res = new Scanner(authSocket.getInputStream());) {
			new PrintStream(authSocket.getOutputStream(), true).println(payload);
			responseText = res.nextLine();
		}
		out.print(responseText);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}