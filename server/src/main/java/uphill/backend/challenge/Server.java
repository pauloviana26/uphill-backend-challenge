package uphill.backend.challenge;

import org.apache.log4j.Logger;
import uphill.backend.challenge.handlers.SessionHandler;
import uphill.backend.challenge.model.Graph;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static final Logger LOGGER = Logger.getLogger(Server.class);
    private static final int PORT = 12345;
    private static final int MAX_THREADS = 100;
    public static final String SERVER_STARTED = "Server started. Listening on port ";

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);
        Graph graph = new Graph();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            LOGGER.info(SERVER_STARTED + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // Start a new session for each client
                SessionHandler sessionHandler = new SessionHandler(clientSocket, graph);
                executorService.submit(sessionHandler);
            }
        } catch (IOException e) {
            LOGGER.error("Some error occurred while connecting server and client -> " + e.getMessage());
        } finally {
            executorService.shutdown();
        }
    }
}
