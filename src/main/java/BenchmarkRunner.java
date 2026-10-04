import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

/**
 * Runs workloads W1-W4 (+ W5 bonus buildHeap) and writes results/results.csv.
 * Every case: WARMUP unmeasured runs, then RUNS measured runs, median time is saved.
 * Same seed (42) => same data => reproducible counters.
 */
public class BenchmarkRunner {
    private static final int[] SIZES = {100, 1000, 10000, 100000};
    private static final String[] LISTS = {"DynamicArray", "MyLinkedList"};
    private static final int WARMUP = 3;
    private static final int RUNS = 5;
    private static final long SEED = 42;
    private static volatile long sink;   // stops the JIT from removing "unused" results

    public static void main(String[] args) throws IOException {
        // Global JVM warm-up: run everything once at n=1000 and throw the output away.
        PrintWriter dummy = new PrintWriter(new StringWriter());
        for (int i = 0; i < 2; i++) {
            runAll(dummy, 1000);
        }

        new File("results").mkdirs();
        try (PrintWriter writer = new PrintWriter(new FileWriter("results/results.csv"))) {
            writer.print("workload,variant,structure,n,time_ms,steps,moves,comparisons\n");
            for (int n : SIZES) {
                System.out.println("n = " + n);
                runAll(writer, n);
            }
        }
        System.out.println("Benchmark completed. Results saved to results/results.csv");
    }

    private static void runAll(PrintWriter w, int n) {
        for (String s : LISTS) runW1(w, n, s);
        for (String s : LISTS) runW2(w, n, s);
        for (String s : LISTS) runW3(w, n, s, "head");
        for (String s : LISTS) runW3(w, n, s, "middle");
        runW4(w, n);
        runW5(w, n);
    }

    private static IntList create(String name) {
        return name.equals("DynamicArray") ? new DynamicArray() : new MyLinkedList();
    }

    /** Fills the structure with n EVEN values (so odd numbers are guaranteed absent). */
    private static int[] fill(IntList s, Random rng, int n) {
        int[] values = new int[n];
        for (int i = 0; i < n; i++) {
            values[i] = rng.nextInt() << 1;
            s.add(values[i]);
        }
        return values;
    }

    // W1: fill n values, then 10 000 get(index) with random indexes
    private static void runW1(PrintWriter w, int n, String name) {
        long[] times = new long[RUNS];
        Metrics m = null;
        for (int run = 0; run < WARMUP + RUNS; run++) {
            Random rng = new Random(SEED);
            IntList s = create(name);
            fill(s, rng, n);
            int[] idxs = new int[10000];                // same indexes for both structures
            for (int i = 0; i < idxs.length; i++) idxs[i] = rng.nextInt(n);

            s.getMetrics().reset();
            long t0 = System.nanoTime();
            long acc = 0;
            for (int idx : idxs) acc += s.get(idx);
            long dt = System.nanoTime() - t0;
            sink += acc;
            if (run >= WARMUP) times[run - WARMUP] = dt;
            m = s.getMetrics();
        }
        record(w, "W1", "-", name, n, times, m);
    }

    // W2: 1000 contains(): half present (random existing values), half absent (odd numbers)
    private static void runW2(PrintWriter w, int n, String name) {
        long[] times = new long[RUNS];
        Metrics m = null;
        for (int run = 0; run < WARMUP + RUNS; run++) {
            Random rng = new Random(SEED);
            IntList s = create(name);
            int[] values = fill(s, rng, n);
            int[] queries = new int[1000];
            for (int i = 0; i < queries.length; i++) {
                queries[i] = (i % 2 == 0) ? values[rng.nextInt(n)] : ((rng.nextInt() << 1) | 1);
            }

            s.getMetrics().reset();
            long t0 = System.nanoTime();
            int found = 0;
            for (int q : queries) if (s.contains(q)) found++;
            long dt = System.nanoTime() - t0;
            if (found != 500) throw new IllegalStateException("W2: expected 500 hits, got " + found);
            if (run >= WARMUP) times[run - WARMUP] = dt;
            m = s.getMetrics();
        }
        record(w, "W2", "-", name, n, times, m);
    }

