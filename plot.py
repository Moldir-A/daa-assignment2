"""Builds all PNG charts from results/results.csv (and results/memory.csv if it exists).
Usage: python plot.py
"""
import csv, math, os
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

OUT = "results/plots"
os.makedirs(OUT, exist_ok=True)

rows = list(csv.DictReader(open("results/results.csv")))
for r in rows:
    r["n"] = int(r["n"]); r["time_ms"] = float(r["time_ms"])
    for k in ("steps", "moves", "comparisons"):
        r[k] = int(r[k])

def series(workload, variant, structure):
    pts = sorted((r for r in rows if r["workload"] == workload and r["variant"] == variant
                  and r["structure"] == structure), key=lambda r: r["n"])
    return pts

COLORS = {"steps": "tab:blue", "moves": "tab:orange", "comparisons": "tab:green"}
STYLES = ["-", "--"]
MARK = ["o", "s"]

def make(workload, variant, structures, title, fname, ref_nlogn=False):
    fig, (a1, a2) = plt.subplots(1, 2, figsize=(13, 4.6))
    for i, st in enumerate(structures):
        pts = series(workload, variant, st)
        ns = [p["n"] for p in pts]
        a1.plot(ns, [p["time_ms"] for p in pts], marker=MARK[i], label=st)
        for metric in ("steps", "moves", "comparisons"):
            ys = [p[metric] for p in pts]
            if any(ys):
                a2.plot(ns, ys, color=COLORS[metric], linestyle=STYLES[i], marker=MARK[i],
                        markersize=4, label=f"{st}: {metric}")
    if ref_nlogn:
        ns = [p["n"] for p in series(workload, variant, structures[0])]
        a2.plot(ns, [n * math.log2(n) for n in ns], "k:", label="n·log2(n) (reference)")
    for ax, yl, t in ((a1, "Median time, ms", "Time vs n"),
                      (a2, "Operations (count)", "Steps / moves / comparisons vs n")):
        ax.set_xscale("log"); ax.set_yscale("log")
        ax.set_xlabel("n (number of elements)"); ax.set_ylabel(yl); ax.set_title(t)
        ax.grid(True, which="both", alpha=0.3); ax.legend(fontsize=8)
    fig.suptitle(title)
    fig.tight_layout()
    fig.savefig(f"{OUT}/{fname}", dpi=130)
    plt.close(fig)

LISTS = ["DynamicArray", "MyLinkedList"]
make("W1", "-", LISTS, "W1: Random Access get(i), 10 000 calls", "W1_Random_Access.png")
make("W2", "-", LISTS, "W2: Search contains(x), 1 000 queries", "W2_Search.png")
make("W3", "head", LISTS, "W3 head: 1 000 inserts + 1 000 removes at index 0", "W3_Head.png")
make("W3", "middle", LISTS, "W3 middle: 1 000 inserts + 1 000 removes at index n/2", "W3_Middle.png")
make("W4", "-", ["MinHeap"], "W4: MinHeap n x insert + n x extractMin", "W4_MinHeap.png", ref_nlogn=True)
make("W5", "-", ["MinHeap-insert", "MinHeap-buildHeap"],
     "W5 (bonus): n x insert vs Floyd buildHeap", "W5_BuildHeap.png")

if os.path.exists("results/memory.csv"):
    mem = list(csv.DictReader(open("results/memory.csv")))
    fig, ax = plt.subplots(figsize=(6.5, 4.6))
    for i, st in enumerate(["DynamicArray", "MyLinkedList", "MinHeap"]):
        pts = sorted((m for m in mem if m["structure"] == st), key=lambda m: int(m["n"]))
        ax.plot([int(m["n"]) for m in pts], [int(m["bytes"]) / 1e6 for m in pts],
                marker="osd"[i], label=st)
    ax.set_xscale("log"); ax.set_yscale("log")
    ax.set_xlabel("n (number of elements)"); ax.set_ylabel("Memory, MB")
    ax.set_title("Memory footprint (JOL) vs n"); ax.grid(True, which="both", alpha=0.3); ax.legend()
    fig.tight_layout(); fig.savefig(f"{OUT}/Memory.png", dpi=130); plt.close(fig)
print("plots saved to", OUT)
