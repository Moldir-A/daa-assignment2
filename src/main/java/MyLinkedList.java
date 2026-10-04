/**
 * Singly linked list with head and tail pointers.
 * Counting rule: creating a node is NOT a move; every pointer assignment
 * (head, tail, node.next) that changes the structure counts as 1 move.
 */
public class MyLinkedList implements IntList {
    private static class Node {
        int val;
        Node next;

        Node(int val) {
            this.val = val;
        }
    }

    private Node head;
    private Node tail;
    private int size;
    private final Metrics metrics = new Metrics();

    @Override
    public int size() {
        return size;
    }

    @Override
    public Metrics getMetrics() {
        return metrics;
    }

    @Override
    public void add(int x) {
        Node newNode = new Node(x);
        if (head == null) {
            head = newNode;
            tail = newNode;
            metrics.moves += 2;                         // head, tail
        } else {
            tail.next = newNode;
            tail = newNode;
            metrics.moves += 2;                         // old tail.next, tail
        }
        size++;
    }

    @Override
    public void add(int index, int x) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }
        if (index == size) {
            add(x);
            return;
        }
        Node newNode = new Node(x);
        if (index == 0) {
            newNode.next = head;
            head = newNode;
            metrics.moves += 2;                         // newNode.next, head
        } else {
            Node curr = head;
            for (int i = 0; i < index - 1; i++) {
                metrics.steps++;                        // move to next node
                curr = curr.next;
            }
            newNode.next = curr.next;
            curr.next = newNode;
            metrics.moves += 2;                         // newNode.next, curr.next
        }
        size++;
    }

    @Override
    public int get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }
        Node curr = head;
        for (int i = 0; i < index; i++) {
            metrics.steps++;                            // move to next node
            curr = curr.next;
        }
        metrics.steps++;                                // read the value
        return curr.val;
    }

    @Override
    public int remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }
        int removedVal;
        if (index == 0) {
            metrics.steps++;                            // read head value
            removedVal = head.val;
            head = head.next;
            metrics.moves++;                            // head
            if (head == null) {
                tail = null;
                metrics.moves++;                        // tail
            }
        } else {
            Node curr = head;
            for (int i = 0; i < index - 1; i++) {
                metrics.steps++;                        // move to next node
                curr = curr.next;
            }
            metrics.steps++;                            // read removed value
            removedVal = curr.next.val;
            curr.next = curr.next.next;
            metrics.moves++;                            // curr.next
            if (index == size - 1) {
                tail = curr;
                metrics.moves++;                        // tail
            }
        }
        size--;
        return removedVal;
    }

    @Override
    public boolean contains(int x) {
        Node curr = head;
        while (curr != null) {
            metrics.steps++;                            // visit node
            metrics.comparisons++;
            if (curr.val == x) {
                return true;
            }
            curr = curr.next;
        }
        return false;
    }
}
