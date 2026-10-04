# DAA Assignment 2: In-Memory Workload Engine

Own implementations of `DynamicArray`, `MyLinkedList` and `MinHeap` (no `java.util` collections,
only primitive `int`), exact operation counters (`steps`, `moves`, `comparisons`), a benchmark
for 4 workloads (+ bonus W5), JUnit 5 tests, and plots.

## Requirements
- JDK 17+
- Maven 3.8+
- Python 3 with `matplotlib` (only for the plots)

## Project structure
```
src/main/java   Metrics, IntList, DynamicArray, MyLinkedList, MinHeap,
                BenchmarkRunner, MemoryFootprint (JOL, bonus A)
src/test/java   DataStructuresTest (JUnit 5)
results/        results.csv, memory.csv, plots/*.png
plot.py         builds all charts from the CSV files
REPORT.md       complexity table, loop-invariant proofs, plots, discussion
```

## Build and test
```bash
mvn test
```

## Run the benchmark (one command, seed 42, writes results/results.csv)
```bash
mvn -q compile exec:java
```
(Alternative, a bit cleaner timing because Maven is not in the same JVM:
`mvn -q compile && java -cp target/classes BenchmarkRunner`)

Each case runs 3 warm-up runs (discarded) + 5 measured runs; the median time is saved.

## Build the plots
```bash
python plot.py
```

## Memory footprint (bonus A, JOL)
```bash
mvn -q compile exec:java -Dexec.mainClass=MemoryFootprint
python plot.py
```

## What the counters mean
| Counter | Definition |
|---|---|
| `steps` | one read of an array cell, or one move to the next node |
| `moves` | one element shifted/written in an array, or one pointer update in a list (creating a node is not a move) |
| `comparisons` | one comparison of two elements |

Counters are incremented inside the methods, not estimated afterwards.

## CSV format
`workload,variant,structure,n,time_ms,steps,moves,comparisons`
(`variant` is `head` / `middle` for W3 and `-` for the others; W5 is the bonus `buildHeap` comparison.)
