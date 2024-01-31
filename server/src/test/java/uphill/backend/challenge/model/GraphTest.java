package uphill.backend.challenge.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GraphTest {

    @Mock
    Graph graph;

    @Mock
    Session session;

    @BeforeEach
    void setUp() {
        graph = new Graph();
        session = Mockito.mock(Session.class);
    }

    @Test
    void addNode() {
        graph.addNode("A", session);
        verify(session).send(Graph.NODE_ADDED);

        // Add node that already exists
        graph.addNode("A", session);
        verify(session).send(Graph.ERROR_NODE_ALREADY_EXISTS);
    }

    @Test
    void addEdge() {
        graph.addNode("A", session);
        graph.addNode("B", session);
        graph.addEdge("A", "B", 5, session);
        verify(session).send(Graph.EDGE_ADDED);

        // Add edge with nonexistent node
        graph.addNode("A", session);
        graph.addEdge("A", "C", 5, session);
        verify(session).send(Graph.ERROR_NODE_NOT_FOUND);
    }

    @Test
    void removeNode() {
        graph.addNode("A", session);
        graph.removeNode("A", session);
        verify(session).send(Graph.NODE_REMOVED);

        // Remove nonexistent node
        graph.removeNode("B", session);
        verify(session).send(Graph.ERROR_NODE_NOT_FOUND);
    }

    @Test
    void removeEdge() {
        graph.addNode("A", session);
        graph.addNode("B", session);
        graph.addEdge("A", "B", 5, session);
        graph.removeEdge("A", "B", session);
        verify(session).send(Graph.EDGE_REMOVED);

        // Remove edge with nonexistent node
        graph.addNode("A", session);
        graph.removeEdge("A", "C", session);
        verify(session).send(Graph.ERROR_NODE_NOT_FOUND);
    }

    @Test
    void shortestPath() {
        graph.addNode("A", session);
        graph.addNode("B", session);
        graph.addEdge("A", "B", 5, session);
        graph.shortestPath("A", "B", session);
        verify(session).send("5");

        // Same node
        graph.addNode("A", session);
        graph.shortestPath("A", "A", session);
        verify(session).send("0");

        // Nonexistent node
        graph.shortestPath("C", "D", session);
        verify(session).send(Graph.ERROR_NODE_NOT_FOUND);
    }

    @Test
    void findNodesCloserThan() {
        graph.addNode("A", session);
        graph.addNode("B", session);
        graph.addEdge("A", "B", 5, session);
        graph.findNodesCloserThan(10, "A", session);
        verify(session).send("B");

        // Nonexistent node
        graph.findNodesCloserThan(10, "C", session);
        verify(session).send(Graph.ERROR_NODE_NOT_FOUND);
    }
}