package uphill.backend.challenge.handlers;

import org.apache.log4j.Logger;
import uphill.backend.challenge.model.Graph;
import uphill.backend.challenge.model.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static uphill.backend.challenge.handlers.CommandHandler.*;

public class SessionHandler implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(SessionHandler.class);
    public static final String SORRY_I_DID_NOT_UNDERSTAND_THAT = "SORRY, I DID NOT UNDERSTAND THAT";
    public static final String HI_I_AM_COMMAND = "HI, I AM ";
    public static final String SOME_ERROR_OCCURRED_WHILE_CLOSING_SOCKET = "Some error occurred while closing socket -> ";

    private final Socket clientSocket;
    private final UUID sessionId;
    private long lastActivityTime;

    private static final Map<UUID, Session> sessions = new HashMap<>();

    private final Graph graph;

    private static final int SESSION_TIMEOUT = 30000; // 30 seconds

    public SessionHandler(Socket clientSocket, Graph graph) {
        this.clientSocket = clientSocket;
        this.sessionId = UUID.randomUUID();
        this.lastActivityTime = System.currentTimeMillis();
        this.graph = graph;
    }

    @Override
    public void run() {
        // Start the session timeout checker thread
        Thread timeoutThread = new Thread(this::checkSessionTimeout);
        timeoutThread.start();
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            Session session = new Session(sessionId, writer, lastActivityTime);
            sessions.put(sessionId, session);
            writer.println(HI_I_AM_COMMAND + sessionId);

            String clientMessage;
            while ((clientMessage = reader.readLine()) != null) {
                lastActivityTime = System.currentTimeMillis();
                handleCommand(session, clientMessage);
            }
        } catch (IOException e) {
            LOGGER.info("Client disconnected (" + e.getMessage() + ")");
        } finally {
            try {
                clientSocket.close();
                LOGGER.info("Client disconnected");
            } catch (IOException e) {
                LOGGER.error(SOME_ERROR_OCCURRED_WHILE_CLOSING_SOCKET + e.getMessage());
            }
        }
    }

    private void handleCommand(Session session, String command) {
        String[] parts = command.split(" ");
        String keyword = parts[0].replace(",", "");

        switch (keyword) {
            case "HI":
                handleHiCommand(session, parts, sessions);
                break;
            case "BYE":
                handleByeCommand(session, parts, sessions);
                break;
            case "ADD":
                handleAddCommand(session, parts, graph);
                break;
            case "REMOVE":
                handleRemoveCommand(session, parts, graph);
                break;
            case "SHORTEST":
                handleShortestPathCommand(session, parts, graph);
                break;
            case "CLOSER":
                handleCloserThanCommand(session, parts, graph);
                break;
            default:
                session.send(SORRY_I_DID_NOT_UNDERSTAND_THAT);
        }
    }

    private void checkSessionTimeout() {
        while (true) {
            if (System.currentTimeMillis() - lastActivityTime > SESSION_TIMEOUT) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    LOGGER.error(SOME_ERROR_OCCURRED_WHILE_CLOSING_SOCKET + e.getMessage());
                }
                break;
            }
            try {
                Thread.sleep(1000); // Check every second
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }
}
