package services;

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


public class Auth extends Thread {
    private static PrintStream log = System.out;

    private final String home = System.getProperty("user.home");
    private final String URL = "jdbc:sqlite:" + home + "/4413/pkg/sqlite/Models_R_US.db";

    private Socket client;

    private Auth(Socket client) {
        this.client = client;
    }

    public void run() {
        log.printf("Connected to %s:%d\n", client.getInetAddress(), client.getPort());

        try (Socket client = this.client; // Makes sure that client is closed at end of try-statement.
            Scanner req = new Scanner(client.getInputStream()); PrintStream res = new PrintStream(client.getOutputStream(), true);) {
            String response;
            String request = req.nextLine().trim();

            if (request.matches("^\\w+@[a-zA-Z_]+?\\.[a-zA-Z]{2,3}+( [a-zA-z0-9]{6,})$")) {
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
        String address = token[0];
        String password = token[1];
        int count = 0;
        String salt = null, hash = null;

        try (Connection connection = DriverManager.getConnection(URL)) {
            log.printf("Connected to database: %s\n", connection.getMetaData().getURL());
            String query = "SELECT * FROM client " + "WHERE name = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, address);

                try (ResultSet rs = statement.executeQuery()) {

                    if (rs.next() == false) {
                        return "FAILURE";
                    } else {
                        hash = rs.getString("hash");
                        salt = rs.getString("salt");
                        count = rs.getInt("count");
                        if (g.Util.hash(password, salt, count).equals(hash)) {
                            return "OK";
                        } else {
                            return "FAILURE";
                        }
                    }
                } catch (Exception e) {
                    log.println(e);
                    return "Hash Error: " + e.getMessage();
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
                (new Auth(client)).start();
            }
        }
    }


}