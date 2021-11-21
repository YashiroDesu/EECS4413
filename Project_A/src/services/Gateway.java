package services;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;

public class Gateway extends Thread {
	private static PrintStream log = System.out;
	private static final Map<Integer, String> httpResponseCodes = new HashMap<>();
	private static String AuthServiceIP, QuoteServiceIP, GeoServiceIP;
	private static int AuthServicePort, QuoteServicePort, GeoServicePort;

	static {
		httpResponseCodes.put(100, "HTTP CONTINUE");
		httpResponseCodes.put(101, "SWITCHING PROTOCOLS");
		httpResponseCodes.put(200, "OK");
		httpResponseCodes.put(201, "CREATED");
		httpResponseCodes.put(202, "ACCEPTED");
		httpResponseCodes.put(203, "NON AUTHORITATIVE INFORMATION");
		httpResponseCodes.put(204, "NO CONTENT");
		httpResponseCodes.put(205, "RESET CONTENT");
		httpResponseCodes.put(206, "PARTIAL CONTENT");
		httpResponseCodes.put(300, "MULTIPLE CHOICES");
		httpResponseCodes.put(301, "MOVED PERMANENTLY");
		httpResponseCodes.put(302, "MOVED TEMPORARILY");
		httpResponseCodes.put(303, "SEE OTHER");
		httpResponseCodes.put(304, "NOT MODIFIED");
		httpResponseCodes.put(305, "USE PROXY");
		httpResponseCodes.put(400, "BAD REQUEST");
		httpResponseCodes.put(401, "UNAUTHORIZED");
		httpResponseCodes.put(402, "PAYMENT REQUIRED");
		httpResponseCodes.put(403, "FORBIDDEN");
		httpResponseCodes.put(404, "NOT FOUND");
		httpResponseCodes.put(405, "METHOD NOT ALLOWED");
		httpResponseCodes.put(406, "NOT ACCEPTABLE");
		httpResponseCodes.put(407, "PROXY AUTHENTICATION REQUIRED");
		httpResponseCodes.put(408, "REQUEST TIME OUT");
		httpResponseCodes.put(409, "CONFLICT");
		httpResponseCodes.put(410, "GONE");
		httpResponseCodes.put(411, "LENGTH REQUIRED");
		httpResponseCodes.put(412, "PRECONDITION FAILED");
		httpResponseCodes.put(413, "REQUEST ENTITY TOO LARGE");
		httpResponseCodes.put(414, "REQUEST URI TOO LARGE");
		httpResponseCodes.put(415, "UNSUPPORTED MEDIA TYPE");
		httpResponseCodes.put(500, "INTERNAL SERVER ERROR");
		httpResponseCodes.put(501, "NOT IMPLEMENTED");
		httpResponseCodes.put(502, "BAD GATEWAY");
		httpResponseCodes.put(503, "SERVICE UNAVAILABLE");
		httpResponseCodes.put(504, "GATEWAY TIME OUT");
		httpResponseCodes.put(505, "HTTP VERSION NOT SUPPORTED");
	}

	private Socket client;

	private Gateway(Socket client) {
		this.client = client;
	}

	private void sendHeaders(PrintStream res, int code, String contentType, String response) {
		// send HTTP Headers
		res.printf("HTTP/1.1 %d %s\n", code, httpResponseCodes.get(code));
		res.println("Server: Java HTTP Server : 1.0");
		res.println("Date: " + new Date());
		res.println("Content-type: " + contentType);
		res.println("Content-length: " + response.getBytes().length);
		res.println(); // blank line between headers and content, very important !
	}

	private Map<String, String> getQueryStrings(String qs) throws Exception {
		Map<String, String> queries = new HashMap<>();
		String[] fields = qs.split("&");

		for (String field : fields) {
			String[] pairs = field.split("=", 2);
			if (pairs.length == 2) {
				queries.put(pairs[0], URLDecoder.decode(pairs[1], "UTF-8"));
			}
		}
		return queries;
	}

