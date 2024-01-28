package uphill.backend.challenge.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class Graph {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private Set<String> nodes = new HashSet<>();

    private Map<String, Set<Edge>> edges = new ConcurrentHashMap<>();

    public Graph() {
    }

    public int size() {
        return nodes.size();
    }

    public List<String> getNodes() {
        return new ArrayList<>(nodes);
    }

    public void addNode(String nodeName, Session session) {
        lock.writeLock().lock();
        try {
            if (nodes.contains(nodeName)) {
                session.send("ERROR: NODE ALREADY EXISTS");
                return;
            }
            nodes.add(nodeName);
            session.send("NODE ADDED");
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void addEdge(String sourceNodeName, String destinationNodeName, int weight, Session session) {
        lock.writeLock().lock();
        try {
            if (!nodes.contains(sourceNodeName) || !nodes.contains(destinationNodeName)) {
                session.send("ERROR: NODE NOT FOUND");
                return;
            }
            Edge edge = new Edge(sourceNodeName, destinationNodeName, weight);
            Set<Edge> toEdges = edges.computeIfAbsent(sourceNodeName, k -> new HashSet<>());
            toEdges.add(edge);
            session.send("EDGE ADDED");
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeNode(String nodeName, Session session) {
        lock.writeLock().lock();
        try {
            if (!nodes.contains(nodeName)) {
                session.send("ERROR: NODE NOT FOUND");
                return;
            }
            boolean removed = nodes.remove(nodeName);
            if (removed) {
                edges.remove(nodeName);
            }
            session.send("NODE REMOVED");
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeEdge(String sourceNodeName, String destinationNodeName, Session session) {
        lock.writeLock().lock();
        try {
            if (!nodes.contains(sourceNodeName) || !nodes.contains(destinationNodeName)) {
                session.send("ERROR: NODE NOT FOUND");
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
                session.send("EDGE REMOVED");
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
                session.send("ERROR: NODE NOT FOUND");
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

    private Map<String, Integer> calculateWeightsInGraph(String sourceNodeName, String destinationNodeName, CalculationStopCheck check) {
        Map<String, Integer> weightedNodes = new HashMap<>();
        Set<String> unvisited = new HashSet<>(nodes);
        Iterator<String> unvisitedIterator = unvisited.iterator();

        while (unvisitedIterator.hasNext()) {
            String node = unvisitedIterator.next();
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
        Iterator<String> unvisitedIterator = unvisited.iterator();

        while (unvisitedIterator.hasNext()) {
            String toCheck = unvisitedIterator.next();
            if (weightedNodes.get(toCheck) < smallestWeight) {
                smallestWeight = weightedNodes.get(toCheck);
                smallest = toCheck;
            }
        }
        return smallest;
    }

    private void visitNode(String currentNode, Set<String> unvisited, Map<String, Integer> weightedNodes) {
        Set<String> neighbours = new HashSet<>();
        int currentNodeWeight = weightedNodes.get(currentNode);
        Set<Edge> outgoingEdges = edges.get(currentNode);

        if (outgoingEdges != null) {
            Iterator<Edge> outgoingEdgesIterator = outgoingEdges.iterator();

            while (outgoingEdgesIterator.hasNext()) {
                Edge edge = outgoingEdgesIterator.next();
                String targetNode = edge.getTo();
                if (unvisited.contains(targetNode)) {
                    neighbours.add(targetNode);
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
        List<String> nodes;
        try {
            Map<String, Integer> weightedGraph = calculateWeightsInGraph(sourceNodeName, UUID.randomUUID().toString(), (node, weightedNodes) ->
                    weightedNodes.get(node) < weight);
            nodes = weightedGraph.entrySet().stream().filter(entry ->
                            entry.getValue() < weight).filter(entry ->
                            !(entry.getKey()).equals(sourceNodeName))
                    .map(Map.Entry::getKey)
                    .sorted(Comparator.naturalOrder())
                    .collect(Collectors.toList());
        } finally {
            this.lock.readLock().unlock();
        }
        session.send(String.join(",", nodes));
    }

    interface CalculationStopCheck {
        boolean shouldContinue(String var1, Map<String, Integer> var2);
    }
}
