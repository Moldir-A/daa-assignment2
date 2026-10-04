/** Common interface so ONE benchmark can run both DynamicArray and MyLinkedList. */
public interface IntList {
    int size();
    void add(int x);
    void add(int index, int x);
    int get(int index);
    int remove(int index);
    boolean contains(int x);
    Metrics getMetrics();
}