	public void run() {
		final String clientAddress = String.format("%s:%d", client.getInetAddress(), client.getPort());
		log.printf("Connected to %s\n", clientAddress);
		log.printf("Connected to %s\n", clientAddress);

		try (Socket client = this.client; // Makes sure that client is closed at end of try-statement.
				Scanner req = new Scanner(client.getInputStream());
				PrintStream res = new PrintStream(client.getOutputStream(), true);) {
			String request = req.nextLine();
			String method, resource, version;
			String responseText = "";

			try (Scanner parse = new Scanner(request)) {
				method = parse.next();
				resource = parse.next();
				version = parse.next();
			}
			int status;
			String contentType = "text/plain";

			try {
				if (!method.equals("GET")) { // only support GET
					status = 501;
				} else if (!version.equals("HTTP/1.1")) { // only support HTTP version 1.1
					status = 505;
				} else {
					status = 200;
					Map<String, String> qs = getQueryStrings(resource.substring(resource.indexOf('?') + 1));
					if (resource.startsWith("/Auth?")) {
						String username = qs.get("username");
						String password = qs.get("password");
						if (username == null || password == null) {
							status = 400;
						} else {
							String payload = username + " " + password;
							try (Socket AuthScoket = new Socket(AuthServiceIP, AuthServicePort);
									Scanner out = new Scanner(AuthScoket.getInputStream());) {
								new PrintStream(AuthScoket.getOutputStream(), true).println(payload);
								responseText = out.nextLine();
							}
						}
					} else if (resource.startsWith("/Geo?")) {
						String lat1 = qs.get("t1");
						String lng1 = qs.get("n1");
						String lat2 = qs.get("t2");
						String lng2 = qs.get("n2");
						if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) {
							status = 400;
						} else {
							String payload = lat1 + " " + lng1 + " " + lat2 + " " + lng2;
							try (Socket GeoSocket = new Socket(GeoServiceIP, GeoServicePort);
									Scanner out = new Scanner(GeoSocket.getInputStream());) {
								new PrintStream(GeoSocket.getOutputStream(), true).println(payload);
								responseText = out.nextLine();
							}
						}
					} else if (resource.startsWith("/Quote?")) {
						String id = qs.get("id");
						String format = qs.get("format");
						if (id == null || format == null) {
							status = 400;
						} else {
							String payload = id + " " + format;
							try (Socket QuoteSocket = new Socket(QuoteServiceIP, QuoteServicePort);
									Scanner out = new Scanner(QuoteSocket.getInputStream());) {
								new PrintStream(QuoteSocket.getOutputStream(), true).println(payload);
								while (out.hasNext()) {
									responseText += out.nextLine();
								}
							}
						}
					} else {
						status = 404;
					}
				}
			} catch (Exception e) {
				status = 500;
			}

			if (status != 200) {
				responseText = httpResponseCodes.get(status);
			}

			log.printf("%s: %d - %s\n", clientAddress, status, request);
			sendHeaders(res, status, contentType, responseText);

			if (method.equals("GET")) {
				res.println(responseText);
			}

			res.flush(); // flush character output stream buffer
		} catch (Exception e) {
			log.println(e);
		} finally {
			log.printf("Disconnected from %s\n", clientAddress);
		}
	}

	public static void main(String[] args) throws Exception {
		int port = 0;
		InetAddress host = InetAddress.getLocalHost(); // .getLoopbackAddress();
		try (ServerSocket server = new ServerSocket(port, 0, host)) {
			log.printf("Server listening on %s:%d\n", server.getInetAddress(), server.getLocalPort());
			GeoServiceIP = args[0];
			GeoServicePort = Integer.parseInt(args[1]);
			AuthServiceIP = args[2];
			AuthServicePort = Integer.parseInt(args[3]);
			QuoteServiceIP = args[4];
			QuoteServicePort = Integer.parseInt(args[5]);
			while (true) {
				Socket client = server.accept();
				(new Gateway(client)).start();
			}
		}
	}
}