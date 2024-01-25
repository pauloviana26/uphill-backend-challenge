package uphill.backend.challenge;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private static final int PORT = 12345;
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server listening on port " + PORT);
            while (true) {
                var client = serverSocket.accept();
                logger.log(Level.INFO, "Client connected: " + client.getInetAddress());
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "An IO exception occurred when waiting for a connection", e);
        }
    }
}
