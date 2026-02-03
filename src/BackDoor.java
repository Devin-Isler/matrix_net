// Backdoor class storing necessary information about the paths between the hosts
public class BackDoor {
    int latency;
    int bandwidth;
    int firewall_level;
    boolean isSealed;

    // Constructor
     BackDoor(int latency, int bandwidth, int firewall_level){
        this.latency = latency;
        this.bandwidth = bandwidth;
        this.firewall_level = firewall_level;
        this.isSealed = false;
    }
}
