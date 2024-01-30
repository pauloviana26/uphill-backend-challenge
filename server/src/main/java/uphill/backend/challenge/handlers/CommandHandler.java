package uphill.backend.challenge.handlers;

import uphill.backend.challenge.model.Graph;
import uphill.backend.challenge.model.Session;

import java.util.Map;
import java.util.UUID;

public class CommandHandler {

    public static final String SORRY_I_DID_NOT_UNDERSTAND_THAT = "SORRY, I DID NOT UNDERSTAND THAT";

    public static void handleCloserThanCommand(Session session, String[] parts, Graph graph) {
        if (parts.length < 3) {
            session.send(SORRY_I_DID_NOT_UNDERSTAND_THAT);
            return;
        }
        String weight = parts[2];
        String sourceNodeName = parts[3];
        graph.findNodesCloserThan(Integer.parseInt(weight), sourceNodeName, session);
    }

    public static void handleShortestPathCommand(Session session, String[] parts, Graph graph) {
        if (parts.length < 3) {
            session.send(SORRY_I_DID_NOT_UNDERSTAND_THAT);
            return;
        }
        String sourceNodeName = parts[2];
        String destinationNodeName = parts[3];
        graph.shortestPath(sourceNodeName, destinationNodeName, session);
    }

    public static void handleRemoveCommand(Session session, String[] parts, Graph graph) {
        if (parts.length < 2) {
            session.send(SORRY_I_DID_NOT_UNDERSTAND_THAT);
            return;
        }
        String action = parts[1];
        String nodeName;
        if (action.equals("NODE")) {
            nodeName = parts[2];
            graph.removeNode(nodeName, session);
        } else if (action.equals("EDGE")) {
            if (parts.length < 4) {
                session.send(SORRY_I_DID_NOT_UNDERSTAND_THAT);
                return;
            }
            String sourceNodeName = parts[2];
            String destinationNodeName = parts[3];
            graph.removeEdge(sourceNodeName, destinationNodeName, session);
        } else {
            session.send(SORRY_I_DID_NOT_UNDERSTAND_THAT);
        }
    }

    public static void handleAddCommand(Session session, String[] parts, Graph graph) {
        if (parts.length < 2) {
            session.send(SORRY_I_DID_NOT_UNDERSTAND_THAT);
            return;
        }

        String action = parts[1];
        String nodeName = parts[2];
        if (action.equals("NODE")) {
            graph.addNode(nodeName, session);
        } else if (action.equals("EDGE")) {
            if (parts.length < 4) {
                session.send(SORRY_I_DID_NOT_UNDERSTAND_THAT);
                return;
            }
            String sourceNodeName = parts[2];
            String destinationNodeName = parts[3];
            int weight = Integer.parseInt(parts[4]);
            graph.addEdge(sourceNodeName, destinationNodeName, weight, session);
        } else {
            session.send(SORRY_I_DID_NOT_UNDERSTAND_THAT);
        }
    }

    public static void handleHiCommand(Session session, String[] parts, Map<UUID, Session> sessions) {
        if (parts.length == 4 && parts[1].equals("I") && parts[2].equals("AM")) {
            String name = parts[3];
            session.setName(name);
            session.send("HI " + name);
            sessions.remove(session.getSessionId());
        } else {
            session.send(SORRY_I_DID_NOT_UNDERSTAND_THAT);
        }
    }

    public static void handleByeCommand(Session session, String[] parts, Map<UUID, Session> sessions) {
        if (parts.length == 2 && parts[1].equals("MATE!")) {
            session.send("BYE " + session.getName() + ", WE SPOKE FOR " + session.getSessionDuration() + " MS");
            sessions.remove(session.getSessionId());
        } else {
            session.send(SORRY_I_DID_NOT_UNDERSTAND_THAT);
        }
    }
}
