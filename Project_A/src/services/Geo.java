package services;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Geo extends Thread {
  private static PrintStream log = System.out;

  private Socket client;
  private Geo(Socket client) {
    this.client = client;
  }

  public void run() {
    log.printf("Connected to %s:%d\n", client.getInetAddress(), client.getPort());

    try (
      Socket client   = this.client;
      Scanner req     = new Scanner(client.getInputStream());
      PrintStream res = new PrintStream(client.getOutputStream(), true);
    ) {
      String response;
      String request = req.nextLine();
      String[] c1;
      double distance;
      double t1, t2, n1, n2;
      if (request.matches("^(-)?[0-9]+(.)?[0-9]+? (-)?[0-9]+(.)?[0-9]+ (-)?[0-9]+(.)?[0-9]+ (-)?[0-9]+(.)?[0-9]+$")) {
    	c1 = request.split("\\s+");
    	t1 = Double.parseDouble(c1[0]);
    	n1 = Double.parseDouble(c1[1]);
    	t2 = Double.parseDouble(c1[2]);
    	n2 = Double.parseDouble(c1[3]);
   
    	double d1 = Math.toRadians(t2-t1);
    	double d2 = Math.toRadians(n2-n1);
    	t1 = Math.toRadians(t1);
    	t2 = Math.toRadians(t2);
    	double x = Math.pow(Math.sin((d1) / 2) , 2) + Math.cos(t1) * Math.cos(t2) * Math.pow(Math.sin((d2) / 2) , 2);
    	distance = 12742.0 * Math.atan2(Math.sqrt(x), Math.sqrt(1-x));
        response = Double.toString(distance);
      } else {
        response = "Don't understand: " + request;
      }
      res.println(response);
    } catch (Exception e) {
      log.println(e);
    } finally {
      log.printf("Disconnected from %s:%d\n", client.getInetAddress(), client.getPort());
    }
  }

  public static void main(String[] args) throws Exception {
    int port = 0;
    InetAddress host = InetAddress.getLocalHost();
    try (ServerSocket server = new ServerSocket(port, 0, host)) {
      log.printf("Server listening on %s:%d\n", server.getInetAddress(), server.getLocalPort());
      while (true) {
        Socket client = server.accept();
        (new Geo(client)).start();
      }
    }
  }
}