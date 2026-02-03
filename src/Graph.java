// Graph class to represent the network using an adjacency list(via the Neighbor class and Hashmap)
public class Graph {
    Hashmap hosts;
    int unsealedBackdoors;
    int totalBandwidth;
    int distinctComponents;
    boolean isChanged;

    // Constructor
    public Graph(int initialCapacity) {
        this.hosts = new Hashmap(initialCapacity);
        this.unsealedBackdoors = 0;
        this.totalBandwidth = 0;
        this.distinctComponents = 1;
        this.isChanged = false;
    }

    // Adding a host to the graph
    public Host addHost(Host host) {
        if (!isValidString(host.id)) {
            return null;
        }
        this.isChanged = true;
        return hosts.insert(host);
    }

    // Getting the host, if not exist null
    public Host getHost(String id) {
        return hosts.search(id);
    }

    // Linking the hosts with given attributes, if the hosts exists, and they have not been linked already
    public boolean linkHosts(String id1, String id2, int latency, int bandwidth, int firewallLevel) {
        Host host1 = getHost(id1);
        Host host2 = getHost(id2);

        if (host1 == null || host2 == null || id1.equals(id2) || findNeighbor(host1, id2) != null) {
            return false;
        }

        BackDoor backdoor = new BackDoor(latency, bandwidth, firewallLevel);
        unsealedBackdoors += 1;
        totalBandwidth += bandwidth;

        host1.neighbors.add(new Neighbor(host2, backdoor));
        host2.neighbors.add(new Neighbor(host1, backdoor));
        this.isChanged = true;

        return true;
    }

    // Sealing an unsealing link or vice versa, if the link exists
    public Neighbor switchBackdoorSeal(String id1, String id2) {
        Host host1 = getHost(id1);
        Host host2 = getHost(id2);

        if (host1 == null || host2 == null || id1.equals(id2)) {
            return null;
        }

        Neighbor link = findNeighbor(host1, id2);

        if (link != null) {
            if(link.backDoor.isSealed){
                link.backDoor.isSealed = false;
                unsealedBackdoors ++;
                totalBandwidth += link.backDoor.bandwidth;
            }
            else{
                link.backDoor.isSealed = true;
                unsealedBackdoors --;
                totalBandwidth -= link.backDoor.bandwidth;
            }
            this.isChanged = true;
            return link;
        }
        return null;
    }

    // Find the optimal route from source Host to the destination Host, just using valid backdoors
    public HeapNode findRoute(String srcId, String destId, int minBandwidth, int lambda) {
        Host src = getHost(srcId);
        Host dest = getHost(destId);
        if (src == null || dest == null) {
            return null;
        }
        MinHeap heap = new MinHeap(1007);
        resetPaths(); // Resetting every hosts possible paths

        src.paths.offer(new int[]{0, 0}); // Adding the initial path (which is 0,0) to the source
        heap.insert(new HeapNode(src, 0, 0, null)); // Adding the node host to the head
        while (!heap.isEmpty()) {
            HeapNode currentNode = heap.deleteMin();
            Host currentHost = hosts.search(currentNode.host.id);

            // If reached, return to node, which has totalLatency and parent information to find the whole path
            if (currentHost.id.equals(destId)) {
                return currentNode;
            }

            for (Neighbor neighbor : currentHost.neighbors) {
                Host neighborHost = neighbor.host;
                BackDoor currentBackDoor = neighbor.backDoor;
                // Checking if the link is valid to pass through
                if (currentBackDoor.isSealed ||
                        currentHost.clearanceLevel < currentBackDoor.firewall_level ||
                        currentBackDoor.bandwidth < minBandwidth) {
                    continue;
                }
                // Calculating the step and latency
                int totalLatency = currentNode.totalLatency + currentBackDoor.latency + lambda * currentNode.steps;
                int newStep = currentNode.steps + 1;
                // Checking whether this path is viable
                if (shouldBeAdded(neighborHost, totalLatency, newStep)) {
                    neighborHost.paths.offer(new int[]{totalLatency, newStep});
                    heap.insert(new HeapNode(neighborHost, totalLatency, newStep, currentNode));
                }
            }
        }
        // If found none, return empty node
        return new HeapNode(null, -1, -1, null);
    }

