# MatrixNet Operator's Console: Technical Backend Overview

This project implements the **MatrixNet** infrastructure, a high-performance backend designed to manage clandestine host nodes and backdoor tunnels for the Resistance. Engineered for both stealth and speed, the system handles complex network topology analysis and multi-objective routing under Agent detection constraints using custom-implemented data structures.

---

## 🛠 Under the Hood: The Engineering

To maintain peak performance across massive datasets (up to 100,000 hosts and 500,000 tunnels), the system bypasses standard libraries in favor of custom-built primitives.

### 1. High-Performance Host Management (Custom Hashmap)
The foundation of MatrixNet is a custom **Hashmap** that stores `Host` objects with $O(1)$ average time complexity for lookups.
* **Hashing Algorithm**: The engine implements **Horner's Method** with a prime base of 31 to transform alphanumeric host IDs into table indices.
* **Collision Strategy**: The system utilizes **Separate Chaining** with Linked Lists to manage hash collisions.
* **Dynamic Resizing**: When the load factor exceeds 0.75, the map automatically **rehashes**, doubling its size to ensure search efficiency remains constant as the network expands.


### 2. Multi-Objective Covert Routing (Min-Heap & Dijkstra)
Tracing a route for operatives requires balancing dynamic latency, steps, and security protocols.
* **Min-Heap Optimization**: MatrixNet utilizes a custom **Min-Heap** to prioritize candidate paths during route discovery.
* **Dynamic Latency & Congestion**: The engine accounts for a **Congestion Factor ($\lambda$)**. The effective latency of each segment increases based on the number of steps already taken: 
  $$l_{eff} = l_{base} + \lambda \times steps_{taken}$$.
* **Path Comparison**: The `HeapNode` class implements a multi-level `compareTo` logic that prioritizes 1) total latency, 2) total steps, and 3) lexicographical ID sequences for tie-breaking.
* **Pruning Logic**: To maintain speed, the `shouldBeAdded` method discards paths that are strictly worse in both latency and steps than existing paths to a host.


### 3. Vulnerability & Breach Simulation
The console allows operators to simulate "What-If" scenarios to find single points of failure without permanently altering the network.
* **Connectivity Scanning**: Using a traversal-based approach, the engine identifies disconnected components within the currently unsealed network.
* **Host Breach (Articulation Points)**: The system temporarily marks a host to be skipped to determine if its failure fragments the infrastructure into more components.
* **Backdoor Breach (Bridges)**: The engine simulates sealing a specific tunnel to see if it is a critical bridge required for connectivity.

### 4. Oracle Topology Reporting
The `oracle_report` command generates a global snapshot of the Resistance network status.
* **Cycle Detection**: The system determines if the network contains closed traversal sequences (cyclic structures) by comparing the relationship between host count, unsealed backdoors, and connected components.
* **Precision Metrics**: Average bandwidth and clearance levels across all spawned hosts are calculated and rounded to exactly one decimal place using `BigDecimal` with `HALF_UP` rounding mode.

---

## 📂 Project Architecture
* **`Host.java`**: Represents access points with specific security clearances and adjacency lists.
* **`BackDoor.java`**: Stores latency, bandwidth, and firewall security for bidirectional tunnels.
* **`Neighbor.java`**: A helper class that pairs a `Host` with the specific `BackDoor` used to reach it.
* **`Graph.java`**: The core management class for adjacency lists, route finding, and connectivity algorithms.
* **`MinHeap.java`**: Efficiently manages `HeapNode` priorities for routing tasks.
* **`Hashmap.java`**: The primary storage engine for $O(1)$ host lookups.

---

## 🚦 How to Run
The engine reads command scripts and generates a chronological log of all network events.

```bash
javac *.java
java Main <input_file> <output_file>
