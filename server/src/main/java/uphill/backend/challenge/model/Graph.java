package uphill.backend.challenge.model;

import java.util.HashMap;
import java.util.Map;

public class Graph {

    //private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private Map<String, Node> nodes = new HashMap<>();

    public void addNode(String nodeName, Session session) {
        //lock.writeLock().lock();
        if (nodes.containsKey(nodeName)) {
            session.send("ERROR: NODE ALREADY EXISTS");
        } else {
            //try {
                nodes.put(nodeName, new Node(nodeName));
                session.send("NODE ADDED");
            //} finally {
                //lock.writeLock().unlock();
            //}
        }
    }

    public void addEdge(String sourceNodeName, String destinationNodeName, int weight, Session session) {
        //lock.writeLock().lock();
        if (!nodes.containsKey(sourceNodeName) || !nodes.containsKey(destinationNodeName)) {
            session.send("ERROR: NODE NOT FOUND");
        } else {
            //try {
                Node sourceNode = nodes.get(sourceNodeName);
                Node destinationNode = nodes.get(destinationNodeName);

                sourceNode.addNeighbor(destinationNode, weight);
                destinationNode.addNeighbor(sourceNode, weight);
                session.send("EDGE ADDED");
            //} finally {
                //lock.writeLock().unlock();
            //}
        }
    }

    public void removeNode(String nodeName, Session session) {
        //lock.writeLock().lock();
        if (!nodes.containsKey(nodeName)) {
            session.send("ERROR: NODE NOT FOUND");
        } else {
            //try {
                Node node = nodes.get(nodeName);
                for (Node neighbor : node.getNeighbors()) {
                    neighbor.removeNeighbor(node);
                }
                nodes.remove(nodeName);
                session.send("NODE REMOVED");
            //} finally {
              //  lock.writeLock().unlock();
            //}
        }
    }

    public void removeEdge(String sourceNodeName, String destinationNodeName, Session session) {
        // lock.writeLock().lock();
        if (!nodes.containsKey(sourceNodeName) || !nodes.containsKey(destinationNodeName)) {
            session.send("ERROR: NODE NOT FOUND");
        } else {
            //try {
                Node sourceNode = nodes.get(sourceNodeName);
                Node destinationNode = nodes.get(destinationNodeName);
                sourceNode.removeNeighbor(destinationNode);
                destinationNode.removeNeighbor(sourceNode);
                session.send("EDGE REMOVED");
            //} finally {
            //    lock.writeLock().unlock();
            //}
        }
    }
}
