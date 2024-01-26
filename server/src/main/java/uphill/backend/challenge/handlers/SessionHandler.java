package uphill.backend.challenge.handlers;

import uphill.backend.challenge.model.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionHandler implements Runnable {

    private final Socket clientSocket;
    private final UUID sessionId;
    private long lastActivityTime;

    private static final Map<UUID, Session> sessions = new HashMap<>();

    private static final int SESSION_TIMEOUT = 30000; // 30 seconds

    public SessionHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.sessionId = UUID.randomUUID();
        this.lastActivityTime = System.currentTimeMillis();
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
            writer.println("HI, I AM " + sessionId);

            String clientMessage;
            while ((clientMessage = reader.readLine()) != null) {
                // Reset session timeout
                lastActivityTime = System.currentTimeMillis();
                handleCommand(session, clientMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkSessionTimeout() {
        while (true) {
            if (System.currentTimeMillis() - lastActivityTime > SESSION_TIMEOUT) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            try {
                Thread.sleep(1000); // Check every second
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void handleCommand(Session session, String command) {
        String[] parts = command.split(" ");
        String keyword = parts[0].replace(",", "");

        switch (keyword) {
            case "HI":
                handleHiCommand(session, parts);
                break;
            case "BYE":
                handleByeCommand(session, parts);
                break;
            default:
                session.send("SORRY, I DID NOT UNDERSTAND THAT");
        }
    }

    private static void handleHiCommand(Session session, String[] parts) {
        if (parts.length == 4 && parts[1].equals("I") && parts[2].equals("AM")) {
            String name = parts[3];
            session.setName(name);
            session.send("HI " + name);
            sessions.remove(session.getSessionId());
        } else {
            session.send("SORRY, I DID NOT UNDERSTAND THAT");
        }
    }

    private static void handleByeCommand(Session session, String[] parts) {
        if (parts.length == 2 && parts[1].equals("MATE!")) {
            session.send("BYE " + session.getName() + ", WE SPOKE FOR " + session.getSessionDuration() + " MS");
            sessions.remove(session.getSessionId());
        } else {
            session.send("SORRY, I DID NOT UNDERSTAND THAT");
        }
    }
}
