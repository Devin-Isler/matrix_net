import java.util.LinkedList;

// Heap class for the MinHeap
public class HeapNode {
    Host host;
    int totalLatency; // Total latency accumulated to reach this host
    int steps; // Steps that it has taken to come to this host
    HeapNode parent; // The host this node was reached from

    // Constructor
    public HeapNode(Host host, int totalLatency, int steps, HeapNode parent) {
        this.host = host;
        this.totalLatency = totalLatency;
        this.steps = steps;
        this.parent = parent;
    }

    // Getting the IDs of the hosts that are passed through to get to this host
    public LinkedList<String> getPathIds() {
        LinkedList<String> path = new LinkedList<>();
        HeapNode current = this;
        while (current != null) {
            path.addFirst(current.host.id);
            current = current.parent;
        }
        return path;
    }

    // Comparing this node to another node, according to their 1) total latency, 2) step_count, 3) path IDs elementwise
    public int compareTo(HeapNode other) {
        // Comparing their latencies
        if (this.totalLatency != other.totalLatency) {
            if(this.totalLatency > other.totalLatency){
                return 1;
            }
            return -1;
        }

        // Comparing how many steps that they have taken to come to this node
        if (this.steps != other.steps) {
            if(this.steps > other.steps){
                return 1;
            }
            return -1;
        }

        LinkedList<String> thisPath = this.getPathIds();
        LinkedList<String> otherPath = other.getPathIds();

        // Comparing elementwise IDs
        int size = thisPath.size();
        for (int i = 0; i < size; i++) {
            String id1 = thisPath.get(i);
            String id2 = otherPath.get(i);
            int cmp = id1.compareTo(id2);
            // If not the same return the result
            if (cmp != 0) {
                return cmp;
            }
        }

        return 0; // They are the same (which can not be possible actually)
    }
}
