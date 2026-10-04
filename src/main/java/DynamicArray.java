public class DynamicArray implements IntList {
    private int[] data;
    private int size;
    private final Metrics metrics = new Metrics();

    public DynamicArray() {
        this(10);
    }

    public DynamicArray(int capacity) {
        // Math.max: capacity 0 would make "length * 2" stay 0 and break add()
        this.data = new int[Math.max(1, capacity)];
        this.size = 0;
    }

    @Override
    public int size() {
        return size;
    }

    int capacity() {
        return data.length;
    }

    @Override
    public Metrics getMetrics() {
        return metrics;
    }

    private void ensureCapacity() {
        if (size == data.length) {
            int[] newData = new int[data.length * 2];   // grow 2x
            for (int i = 0; i < size; i++) {
                metrics.steps++;                        // read data[i]
                metrics.moves++;                        // write newData[i]
                newData[i] = data[i];
            }
            data = newData;
        }
    }

    @Override
    public void add(int x) {
        ensureCapacity();
        metrics.moves++;                                // write data[size]
        data[size++] = x;
    }

    @Override
    public void add(int index, int x) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }
        ensureCapacity();
        for (int i = size; i > index; i--) {            // shift right
            metrics.steps++;
            metrics.moves++;
            data[i] = data[i - 1];
        }
        metrics.moves++;                                // write the new element
        data[index] = x;
        size++;
    }

    @Override
    public int get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }
        metrics.steps++;
        return data[index];
    }

    @Override
    public int remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }
        metrics.steps++;                                // read removed element
        int removed = data[index];
        for (int i = index; i < size - 1; i++) {        // shift left
            metrics.steps++;
            metrics.moves++;
            data[i] = data[i + 1];
        }
        size--;
        return removed;
    }

    @Override
    public boolean contains(int x) {
        for (int i = 0; i < size; i++) {
            metrics.steps++;
            metrics.comparisons++;
            if (data[i] == x) {
                return true;
            }
        }
        return false;
    }
}