    // Scanning if the graph is connected or not
    public int scanConnectivity(){
        int counter = 0;
        if(!isChanged){
            return distinctComponents;
        }
        MinHeap heap = new MinHeap(1007);
        resetIsVisited();
        for(int i = 0; i < hosts.tableSize; i++){
            if (hosts.table[i] == null) {
                continue;
            }
            for(Host startingHost: hosts.table[i]){
                // Checking if the host is already visited, or the host is marked
                if(startingHost == null || startingHost.isVisited || startingHost.willBeSkipped){
                    continue;
                }
                counter += 1; // Counting the disconnected components
                startingHost.isVisited = true;
                heap.insert(new HeapNode(startingHost, 0, 0,null));
                while(!heap.isEmpty()){
                    HeapNode minHost = heap.deleteMin();
                    // Adding all the neighbors
                    for(Neighbor neighbor: minHost.host.neighbors){
                        Host neighborHost = neighbor.host;
                        // Checking if the link or the neighbor host valid or not
                        if(neighborHost.willBeSkipped || neighborHost.isVisited || neighbor.backDoor.isSealed){
                            continue;
                        }
                        neighborHost.isVisited = true;
                        heap.insert(new HeapNode(neighborHost, 0, 0, null));
                    }
                }
            }
        }
        distinctComponents = counter;
        return counter;
    }

    // Scanning connectivity with temporarily removing a single host
    public int hostBreach(String id){
        if(hosts.size == 0 || hosts.size == 1){
            return 0;
        }
        Host host = hosts.search(id);
        if(host == null){
            return -1;
        }
        int initialComponents = scanConnectivity();
        host.willBeSkipped = true;
        int newComponents = scanConnectivity();
        host.willBeSkipped = false;
        if(initialComponents < newComponents){
            return newComponents;
        }
        return 0;
    }

    // Scanning connectivity with temporarily removing a link
    public int backdoorBreach(String id1, String id2){
        Host host1 = hosts.search(id1);
        Host host2 = hosts.search(id2);

        if(host1 == null || host2 == null || id1.equals(id2)){
            return -1;
        }
        Neighbor neighbor = findNeighbor(host1, id2);
        if(neighbor == null || neighbor.backDoor.isSealed){
            return -1;
        }
        int initialComponents = scanConnectivity();
        neighbor.backDoor.isSealed = true;
        int newComponents = scanConnectivity();
        neighbor.backDoor.isSealed = false;
        if(initialComponents < newComponents){
            return newComponents;
        }
        return 0;
    }

    // Checking if the id is valid
    private boolean isValidString(String str) {
        if (str == null || str.isEmpty())
            return false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!((c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_')) {
                return false;
            }
        }
        return true;
    }

    // Search for the neighbor; if not found, null
    private Neighbor findNeighbor(Host host, String id) {
        for (Neighbor neighbor : host.neighbors) {
            if (neighbor.host.id.equals(id)) {
                return neighbor;
            }
        }
        return null;
    }

    // Going over all the host and clean their possible paths that is there from previous find_route
    private void resetPaths() {
        for (int i = 0; i < hosts.tableSize; i++) {
            if (hosts.table[i] != null) {
                for (Host host : hosts.table[i]) {
                    host.paths.clear();
                }
            }
        }
    }

    // Going over all the host and mark them as not visited to make them ready for new scanning
    private void resetIsVisited(){
        for (int i = 0; i < hosts.tableSize; i++) {
            if (hosts.table[i] != null) {
                for (Host host : hosts.table[i]) {
                    host.isVisited = false;
                }
            }
        }
    }

    // Checking if this path is better than any of the other path of the host
    private boolean shouldBeAdded(Host host, int newLatency, int newStep){
        for(int[] efficientState: host.paths){
            // If there is a possible path better than this path in both cases, this path shouldn't be added
            if(efficientState[0] <= newLatency && efficientState[1] <= newStep){
                return false;
            }
        }
        return true;
    }
}
