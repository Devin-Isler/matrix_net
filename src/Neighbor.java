// Neighbor class to store both the neighbor host information and the link information
public class Neighbor {
    Host host;
    BackDoor backDoor;

    // Constructor
    Neighbor(Host host, BackDoor backdoor){
        this.host = host;
        this.backDoor = backdoor;
    }
}
