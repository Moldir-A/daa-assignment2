import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

public class DataStructuresTest {

    // ===================== shared checks for DynamicArray and MyLinkedList =====================

    private static void checkEmptyAndInvalidIndexes(IntList s) {
        assertEquals(0, s.size());
        assertFalse(s.contains(1));
        assertThrows(IndexOutOfBoundsException.class, () -> s.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> s.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> s.remove(0));
        assertThrows(IndexOutOfBoundsException.class, () -> s.add(-1, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> s.add(1, 1));   // index > size
        assertEquals(0, s.size());                                          // nothing was corrupted

        s.add(7);
        assertThrows(IndexOutOfBoundsException.class, () -> s.get(1));      // index == size
        assertThrows(IndexOutOfBoundsException.class, () -> s.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> s.remove(1));
        assertThrows(IndexOutOfBoundsException.class, () -> s.remove(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> s.add(2, 1));
        assertEquals(1, s.size());
        assertEquals(7, s.get(0));
    }

    private static void checkOneElement(IntList s) {
        s.add(5);
        assertEquals(1, s.size());
        assertEquals(5, s.get(0));
        assertTrue(s.contains(5));
        assertEquals(5, s.remove(0));
        assertEquals(0, s.size());
        assertFalse(s.contains(5));
        s.add(0, 9);                                  // structure still works after becoming empty
        s.add(1, 10);
        assertEquals(9, s.get(0));
        assertEquals(10, s.get(1));
    }

    private static void checkDuplicates(IntList s) {
        for (int i = 0; i < 4; i++) s.add(3);
        assertTrue(s.contains(3));
        assertEquals(3, s.remove(0));
        assertEquals(3, s.size());
        assertTrue(s.contains(3));
        s.remove(0);
        s.remove(0);
        s.remove(0);
        assertFalse(s.contains(3));
        assertEquals(0, s.size());
    }

    private static void checkFirstAndLast(IntList s) {
        for (int i = 1; i <= 5; i++) s.add(i);
        assertEquals(1, s.get(0));
        assertEquals(5, s.get(4));
        s.add(0, 0);                                   // insert at the first index
        s.add(s.size(), 99);                           // insert at the end (index == size)
        assertEquals(0, s.get(0));
        assertEquals(99, s.get(s.size() - 1));
        assertEquals(0, s.remove(0));                  // remove first
        assertEquals(99, s.remove(s.size() - 1));      // remove last
        for (int i = 1; i <= 5; i++) assertEquals(i, s.get(i - 1));
        assertEquals(5, s.size());
    }

    private static void checkRandomAgainstJavaUtil(IntList s, List<Integer> model, long seed) {
        Random rng = new Random(seed);
        for (int op = 0; op < 5000; op++) {
            int kind = rng.nextInt(5);
            int v = rng.nextInt(50);                   // small range => many duplicates
            if (kind == 0) {
                s.add(v);
                model.add(v);
            } else if (kind == 1) {
                int idx = rng.nextInt(model.size() + 1);
                s.add(idx, v);
                model.add(idx, v);
            } else if (kind == 2 && !model.isEmpty()) {
                int idx = rng.nextInt(model.size());
                assertEquals((int) model.remove(idx), s.remove(idx));
            } else if (kind == 3 && !model.isEmpty()) {
                int idx = rng.nextInt(model.size());
                assertEquals((int) model.get(idx), s.get(idx));
            } else {
                assertEquals(model.contains(v), s.contains(v));
            }
            assertEquals(model.size(), s.size());
            if (op % 100 == 0) {
                for (int i = 0; i < model.size(); i++) assertEquals((int) model.get(i), s.get(i));
            }
        }
        for (int i = 0; i < model.size(); i++) assertEquals((int) model.get(i), s.get(i));
    }

    // ===================== DynamicArray =====================

    @Test
    public void dynamicArrayEmptyAndInvalid() { checkEmptyAndInvalidIndexes(new DynamicArray()); }

    @Test
    public void dynamicArrayOneElement() { checkOneElement(new DynamicArray()); }

    @Test
    public void dynamicArrayDuplicates() { checkDuplicates(new DynamicArray()); }

    @Test
    public void dynamicArrayFirstAndLast() { checkFirstAndLast(new DynamicArray()); }

    @Test
    public void dynamicArrayRandomVsArrayList() {
        checkRandomAgainstJavaUtil(new DynamicArray(), new ArrayList<>(), 42);
    }

    @Test
    public void dynamicArrayGrowsTwice() {
        DynamicArray da = new DynamicArray(4);
        for (int i = 0; i < 4; i++) da.add(i);
        assertEquals(4, da.capacity());
        da.add(4);                                     // full -> grow 2x
        assertEquals(8, da.capacity());
        for (int i = 5; i < 9; i++) da.add(i);
        assertEquals(16, da.capacity());
        for (int i = 0; i < 9; i++) assertEquals(i, da.get(i));   // nothing lost while growing
    }

    @Test
    public void dynamicArrayZeroCapacityConstructor() {
        DynamicArray da = new DynamicArray(0);
        for (int i = 0; i < 20; i++) da.add(i);
        assertEquals(20, da.size());
        assertEquals(19, da.get(19));
    }

    @Test
    public void dynamicArrayCounters() {
        DynamicArray da = new DynamicArray();
        for (int i = 0; i < 100; i++) da.add(i);
        da.getMetrics().reset();
        da.get(50);
        assertEquals(1, da.getMetrics().steps);        // get(i) = 1 step
        da.getMetrics().reset();
        da.add(0, -1);                                 // shifts 100 elements
        assertEquals(101, da.getMetrics().moves);      // 100 shifts + 1 write
        da.getMetrics().reset();
        assertFalse(da.contains(12345));
        assertEquals(101, da.getMetrics().comparisons);
    }

    // ===================== MyLinkedList =====================

    @Test
    public void listEmptyAndInvalid() { checkEmptyAndInvalidIndexes(new MyLinkedList()); }

    @Test
    public void listOneElement() { checkOneElement(new MyLinkedList()); }

    @Test
    public void listDuplicates() { checkDuplicates(new MyLinkedList()); }

    @Test
    public void listFirstAndLast() { checkFirstAndLast(new MyLinkedList()); }

    @Test
    public void listRandomVsLinkedList() {
        checkRandomAgainstJavaUtil(new MyLinkedList(), new LinkedList<>(), 7);
    }

    @Test
    public void listTailStaysCorrect() {
        MyLinkedList l = new MyLinkedList();
        l.add(1);
        l.add(2);
        l.add(3);
        assertEquals(3, l.remove(2));                  // remove the tail
        l.add(4);                                      // must attach after the NEW tail
        assertEquals(3, l.size());
        assertEquals(4, l.get(2));
        l.remove(0);
        l.remove(0);
        l.remove(0);                                   // list becomes empty (head and tail = null)
        l.add(8);
        assertEquals(8, l.get(0));
        assertEquals(1, l.size());
    }

    @Test
    public void listCountersAreNotZero() {
        MyLinkedList l = new MyLinkedList();
        for (int i = 0; i < 100; i++) l.add(i);
        l.getMetrics().reset();
        l.add(0, -1);
        assertTrue(l.getMetrics().moves > 0);          // pointer updates are counted
        l.remove(0);
        assertTrue(l.getMetrics().steps > 0);
        l.getMetrics().reset();
        l.get(60);
        assertEquals(61, l.getMetrics().steps);        // get(i) = i moves + 1 read
    }

    // ===================== MinHeap =====================

    @Test
    public void heapEmptyThrows() {
        MinHeap h = new MinHeap();
        assertThrows(IllegalStateException.class, h::peekMin);
        assertThrows(IllegalStateException.class, h::extractMin);
    }

    @Test
    public void heapSingleElementAndPeek() {
        MinHeap h = new MinHeap();
        h.insert(42);
        assertEquals(42, h.peekMin());
        assertEquals(1, h.size());                     // peekMin does not remove
        assertEquals(42, h.extractMin());
        assertEquals(0, h.size());
        assertThrows(IllegalStateException.class, h::extractMin);
    }

    @Test
    public void heapZeroCapacityConstructor() {
        MinHeap h = new MinHeap(0);
        for (int i = 20; i > 0; i--) h.insert(i);
        assertEquals(1, h.peekMin());
    }

    @Test
    public void heapPropertyAfterEveryOperation() {
        MinHeap h = new MinHeap();
        Random rng = new Random(42);
        for (int i = 0; i < 1000; i++) {
            h.insert(rng.nextInt(100));                // duplicates on purpose
            assertTrue(h.isValidHeap());
        }
        while (h.size() > 0) {
            h.extractMin();
            assertTrue(h.isValidHeap());
        }
    }

    private static void checkSortedOutput(int n) {
        MinHeap h = new MinHeap();
        Random rng = new Random(42);
        for (int i = 0; i < n; i++) h.insert(rng.nextInt());
        int prev = h.extractMin();
        for (int i = 1; i < n; i++) {
            int cur = h.extractMin();
            assertTrue(prev <= cur);
            prev = cur;
        }
        assertEquals(0, h.size());
    }

    @Test
    public void heapSortedOutput100() { checkSortedOutput(100); }

    @Test
    public void heapSortedOutput10000() { checkSortedOutput(10000); }

    @Test
    public void heapDuplicatesOnly() {
        MinHeap h = new MinHeap();
        for (int i = 0; i < 50; i++) h.insert(5);
        for (int i = 0; i < 50; i++) assertEquals(5, h.extractMin());
    }

    @Test
    public void heapRandomVsPriorityQueue() {
        MinHeap h = new MinHeap();
        PriorityQueue<Integer> pq = new PriorityQueue<>();
        Random rng = new Random(3);
        for (int op = 0; op < 5000; op++) {
            if (rng.nextInt(3) != 0 || pq.isEmpty()) {
                int v = rng.nextInt(1000);
                h.insert(v);
                pq.add(v);
            } else {
                assertEquals((int) pq.poll(), h.extractMin());
            }
            assertEquals(pq.size(), h.size());
            if (!pq.isEmpty()) assertEquals((int) pq.peek(), h.peekMin());
            assertTrue(h.isValidHeap());
        }
    }

    @Test
    public void heapBuildHeap() {
        Random rng = new Random(11);
        int[] arr = new int[1000];
        for (int i = 0; i < arr.length; i++) arr[i] = rng.nextInt(500);
        MinHeap h = new MinHeap();
        h.buildHeap(arr);
        assertEquals(1000, h.size());
        assertTrue(h.isValidHeap());
        int prev = h.extractMin();
        for (int i = 1; i < 1000; i++) {
            int cur = h.extractMin();
            assertTrue(prev <= cur);
            prev = cur;
        }
    }

    @Test
    public void heapBuildHeapEmptyThenInsert() {
        MinHeap h = new MinHeap();
        h.buildHeap(new int[0]);
        assertEquals(0, h.size());
        h.insert(3);
        h.insert(1);
        assertEquals(1, h.peekMin());
    }
}