    // W3: 1000 insertions + 1000 removals at index 0 (head) or n/2 (middle)
    private static void runW3(PrintWriter w, int n, String name, String variant) {
        long[] times = new long[RUNS];
        Metrics m = null;
        for (int run = 0; run < WARMUP + RUNS; run++) {
            Random rng = new Random(SEED);
            IntList s = create(name);
            fill(s, rng, n);
            int idx = variant.equals("head") ? 0 : n / 2;

            s.getMetrics().reset();
            long t0 = System.nanoTime();
            for (int i = 0; i < 1000; i++) s.add(idx, 999);
            for (int i = 0; i < 1000; i++) s.remove(idx);
            long dt = System.nanoTime() - t0;
            if (s.size() != n) throw new IllegalStateException("W3: wrong final size");
            if (run >= WARMUP) times[run - WARMUP] = dt;
            m = s.getMetrics();
        }
        record(w, "W3", variant, name, n, times, m);
    }

    // W4: insert n values, then extractMin n times; check non-decreasing order
    private static void runW4(PrintWriter w, int n) {
        long[] times = new long[RUNS];
        Metrics m = null;
        for (int run = 0; run < WARMUP + RUNS; run++) {
            Random rng = new Random(SEED);
            MinHeap heap = new MinHeap();
            heap.getMetrics().reset();
            long t0 = System.nanoTime();
            for (int i = 0; i < n; i++) heap.insert(rng.nextInt());
            int prev = Integer.MIN_VALUE;
            boolean sorted = true;
            for (int i = 0; i < n; i++) {
                int cur = heap.extractMin();
                if (cur < prev) sorted = false;
                prev = cur;
            }
            long dt = System.nanoTime() - t0;
            if (!sorted) throw new IllegalStateException("W4: output is not sorted!");
            if (run >= WARMUP) times[run - WARMUP] = dt;
            m = heap.getMetrics();
        }
        record(w, "W4", "-", "MinHeap", n, times, m);
    }

    // W5 (bonus B): n separate insert() calls vs Floyd buildHeap on the same data
    private static void runW5(PrintWriter w, int n) {
        long[] tIns = new long[RUNS];
        long[] tBuild = new long[RUNS];
        Metrics mIns = null, mBuild = null;
        for (int run = 0; run < WARMUP + RUNS; run++) {
            Random rng = new Random(SEED);
            int[] data = new int[n];
            for (int i = 0; i < n; i++) data[i] = rng.nextInt();

            MinHeap h1 = new MinHeap();
            h1.getMetrics().reset();
            long t0 = System.nanoTime();
            for (int v : data) h1.insert(v);
            long d1 = System.nanoTime() - t0;

            MinHeap h2 = new MinHeap();
            h2.getMetrics().reset();
            t0 = System.nanoTime();
            h2.buildHeap(data);
            long d2 = System.nanoTime() - t0;

            if (!h1.isValidHeap() || !h2.isValidHeap()) throw new IllegalStateException("W5: invalid heap");
            if (run >= WARMUP) {
                tIns[run - WARMUP] = d1;
                tBuild[run - WARMUP] = d2;
            }
            mIns = h1.getMetrics();
            mBuild = h2.getMetrics();
        }
        record(w, "W5", "-", "MinHeap-insert", n, tIns, mIns);
        record(w, "W5", "-", "MinHeap-buildHeap", n, tBuild, mBuild);
    }

    private static void record(PrintWriter w, String workload, String variant, String structure,
                               int n, long[] times, Metrics m) {
        long[] sorted = times.clone();
        Arrays.sort(sorted);
        double medianMs = sorted[sorted.length / 2] / 1_000_000.0;   // median of 5 = 3rd value
        // Locale.US: otherwise some locales print "0,1234" and break the CSV
        w.print(String.format(Locale.US, "%s,%s,%s,%d,%.4f,%d,%d,%d\n",
                workload, variant, structure, n, medianMs, m.steps, m.moves, m.comparisons));
        w.flush();
    }
}
