import org.openjdk.jol.info.GraphLayout;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Random;

/** Bonus Task A: real memory (bytes) of n elements measured with JOL -> results/memory.csv */
public class MemoryFootprint {
    public static void main(String[] args) throws Exception {
        new File("results").mkdirs();
        try (PrintWriter w = new PrintWriter(new FileWriter("results/memory.csv"))) {
            w.print("structure,n,bytes,bytes_per_element\n");
            for (int n : new int[]{100, 1000, 10000, 100000}) {
                Random rng = new Random(42);
                DynamicArray da = new DynamicArray();
                MyLinkedList list = new MyLinkedList();
                MinHeap heap = new MinHeap();
                for (int i = 0; i < n; i++) {
                    int v = rng.nextInt();
                    da.add(v);
                    list.add(v);
                    heap.insert(v);
                }
                write(w, "DynamicArray", n, GraphLayout.parseInstance(da).totalSize());
                write(w, "MyLinkedList", n, GraphLayout.parseInstance(list).totalSize());
                write(w, "MinHeap", n, GraphLayout.parseInstance(heap).totalSize());
            }
        }
        // Layout of one node: header + fields + alignment padding
        System.out.println(org.openjdk.jol.info.ClassLayout.parseClass(
                Class.forName("MyLinkedList$Node")).toPrintable());
        System.out.println("Saved results/memory.csv");
    }

    private static void write(PrintWriter w, String name, int n, long bytes) {
        w.print(String.format(Locale.US, "%s,%d,%d,%.2f\n", name, n, bytes, bytes / (double) n));
    }
}
