---
pagetitle: "F05 Separate Memory Exhaustion, Leaks, and GC Pressure"
---

# F05 Separate Memory Exhaustion, Leaks, and GC Pressure

[English](F05-memory-failures.md) · [中文](../../foundations/F05-分清内存溢出泄漏与GC压力.md)

## Do not call every memory symptom a leak

**Exhaustion** is a failed resource request. A **leak** is one possible cause: the program retains data it no longer needs, preventing timely eligibility for collection. **GC pressure** is the time and resource cost of allocation and collection activity. The three are related but not interchangeable.

A legitimately large dataset can exceed a small heap without a leak. An unbounded cache can leak for a long time before exhausting memory. Rapid short-lived allocation can cause frequent collection while post-collection live data stays stable.

## Observe failure in a small process

Unlike the ordinary examples, this one **must use the heap-limited command below**. It runs in a newly launched Java process with a 32 MiB maximum Java heap. A guard rejects accidental execution under a default large heap. Native process memory is additional, but the example does not request gigabytes of Java heap for its payloads.

The program attempts to retain at most 128 arrays of 1 MiB each. The number of attempts is bounded. The list retains all successful allocations, so the collector cannot simply discard the reachable payloads. Allocation should fail before all attempts succeed. Serial GC is chosen explicitly to reduce distractions such as collector-specific large-object paths.

Clearing the list in catch allows this short experiment to report and exit. It is **not a recommendation to catch OOM and continue normal production service**. A real process may no longer reliably allocate logs, responses, or cleanup state.

## Complete code

Save as `Foundation05.java`.

```java
import java.util.ArrayList;
import java.util.List;
public class Foundation05 {
    public static void main(String[] args) {
        long max = Runtime.getRuntime().maxMemory();
        if (max < 16L * 1024 * 1024 || max > 40L * 1024 * 1024) {
            throw new IllegalStateException("use -Xms16m -Xmx32m -XX:+UseSerialGC");
        }
        List<byte[]> retained = new ArrayList<>(128);
        System.out.println("heap guard passed=true");
        try {
            for (int i = 0; i < 128; i++) retained.add(new byte[1024 * 1024]);
            throw new AssertionError("small heap unexpectedly held all payloads");
        } catch (OutOfMemoryError expected) {
            retained.clear();
            System.out.println("failure=OutOfMemoryError");
            System.out.println("references cleared=" + retained.isEmpty());
        }
    }
}
```

## Compile and run

```bash
javac --release 17 -encoding UTF-8 Foundation05.java
java -Xms16m -Xmx32m -XX:+UseSerialGC -cp . Foundation05
```

Expected output:

```text
heap guard passed=true
failure=OutOfMemoryError
references cleared=true
```

## Read the flags and output separately

`-Xms16m` configures the initial heap; `-Xmx32m` limits the maximum heap. These suffixes use binary magnitudes: 1 MiB is 1024×1024 bytes. Thread stacks have separate controls such as -Xss. Raising Xmx does not repair unbounded recursion.

The first line confirms the guard. The second records OutOfMemoryError. The third establishes only that the list no longer retains elements, **not that heap occupancy instantly became zero**. Do not assert an exact failing allocation count: headers, list storage, available space, and implementation details affect the boundary.

| Symptom or message | Initial area to investigate | Next evidence |
| --- | --- | --- |
| OutOfMemoryError: Java heap space | Heap allocation capacity | Heap flags, post-GC trends, retaining paths |
| OutOfMemoryError: Metaspace | Class metadata | Loaded classes, retained loaders, metadata limits |
| StackOverflowError | Thread invocation depth or stack capacity | Repeated frames and recursion termination |
| unable to create native thread | Thread creation and native limits | Thread count, OS limits, native process memory |
| Container kills the process without a Java exception | Possible external resource limit | Exit reason, container events, system records |

These are investigation starting points, not diagnoses established by matching one string. Increasing the heap is not a universal response.

## Start GC-log reading with arithmetic

The recorded Chapter 09 log includes `14M->1M(32M) 0.784ms`. This summarizes heap usage before and after that event, reported capacity at that time, and pause duration. An approximately 13M occupancy decrease is not the total allocated since startup and is not a promise about the next collection.

A rising baseline after repeated collections motivates investigation of retained objects. One instantaneous snapshot cannot establish a leak. Chapter 18 compares bounded and unbounded caches under the same workload.

**Exercises and answers:** retaining only four arrays with the 32 MiB cap should become a normal-completion test rather than an OOM expectation. Changing only the original command to -Xmx512m is rejected by the guard intentionally. Whether retained data is a leak depends on whether the application still needs it, not merely on the existence of a reference.

---

[Previous: Understand GC through Reference Graphs](F04-gc-from-graphs.md) · [Contents](../../README.md) · [Next: Draw Thread Schedules before Discussing Visibility](F06-threads-and-order.md)
