package uphill.backend.challenge;

import uphill.backend.challenge.handlers.SessionHandler;
import uphill.backend.challenge.model.Graph;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started. Listening on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();

                // Start a new session for each client
                Graph graph = new Graph();
                SessionHandler sessionHandler = new SessionHandler(clientSocket, graph);
                Thread sessionThread = new Thread(sessionHandler);
                sessionThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
