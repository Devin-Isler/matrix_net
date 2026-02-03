// Min-Heap to store the possible paths with partially-ordered way
public class MinHeap {
    int currentSize;
    HeapNode[] array;

    // Constructor
    public MinHeap(int capacity) {
        currentSize = 0;
        array = new HeapNode[capacity + 1];
    }

    // Inserting a possible path to the heap
    public void insert(HeapNode x) {
        if (currentSize == array.length - 1) {
            enlargeArray(array.length * 2 + 1);
        }

        int hole = ++currentSize;
        percolateUp(x, hole);
    }

    // Delete and return the best(minimum) path
    public HeapNode deleteMin() {
        if (isEmpty()) {
            return null;
        }

        HeapNode min = array[1];
        array[1] = array[currentSize--];
        if (currentSize > 0) {
            percolateDown(1);
        }
        return min;
    }

    // Percolating up the given freelancer to its proper place
    private void percolateUp(HeapNode x, int hole) {
        for (array[0] = x; x.compareTo(array[hole / 2]) < 0; hole /= 2) {
            array[hole] = array[hole / 2];
        }
        array[hole] = x;
    }

    // Percolating down the given node to its proper place
    private void percolateDown(int hole) {
        int child;
        HeapNode tmp = array[hole];
        for (; hole * 2 <= currentSize; hole = child) {
            child = hole * 2;
            if (child != currentSize && array[child + 1].compareTo(array[child]) < 0) {
                child++;
            }
            if (array[child].compareTo(tmp) < 0) {
                array[hole] = array[child];
            }
            else {
                break;
            }
        }
        array[hole] = tmp;
    }

    // Check if the heap is empty or not
    public boolean isEmpty() {
        return currentSize == 0;
    }

    // If the heap is full, make the heap larger
    private void enlargeArray(int newSize) {
        HeapNode[] old = array;
        array = new HeapNode[newSize];
        for (int i = 0; i < old.length; i++) {
            array[i] = old[i];
        }
    }
}
