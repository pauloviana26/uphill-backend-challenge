package uphill.backend.challenge;

import uphill.backend.challenge.handlers.SessionHandler;
import uphill.backend.challenge.model.Graph;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 12345;
    private static final int MAX_THREADS = 100;

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started. Listening on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();

                // Start a new session for each client
                Graph graph = new Graph();
                SessionHandler sessionHandler = new SessionHandler(clientSocket, graph);
                executorService.submit(sessionHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
    }
}
