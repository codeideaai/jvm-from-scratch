---
pagetitle: "09 Observing Allocation Pressure with GC Logs"
---

# 09 Observing Allocation Pressure with GC Logs

[English](09-gc-logs.md) · [中文](../../tutorial/09-用GC日志观察分配压力.md)

A service that creates many short-lived objects may slow down because of frequent collection, or because of an unrelated bottleneck. We will create a bounded allocation workload and inspect unified GC logs. Correctness checks must be stable; collection counts and pause durations must be measured.

## Build the mechanism model

Collection includes identifying live objects and reclaiming or reorganizing the remaining space. Marking, copying, and compaction are useful algorithmic building blocks; real collectors combine them across different phases. Generations exploit observed object lifetimes, while cross-generation references require bookkeeping. The young generation is not a completely independent heap.

G1 organizes the heap into regions and selects collection sets from them. A pause target is a scheduling goal, not a real-time upper bound. Live data, reference processing, and operating-system scheduling also affect pauses. Reducing the heap maximum typically increases pressure; it is not a free reduction in memory cost.

Throughput, pause distributions, and resource use require workload-specific trade-offs. We select G1 explicitly so that different default collectors do not contaminate the comparison.

## Complete code

Save this as `Demo09.java`. It uses only the JDK 17 standard library and compiles independently.

```java
public class Demo09 {
    static volatile byte[] sink;
    public static void main(String[] args) {
        long checksum = 0;
        for (int i = 0; i < 2000; i++) {
            byte[] block = new byte[128 * 1024];
            block[0] = (byte) i;
            sink = block;
            checksum += sink[0];
        }
        if (checksum != 152) throw new AssertionError(checksum);
        System.out.println("checksum=" + checksum);
    }
}
```

## Compile and run

```bash
javac --release 17 -encoding UTF-8 Demo09.java
java Demo09
```

Expected output (default verification mode):

```text
checksum=152
```

## Interpreting the evidence

Each allocation is published through a volatile static field, making its result observable. The field retains only the latest array, avoiding an ever-growing live set. At 128 KiB, the arrays are not intended to exercise the humongous-object path under this experiment's usual G1 region configuration.

Run the additional command and inspect gc.log. Identify the collector, occupancy before and after collection, pause causes, and time units. Heap capacity is not total process memory: thread stacks, metaspace, code cache, and native allocations are not all in the Java heap.

Changing Xmx from 32m to 64m produces one capacity comparison. This short workload is insufficient to choose a production collector; sustained load, allocation rate, and tail latency also matter. Unified logging options are documented in the [java command reference](https://docs.oracle.com/en/java/javase/17/docs/specs/man/java.html).

Additional observation commands, run in the directory containing this chapter's class files:

```bash
java -Xms32m -Xmx32m -XX:+UseG1GC -Xlog:gc*:file=gc.log:time,uptime,level,tags Demo09
```

## Read one recorded log line field by field

The event below comes from validation/gc.txt with timestamp and level prefixes omitted. It records one actual run, not a fixed future expectation:

```log
GC(0) Pause Young (Normal) (G1 Evacuation Pause) 14M->1M(32M) 0.784ms
```

| Field | Meaning in this event | Invalid conclusion |
| --- | --- | --- |
| GC(0) | Event identifier | Zero collections occurred |
| Pause Young | Young-generation-related pause | The process only ever collects young objects |
| G1 Evacuation Pause | Name of this G1 evacuation event | Every object has moved |
| 14M → 1M | Summarized heap usage before and after | Process RSS fell from 14M to 1M |
| (32M) | Reported heap capacity at this time | Total process memory is capped at 32M |
| 0.784ms | Duration of this pause | Every request's latency rose by exactly this amount |

The program attempts 2000×128 KiB, approximately 250 MiB of array payload allocation, but does not retain all of it simultaneously. **Cumulative allocation, current live data, and heap capacity are three quantities.** Retaining only the latest array permits older arrays to become collectible, allowing a small heap to sustain larger cumulative allocation.

**Two checks:** does low post-GC occupancy prove no leak? No, it is one observation from a short workload. Do shorter pauses prove higher throughput? No, more frequent pauses and other GC work also cost resources. Hold request patterns, data size, and observation windows constant before comparing a set of metrics.

## Exercises and self-checks

Record collection count, total pause time, and longest pause for both heap capacities without assuming the larger heap wins on every metric. Then retain a bounded recent history using a ring buffer and observe post-collection occupancy. Do not use an unlimited cache to exhaust system memory.

---

[Previous: From Object References to GC Roots](08-references-and-gc-roots.md) · [Contents](../../README.md) · [Next: Publishing Data with Happens-Before](10-happens-before.md)
