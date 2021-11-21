package services;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;


public class Geo2 extends Thread {

    private Socket client;
    private static HashMap<Integer, String[]> map = new HashMap<>();
    private static String servicehost;
    private static int serviceport;
    public static PrintStream log = System.out;
    public Geo2(Socket client) {
        this.client = client;
    }
    public static void main(String[] args) throws Exception {
        int port = 0;
        InetAddress host = InetAddress.getLocalHost(); // .getLoopbackAddress();
        servicehost = args[0];
        serviceport =Integer.parseInt(args[1]);
        try (ServerSocket server = new ServerSocket(port, 0, host)) {
            log.printf("Server listening on %s:%d\n", server.getInetAddress(), server.getLocalPort());
            while (true) {
                Socket client = server.accept();
                (new Geo2(client)).start();
            }
        }
    }
    
    private synchronized void newEntry (int cookie, String lat, String lng) {
    	map.put(cookie, new String[]{lat, lng});
    }
    

    public void run() {
        try (Scanner in = new Scanner(client.getInputStream()); PrintStream res = new PrintStream(client.getOutputStream())) {
            String response;
            String request = in .nextLine();
            Random rand = new Random();
            String[] input = request.split("\\s+");
            if (input.length == 2) {
            	String lat = input[0];
                String lng = input[1];
                int cookie = rand.nextInt(1000);
                newEntry(cookie, lat, lng);
                res.println("Your cookie is: " + cookie);
            } else if (input.length == 3) {
                String lat = input[0];
                String lng = input[1];
            	int cookie = Integer.parseInt(input[2]);
                String payload = map.get(cookie)[0] + " " + map.get(cookie)[1] + " " + lat + " " + lng;
                try (Socket geoSocket = new Socket(servicehost, serviceport);
                	 Scanner out = new Scanner(geoSocket.getInputStream());	
                		) {
                    new PrintStream(geoSocket.getOutputStream(), true).println(payload);
                    response = out.nextLine();
                    res.println(response);
                }
            } 
        } catch (Exception e) {
            log.println(e);
        }

    }
}