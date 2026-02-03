import java.util.LinkedList;

// Host class
public class Host {
    String id;
    int clearanceLevel;
    // The different possible paths that you can get to this host demonstrated as (latency, step)
    LinkedList<int[]> paths;
    // Store the other hosts that are linked to the host, also has information about the link itself
    LinkedList<Neighbor> neighbors;
    boolean isVisited;
    boolean willBeSkipped;

    // Constructor
    Host(String id, int clearanceLevel) {
        this.id = id;
        this.clearanceLevel = clearanceLevel;
        this.neighbors = new LinkedList<>();
        this.paths = new LinkedList<>();
        this.isVisited = false;
        this.willBeSkipped = false;
    }
}
