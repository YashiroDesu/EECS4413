package services;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement; // import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.Scanner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.google.gson.Gson;

import services.model.ProductBean;

public class Quote extends Thread {
    private static PrintStream log = System.out;

    private final String URL = "jdbc:derby://localhost:64413/EECS";

    private Socket client;

    private Quote(Socket client) {
        this.client = client;
    }

    public void run() {
        log.printf("Connected to %s:%d\n", client.getInetAddress(), client.getPort());

        try (Socket client = this.client; // Makes sure that client is closed at end of try-statement.
            Scanner req = new Scanner(client.getInputStream()); PrintStream res = new PrintStream(client.getOutputStream(), true);) {
            String response;
            String request = req.nextLine().trim();

            if (request.matches("^([S0-9_0-9])+ (json|xml)$")) {
                response = doRequest(request);
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

    private String doRequest(String request) {
        String[] token = request.split("\\s+");
        String id = token[0];
        String format = token[1];
        Object responseObject;

        try (Connection connection = DriverManager.getConnection(URL)) {
            log.printf("Connected to database: %s\n", connection.getMetaData().getURL());
            String query = "SELECT * FROM hr.Product " + "WHERE id = ? ";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, id);

                try (ResultSet rs = statement.executeQuery()) {
                    ProductBean bean = new ProductBean();
                    if (rs.next() == false) {
                        bean.setID(("ID NOT FOUND"));
                        bean.setName("");
                        bean.setPrice(0.0);
                        responseObject = bean;
                    } else {
                        bean.setID(rs.getString("id"));
                        bean.setName(rs.getString("name"));
                        bean.setPrice(rs.getDouble("cost"));
                        responseObject = bean;
                    }

                    if (format.equals("xml")) {
                        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                            JAXBContext context = JAXBContext.newInstance(ProductBean.class);
                            Marshaller m = context.createMarshaller();
                            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                            m.marshal(responseObject, baos);
                            log.println(baos);
                            return baos.toString();
                        } catch (Exception e) {
                            log.println(e);
                            return "XML Error: " + e.getMessage();
                        }
                    } else if (format.equals("json")) {
                        return (new Gson()).toJson(responseObject);
                    } else {
                        return "Unrecognized format: " + format;
                    }
                }
            }
        } catch (SQLException e) {
            log.println(e);
            return "SQL Error: " + e.getMessage();
        } finally {
            log.println("Disconnected from database.");
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 0;
        InetAddress host = InetAddress.getLocalHost(); // .getLoopbackAddress();
        try (ServerSocket server = new ServerSocket(port, 0, host)) {
            log.printf("Server listening on %s:%d\n", server.getInetAddress(), server.getLocalPort());
            while (true) {
                Socket client = server.accept();
                (new Quote(client)).start();
            }
        }
    }

}