package uphill.backend.challenge.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node {

    private String nodeName;

    private List<Node> neighbors;

    private int distance;

    private boolean visited;

    private Map<Node, Integer> neighborWeights; // Map to store the weights of the edges

    public Node(String nodeName) {
        this.nodeName = nodeName;
        this.neighbors = new ArrayList<>();
        this.distance = -1; // Initialize distance to -1 for nodes that have not been visited
        this.visited = false;
        this.neighborWeights = new HashMap<>(); // Initialize the neighbor weights map
    }

    public String getNodeName() {
        return nodeName;
    }

    public void addNeighbor(Node neighbor, int weight) {
        neighbors.add(neighbor);
        neighbor.getNeighbors().add(this); // Add the current node as a neighbor to the neighbor node as well
        this.neighborWeights.put(neighbor, weight); // Maintain a map to store the weights of the edges
    }

    public void removeNeighbor(Node neighbor) {
        neighbors.remove(neighbor);
        neighbor.getNeighbors().remove(this);
        this.neighborWeights.remove(neighbor);
    }

    public List<Node> getNeighbors() {
        return neighbors;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public int getNeighborWeight(Node neighbor) {
        return this.neighborWeights.get(neighbor);
    }
}
