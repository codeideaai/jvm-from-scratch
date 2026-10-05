---
pagetitle: "16 Connecting Thread, Heap, and JFR Evidence"
---

# 16 Connecting Thread, Heap, and JFR Evidence

[English](16-diagnostics-and-jfr.md) · [中文](../../tutorial/16-把线程堆与JFR串成诊断流程.md)

Increasing heap size as soon as a service slows down may miss the real bottleneck. First classify the symptom: CPU saturation, waiting requests, allocation pressure, or growing native memory. Then select evidence that can test the hypothesis.

## Build the mechanism model

Thread stacks show what threads execute or wait for. Heap information addresses heap capacity and objects. JFR connects execution, allocation, locks, and time through events. These tools have different costs and coverage; one screenshot cannot explain every failure.

jcmd sends diagnostic commands to a target JVM, normally requiring the same user and appropriate system permissions. Container PID namespaces and disabled Attach support can affect connection. This experiment starts its own process and prints its PID; it does not scan or manipulate other services.

A class histogram aggregates object counts but cannot explain every retaining reference. Heap-dump analysis can reveal dominators and reference paths. Dumps may require substantial pauses and disk space, so understand the cost in a controlled experiment first.

## Complete code

Save this as `Demo16.java`. It uses only the JDK 17 standard library and compiles independently.

```java
import java.util.ArrayList;
import java.util.List;
public class Demo16 {
    static final List<byte[]> retained = new ArrayList<>();
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 16; i++) retained.add(new byte[64 * 1024]);
        System.out.println("blocks=" + retained.size());
        if (args.length > 0 && args[0].equals("--observe")) {
            System.out.println("pid=" + ProcessHandle.current().pid());
            Thread.sleep(60_000);
        }
        if (retained.size() != 16) throw new AssertionError();
    }
}
```

## Compile and run

```bash
javac --release 17 -encoding UTF-8 Demo16.java
java Demo16
```

Expected output (default verification mode):

```text
blocks=16
```

## Interpreting the evidence

Default execution exits promptly for automated verification. Observation mode waits for approximately one minute and prints a PID. In a second terminal, replace PID below with that number. Capture a stack and heap summary, then start a ten-second JFR recording.

```bash
java Demo16 --observe
# In another terminal, replace PID with the printed process ID.
jcmd PID Thread.print
jcmd PID GC.heap_info
jcmd PID JFR.start name=learning settings=profile duration=10s filename=learning.jfr
# After recording completes:
jfr summary learning.jfr
```

The main thread sleeps, so expect timed waiting. TIMED_WAITING alone does not mean deadlock. CPU investigations require samples over time to identify persistent activity. Leak investigations require growth trends and retaining-reference evidence.

To investigate native memory, start the JVM with `-XX:NativeMemoryTracking=summary`, then run `jcmd PID VM.native_memory summary`. This tracks supported HotSpot allocation categories, not every allocation made by arbitrary third-party native libraries.

## What to read first in a thread dump

This excerpt from the recorded experiment omits variable addresses, PID, and timing:

```log
"main"
   java.lang.Thread.State: TIMED_WAITING (sleeping)
    at java.lang.Thread.sleep(java.base@17.0.20/Native Method)
    at Demo16.main(Demo16.java:10)
```

Identify the thread, read its state, then find the first frame you can relate to your source. Here main deliberately sleeps for the observation window. Native Method describes how that method is implemented; it is not evidence of a JNI crash.

| Original symptom | First evidence | Next question |
| --- | --- | --- |
| Sustained high CPU | Repeated samples, CPU metrics, JFR | Is the same hotspot active, and does load explain it? |
| Requests never return | Thread stacks and held/awaited resources | A lock cycle, normal I/O, or a condition wait? |
| Rising post-GC baseline | GC trends, object distribution, retaining paths if needed | What retains data, and is it still useful? |
| Process memory rises while heap stays modest | Threads, native memory, system records | Is attention to Xmx alone missing the resource? |

Many RUNNABLE threads alone do not prove saturated CPU, and many WAITING threads alone do not prove deadlock. Relate state to time, workload, and resource relationships.

## Keep a reviewable diagnostic notebook

Record symptom and time window → hypothesis → commands and options → key evidence → one controlled change → equivalent-load recheck. Cache retention should be supported by surviving objects and their retaining paths. If evidence instead identifies computation, revise the hypothesis rather than continuing to enlarge the heap.

**Check:** would increasing heap wake this sleeping main thread earlier? No; the wait is specified by the code. Does successful data collection mean diagnosis is complete? No; tool execution gathers evidence, which still requires interpretation.

## Exercises and self-checks

Replace sleep during observation with a bounded computation and compare thread stacks and JFR samples. Then keep computation fixed and increase only the bounded retained data. Determine which metrics change. Changing workload, heap size, and recording settings together makes causal interpretation much harder.

---

[Previous: Building Trustworthy Performance Experiments](15-performance-experiments.md) · [Contents](../../README.md) · [Next: A Minimal Java Agent for Object Sizes](17-java-agent.md)
