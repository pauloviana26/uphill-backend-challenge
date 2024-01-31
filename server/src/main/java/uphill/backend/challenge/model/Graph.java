package uphill.backend.challenge.model;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class Graph {

    public static final String ERROR_NODE_ALREADY_EXISTS = "ERROR: NODE ALREADY EXISTS";
    public static final String NODE_ADDED = "NODE ADDED";
    public static final String ERROR_NODE_NOT_FOUND = "ERROR: NODE NOT FOUND";
    public static final String EDGE_ADDED = "EDGE ADDED";
    public static final String NODE_REMOVED = "NODE REMOVED";
    public static final String EDGE_REMOVED = "EDGE REMOVED";
    public static final String NODE_DELIMITER = ",";
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Set<String> nodes = new HashSet<>();

    private final Map<String, Set<Edge>> edges = new HashMap<>();

    public Graph() {
    }

    public void addNode(String nodeName, Session session) {
        lock.writeLock().lock();
        try {
            if (nodes.contains(nodeName)) {
                session.send(ERROR_NODE_ALREADY_EXISTS);
                return;
            }
            nodes.add(nodeName);
            session.send(NODE_ADDED);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addEdge(String sourceNodeName, String destinationNodeName, int weight, Session session) {
        lock.writeLock().lock();
        try {
            if (!nodes.contains(sourceNodeName) || !nodes.contains(destinationNodeName)) {
                session.send(ERROR_NODE_NOT_FOUND);
                return;
            }
            Edge edge = new Edge(sourceNodeName, destinationNodeName, weight);
            Set<Edge> toEdges = edges.computeIfAbsent(sourceNodeName, k -> new HashSet<>());
            toEdges.add(edge);
            session.send(EDGE_ADDED);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeNode(String nodeName, Session session) {
        lock.writeLock().lock();
        try {
            if (!nodes.contains(nodeName)) {
                session.send(ERROR_NODE_NOT_FOUND);
                return;
            }
            boolean removed = nodes.remove(nodeName);
            if (removed) {
                edges.remove(nodeName);
            }
            session.send(NODE_REMOVED);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeEdge(String sourceNodeName, String destinationNodeName, Session session) {
        lock.writeLock().lock();
        try {
            if (!nodes.contains(sourceNodeName) || !nodes.contains(destinationNodeName)) {
                session.send(ERROR_NODE_NOT_FOUND);
                return;
            }
            Set<Edge> fromEdges = edges.get(sourceNodeName);
            if (fromEdges != null) {
                Set<Edge> toRemove = new HashSet<>();

                for (Edge edge : fromEdges) {
                    if (edge.getTo().equals(destinationNodeName)) {
                        toRemove.add(edge);
                    }
                }

                fromEdges.removeAll(toRemove);
                session.send(EDGE_REMOVED);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void shortestPath(String sourceNodeName, String destinationNodeName, Session session) {
        lock.writeLock().lock();
        int weight;
        try {
            if (!nodes.contains(sourceNodeName) || !nodes.contains(destinationNodeName)) {
                session.send(ERROR_NODE_NOT_FOUND);
                return;
            }

            if (sourceNodeName.equals(destinationNodeName)) {
                weight = 0;
                session.send(String.valueOf(weight));
                return;
            }

            weight = calculateWeightsInGraph(sourceNodeName, destinationNodeName, (node, weightedNodes) ->
                    !node.equals(destinationNodeName)).get(destinationNodeName);
        } finally {
            lock.writeLock().unlock();
        }

        session.send(String.valueOf(weight));
    }

    private Map<String, Integer> calculateWeightsInGraph(String sourceNodeName, String destinationNodeName, StopCalculationCheck check) {
        Map<String, Integer> weightedNodes = new HashMap<>();
        Set<String> unvisited = new HashSet<>(nodes);

        for (String node : unvisited) {
            weightedNodes.put(node, Integer.MAX_VALUE);
        }

        weightedNodes.put(sourceNodeName, 0);
        visitNode(sourceNodeName, unvisited, weightedNodes);

        String nodeToVisit;
        do {
            nodeToVisit = findNextNodeToVisit(unvisited, weightedNodes);
            if (nodeToVisit == null) {
                break;
            }

            visitNode(nodeToVisit, unvisited, weightedNodes);
        } while (check.shouldContinue(nodeToVisit, weightedNodes));

        return weightedNodes;
    }

    private String findNextNodeToVisit(Set<String> unvisited, Map<String, Integer> weightedNodes) {
        String smallest = null;
        int smallestWeight = Integer.MAX_VALUE;
        for (String toCheck : unvisited) {
            if (weightedNodes.get(toCheck) < smallestWeight) {
                smallestWeight = weightedNodes.get(toCheck);
                smallest = toCheck;
            }
        }
        return smallest;
    }

    private void visitNode(String currentNode, Set<String> unvisited, Map<String, Integer> weightedNodes) {
        int currentNodeWeight = weightedNodes.get(currentNode);
        Set<Edge> outgoingEdges = edges.get(currentNode);
        if (outgoingEdges != null) {
            for (Edge edge : outgoingEdges) {
                String targetNode = edge.getTo();
                if (unvisited.contains(targetNode)) {
                    int potentialNewWeight = currentNodeWeight + edge.getWeight();
                    if (potentialNewWeight < weightedNodes.get(targetNode)) {
                        weightedNodes.put(targetNode, potentialNewWeight);
                    }
                }
            }
        }
        unvisited.remove(currentNode);
    }

    public void findNodesCloserThan(int weight, String sourceNodeName, Session session) {
        this.lock.readLock().lock();
        List<String> nodesCloserThan;
        try {
            if (!nodes.contains(sourceNodeName)) {
                session.send(ERROR_NODE_NOT_FOUND);
                return;
            }
            Map<String, Integer> weightedGraph = calculateWeightsInGraph(sourceNodeName, UUID.randomUUID().toString(), (node, weightedNodes) ->
                    weightedNodes.get(node) < weight);
            nodesCloserThan = weightedGraph.entrySet().stream().filter(entry ->
                            entry.getValue() < weight).filter(entry ->
                            !(entry.getKey()).equals(sourceNodeName))
                    .map(Map.Entry::getKey)
                    .sorted(Comparator.naturalOrder())
                    .collect(Collectors.toList());
        } finally {
            this.lock.readLock().unlock();
        }
        session.send(String.join(NODE_DELIMITER, nodesCloserThan));
    }

    interface StopCalculationCheck {
        boolean shouldContinue(String node, Map<String, Integer> weightedNode);
    }
}
