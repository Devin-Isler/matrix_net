import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.Locale;

public class Main {
    static Graph network = new Graph(10007); // Graph to store and manage the hosts and links
    static int totalClearance = 0;

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);

        if (args.length != 2) {
            System.err.println("Usage: java Main <input_file> <output_file>");
            System.exit(1);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(args[0]));
             BufferedWriter writer = new BufferedWriter(new FileWriter(args[1]))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                processCommand(line, writer);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processCommand(String command, BufferedWriter writer) throws IOException {
        String[] parts = command.split("\\s+");
        String operation = parts[0];
        String result = "";

        try {
            switch (operation) {
                case "spawn_host":
                    // Format: spawn_host <hostId> <clearanceLevel>
                    String id1 = parts[1];
                    int clearanceLevel = Integer.parseInt(parts[2]);
                    Host host1 = new Host(id1, clearanceLevel);

                    if (network.addHost(host1) == null) {
                        result = "Some error occurred in spawn_host.";
                    } else {
                        totalClearance += clearanceLevel;
                        result = String.format("Spawned host %s with clearance level %d.", id1, clearanceLevel);
                    }
                    break;

                case "link_backdoor":
                    // Format: link_backdoor <hostId1> <hostId2> <latency> <bandwidth> <firewall level>
                    String id2 = parts[1];
                    String id_2 = parts[2];
                    int latency2 = Integer.parseInt(parts[3]);
                    int bandwidth2 = Integer.parseInt(parts[4]);
                    int level2 = Integer.parseInt(parts[5]);

                    if (network.linkHosts(id2, id_2, latency2, bandwidth2, level2)) {
                        result = String.format("Linked %s <-> %s with latency %dms, bandwidth %dMbps, firewall %d.",
                                id2, id_2, latency2, bandwidth2, level2);
                    } else {
                        result = "Some error occurred in link_backdoor.";
                    }
                    break;

                case "seal_backdoor":
                    // Format: seal_backdoor <hostId1> <hostId2>
                    String id3 = parts[1];
                    String id_3 = parts[2];

                    Neighbor link = network.switchBackdoorSeal(id3, id_3);

                    if (link == null) {
                        result = "Some error occurred in seal_backdoor.";
                    }
                    else{
                        result = String.format("Backdoor %s <-> %s ", id3, id_3);
                        if(link.backDoor.isSealed){
                            result += "sealed.";
                        }
                        else{
                            result += "unsealed.";
                        }
                    }
                    break;

                case "trace_route":
                    // Format: trace_route <sourceId> <destId> <min_bandwidth> <lambda>
                    String srcId = parts[1];
                    String desId = parts[2];
                    int minBandwidth = Integer.parseInt(parts[3]);
                    int lambda = Integer.parseInt(parts[4]);
                    HeapNode path = network.findRoute(srcId, desId, minBandwidth, lambda);
                    if(path == null){
                        result = "Some error occurred in trace_route.";
                    }
                    else if(path.host == null){
                        result = String.format("No route found from %s to %s", srcId, desId);
                    }
                    else{
                        result = String.format("Optimal route %s -> %s: ", srcId, desId);
                        LinkedList<String> routePath = path.getPathIds();

                        result += String.join(" -> ", routePath);
                        result += String.format(" (Latency = %dms)", path.totalLatency);
                    }
                    break;

                case "scan_connectivity":
                    // Format: scan_connectivity
                    int n1 = network.scanConnectivity();
                    if (n1 == 1){
                        result = "Network is fully connected.";
                    }
                    else{
                        result = String.format("Network has %d disconnected components.", n1);
                    }
                    break;

                case "simulate_breach":
                    // Format: simulate_breach <hostId>
                    if(parts.length == 2){
                        String id4 = parts[1];
                        int n2 = network.hostBreach(id4);
                        if(n2 == -1){
                            result = "Some error occurred in simulate_breach.";
                        }
                        else if(n2 == 0){
                            result = String.format("Host %s is NOT an articulation point. Network remains the same.", id4);
                        }
                        else{
                            result = String.format("Host %s IS an articulation point.\n" +
                                    "Failure results in %d disconnected components.", id4, n2);
                        }
                   }

                    // Format: simulate_breach <hostId1> <hostId2>
                    if(parts.length == 3){
                        String id5 = parts[1];
                        String id_5 = parts[2];
                        int n3 = network.backdoorBreach(id5, id_5);
                        if(n3 == -1){
                            result = "Some error occurred in simulate_breach.";
                        }
                        else if(n3 == 0){
                            result = String.format("Backdoor %s <-> %s is NOT a bridge. Network remains the same.", id5, id_5);
                        }
                        else{
                            result = String.format("Backdoor %s <-> %s IS a bridge.\n" +
                                    "Failure results in %d disconnected components.", id5,id_5, n3);
                        }
                    }
                    break;

                case "oracle_report":
                    // Format: oracle_report
                    int totalHosts = network.hosts.size;
                    int totalUnsealedBackdoors = network.unsealedBackdoors;
                    int components = network.scanConnectivity();
                    String isConnected = (components == 1) ? "Connected" : "Disconnected";
                    String hasCycle = (totalUnsealedBackdoors > totalHosts - components) ? "Yes" : "No";
                    BigDecimal averageBandwidth = new BigDecimal(Double.toString((double) network.totalBandwidth / totalUnsealedBackdoors))
                            .setScale(1, RoundingMode.HALF_UP);
                    BigDecimal averageClearance = new BigDecimal(Double.toString((double) totalClearance / totalHosts))
                            .setScale(1, RoundingMode.HALF_UP);

                    result = "--- Resistance Network Report ---\n";
                    result += String.format("Total Hosts: %d\n", totalHosts);
                    result += String.format("Total Unsealed Backdoors: %d\n", totalUnsealedBackdoors);
                    result += String.format("Network Connectivity: %s\n", isConnected);
                    result += String.format("Connected Components: %d\n", components);
                    result += String.format("Contains Cycles: %s\n", hasCycle);
                    result += "Average Bandwidth: " + averageBandwidth + "Mbps\n";
                    result += "Average Clearance Level: " + averageClearance;
                    break;


                default:
                    result = "Unknown command: " + operation;
            }

            writer.write(result);
            writer.newLine();

        } catch (Exception e) {
            writer.write("Error processing command: " + command);
            writer.newLine();
        }
    }
}
