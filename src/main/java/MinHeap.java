public class MinHeap {
    private int[] heap;
    private int size;
    private final Metrics metrics = new Metrics();

    public MinHeap() {
        this(10);
    }

    public MinHeap(int capacity) {
        this.heap = new int[Math.max(1, capacity)];
        this.size = 0;
    }

    public int size() {
        return size;
    }

    int capacity() {
        return heap.length;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    private void ensureCapacity() {
        if (size == heap.length) {
            int[] newHeap = new int[heap.length * 2];
            for (int i = 0; i < size; i++) {
                metrics.steps++;
                metrics.moves++;
                newHeap[i] = heap[i];
            }
            heap = newHeap;
        }
    }

    public void insert(int x) {
        ensureCapacity();
        metrics.moves++;                                // write heap[size]
        heap[size] = x;
        size++;
        bubbleUp(size - 1);
    }

    private void bubbleUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            metrics.steps += 2;                         // read heap[index], heap[parent]
            metrics.comparisons++;
            if (heap[index] < heap[parent]) {
                swap(index, parent);
                index = parent;
            } else {
                break;
            }
        }
    }

    public int peekMin() {
        if (size == 0) {
            throw new IllegalStateException("Heap is empty");
        }
        metrics.steps++;
        return heap[0];
    }

    public int extractMin() {
        if (size == 0) {
            throw new IllegalStateException("Heap is empty");
        }
        metrics.steps++;                                // read heap[0]
        int min = heap[0];
        metrics.steps++;                                // read last element
        metrics.moves++;                                // write heap[0]
        heap[0] = heap[size - 1];
        size--;
        if (size > 0) {
            bubbleDown(0);
        }
        return min;
    }

    private void bubbleDown(int index) {
        while (true) {
            int left = 2 * index + 1;
            int right = 2 * index + 2;
            int smallest = index;

            if (left < size) {
                metrics.steps += 2;
                metrics.comparisons++;
                if (heap[left] < heap[smallest]) {
                    smallest = left;
                }
            }
            if (right < size) {
                metrics.steps += 2;
                metrics.comparisons++;
                if (heap[right] < heap[smallest]) {
                    smallest = right;
                }
            }
            if (smallest != index) {
                swap(index, smallest);
                index = smallest;
            } else {
                break;
            }
        }
    }

    private void swap(int i, int j) {
        metrics.steps += 2;                             // read both cells
        metrics.moves += 2;                             // write both cells
        int temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }

    /** Floyd's bottom-up buildHeap, O(n). Replaces the current contents. */
    public void buildHeap(int[] array) {
        this.heap = new int[Math.max(1, array.length)];
        this.size = array.length;
        for (int i = 0; i < array.length; i++) {
            metrics.steps++;
            metrics.moves++;
            heap[i] = array[i];
        }
        for (int i = (size / 2) - 1; i >= 0; i--) {
            bubbleDown(i);
        }
    }

    /** Checks heap[parent] <= heap[child] for every node (used by tests). */
    boolean isValidHeap() {
        for (int i = 1; i < size; i++) {
            if (heap[(i - 1) / 2] > heap[i]) {
                return false;
            }
        }
        return true;
    }
}
