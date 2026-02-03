import java.util.LinkedList;

// Hashmap class to store the hosts, which is a hash table with linked lists to handle collisions(Separate Chaining)
public class Hashmap {
    LinkedList<Host>[] table;
    int size;
    int tableSize;

    // Constructor
    public Hashmap(int size){
        this.tableSize = size;
        this.table = (LinkedList<Host>[]) new LinkedList[tableSize];
        this.size = 0;
    }

    // Hashes the id of the user to get the index of the table, using the horner's method
    private int hashFunction(String id){
        int hash = 0;
        int B = 31;
        int len = id.length();
        for(int i = 0; i < len; i++){
            hash = id.charAt(i) + (B * hash);
        }
        return ((hash % tableSize) + tableSize) % tableSize;
    }

    // Inserts a user into the hashmap, returns null if already exists
    public Host insert(Host host){
        // Check load factor and expand if needed
        if ((double)size / tableSize > 0.75) {
            rehash();
        }

        int index = hashFunction(host.id);
        if (table[index] == null){
            table[index] = new LinkedList<>();
        }

        // No duplicate IDs
        for (Host h : table[index]) {
            if (h.id.equals(host.id)) {
                return null;
            }
        }

        table[index].add(host);
        size++;
        return host;
    }

    // Searches for a user in the hashmap
    public Host search(String id){
        if(id == null){
            return null;
        }
        int index = hashFunction(id);
        if(table[index] == null){
            return null;
        }

        for (Host h : table[index]) {
            if (h.id.equals(id)) {
                return h;
            }
        }
        return null;
    }

    // Rehashes the hashmap to a larger size
    private void rehash(){
        LinkedList<Host>[] oldTable = table;
        int oldTableSize = tableSize;

        tableSize = oldTableSize * 2 + 1;
        this.table = (LinkedList<Host>[]) new LinkedList[tableSize];
        size = 0;

        for(int i = 0; i < oldTableSize; i++){
            if(oldTable[i] != null){
                for (Host h : oldTable[i]) {
                    this.insert(h);
                }
            }
        }
    }
}
