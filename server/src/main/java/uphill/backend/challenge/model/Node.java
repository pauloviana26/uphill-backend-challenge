package uphill.backend.challenge.model;

import java.util.ArrayList;
import java.util.List;

public class Node {

    private String nodeName;
    private List<Node> neighbors;

    public Node(String nodeName) {
        this.nodeName = nodeName;
        this.neighbors = new ArrayList<>();
//        this.neighborWeights = new HashMap<>(); // Initialize the neighbor weights map
    }

    public String getNodeName() {
        return nodeName;
    }

    public void addNeighbor(Node neighbor, int weight) {
        neighbors.add(neighbor);
        neighbor.getNeighbors().add(this); // Add the current node as a neighbor to the neighbor node as well
//        this.neighborWeights.put(neighbor, weight); // Maintain a map to store the weights of the edges
    }

    public void removeNeighbor(Node neighbor) {
        neighbors.remove(neighbor);
        neighbor.getNeighbors().remove(this);
//        this.neighborWeights.remove(neighbor);
    }

    public List<Node> getNeighbors() {
        return neighbors;
    }
}
