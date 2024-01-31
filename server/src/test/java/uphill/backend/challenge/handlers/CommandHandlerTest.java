package uphill.backend.challenge.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uphill.backend.challenge.model.Graph;
import uphill.backend.challenge.model.Session;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommandHandlerTest {

    @Mock
    Session session;

    @Mock
    Graph graph;

    @Mock
    PrintWriter out;

    @InjectMocks
    CommandHandler commandHandler;

    @Test
    void handleCloserThanCommand() {
        String[] parts = {"", "", "5", "sourceNode"};
        CommandHandler.handleCloserThanCommand(session, parts, graph);
        verify(graph).findNodesCloserThan(5, "sourceNode", session);

        // Invalid action
        parts = new String[]{"UNKNOWN"};
        CommandHandler.handleShortestPathCommand(session, parts, graph);
        verify(session).send(CommandHandler.SORRY_I_DID_NOT_UNDERSTAND_THAT);
    }

    @Test
    void handleShortestPathCommand() {
        String[] parts = {"", "", "sourceNode", "destinationNode"};
        CommandHandler.handleShortestPathCommand(session, parts, graph);
        verify(graph).shortestPath("sourceNode", "destinationNode", session);

        // Invalid action
        parts = new String[]{"UNKNOWN"};
        CommandHandler.handleShortestPathCommand(session, parts, graph);
        verify(session).send(CommandHandler.SORRY_I_DID_NOT_UNDERSTAND_THAT);
    }

    @Test
    void handleRemoveCommand() {
        String[] parts = {"", "NODE", "nodeName"};
        CommandHandler.handleRemoveCommand(session, parts, graph);
        verify(graph).removeNode("nodeName", session);

        parts = new String[]{"", "EDGE", "sourceNode", "destinationNode"};
        CommandHandler.handleRemoveCommand(session, parts, graph);
        verify(graph).removeEdge("sourceNode", "destinationNode", session);

        // Invalid action
        parts = new String[]{"UNKNOWN"};
        CommandHandler.handleRemoveCommand(session, parts, graph);
        verify(session).send(CommandHandler.SORRY_I_DID_NOT_UNDERSTAND_THAT);
    }

    @Test
    void handleAddCommand() {
        String[] parts = {"", "NODE", "nodeName"};
        CommandHandler.handleAddCommand(session, parts, graph);
        verify(graph).addNode("nodeName", session);

        parts = new String[]{"", "EDGE", "sourceNode", "destinationNode", "5"};
        CommandHandler.handleAddCommand(session, parts, graph);
        verify(graph).addEdge("sourceNode", "destinationNode", 5, session);

        // Invalid action
        parts = new String[]{"UNKNOWN"};
        CommandHandler.handleAddCommand(session, parts, graph);
        verify(session).send(CommandHandler.SORRY_I_DID_NOT_UNDERSTAND_THAT);
    }

    @Test
    void handleHiCommand() {
        String[] parts = {"HI", "I", "AM", "Cesar"};
        Map<UUID, Session> sessions = new HashMap<>();
        UUID sessionId = UUID.randomUUID();
        Session session = new Session(sessionId, out, 0);
        sessions.put(sessionId, session);

        CommandHandler.handleHiCommand(session, parts, sessions);
        verify(out).println("HI Cesar");
        verifyNoMoreInteractions(out);

        // Invalid command
        parts = new String[]{"UNKNOWN"};
        CommandHandler.handleHiCommand(session, parts, sessions);
        verify(out).println(CommandHandler.SORRY_I_DID_NOT_UNDERSTAND_THAT);
        verifyNoMoreInteractions(out);
    }

    @Test
    void handleByeCommand() {
        String[] parts = {"BYE", "MATE!"};
        Map<UUID, Session> sessions = new HashMap<>();
        UUID sessionId = UUID.randomUUID();
        Session session = new Session(sessionId, out, 0);
        sessions.put(sessionId, session);
        CommandHandler.handleByeCommand(session, parts, new HashMap<>());
        verify(out).println(anyString());
        verifyNoMoreInteractions(out);

        // Invalid command
        parts = new String[]{"UNKNOWN"};
        CommandHandler.handleByeCommand(session, parts, sessions);
        verify(out).println(CommandHandler.SORRY_I_DID_NOT_UNDERSTAND_THAT);
        verifyNoMoreInteractions(out);
    }
}