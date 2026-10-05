---
pagetitle: "10 Publishing Data with Happens-Before"
---

# 10 Publishing Data with Happens-Before

[English](10-happens-before.md) · [中文](../../tutorial/10-用happens-before安全发布数据.md)

One thread finishing a configuration write does not by itself guarantee what another thread sees. The Java Memory Model specifies permitted observations across threads. It is not merely a diagram assigning every variable to physical “main” and “working” memory regions.

## Build the mechanism model

Visibility, ordering, and atomicity are different properties. volatile establishes synchronization relationships for particular reads and writes, but does not turn a read-increment-write sequence into one indivisible action. AtomicInteger or a lock can provide an atomic counter operation.

This example uses one-shot publication: the producer writes the ordinary payload field, then the volatile ready field. The consumer reads payload after observing ready as true. The protocol establishes a happens-before relationship between the preceding writes and subsequent reads. It is not a reusable multi-message queue protocol.

Thread.start and Thread.join also have synchronization semantics. Joining a producer before an intended racy read can eliminate the very race an experiment was meant to study. Identify what every synchronization point proves.

## Complete code

Save this as `Demo10.java`. It uses only the JDK 17 standard library and compiles independently.

```java
public class Demo10 {
    static int payload;
    static volatile boolean ready;
    static volatile int observed;
    public static void main(String[] args) throws Exception {
        Thread reader = new Thread(() -> {
            long deadline = System.nanoTime() + 5_000_000_000L;
            while (!ready) {
                if (System.nanoTime() - deadline >= 0) return;
                Thread.onSpinWait();
            }
            observed = payload;
        });
        reader.start();
        payload = 42;
        ready = true;
        reader.join();
        if (observed != 42) throw new AssertionError("publication timed out or failed");
        System.out.println("observed=" + observed);
    }
}
```

## Compile and run

```bash
javac --release 17 -encoding UTF-8 Demo10.java
java Demo10
```

Expected output (default verification mode):

```text
observed=42
```

## Interpreting the evidence

The consumer reads payload only after observing ready. The main thread checks observed after joining the consumer, using termination synchronization to receive its result. ready publishes the business data; join completes the test. They have different roles.

The loop has a deadline to prevent an edited experiment from hanging indefinitely. Five seconds is not a formal operating-system scheduling guarantee, however, and extreme load can cause a timeout. Correctness follows from synchronization rules; a successful execution only confirms that this run matched the expectation.

Removing volatile and seeing ten thousand successful runs would not prove the racy program correct. A permitted failure may not manifest with the current hardware and optimization choices. See [JLS 17.4](https://docs.oracle.com/javase/specs/jls/se17/html/jls-17.html#jls-17.4).

## Connect publication through four actions

Start with [F06's thread schedule](../foundations/F06-threads-and-order.md). Actions A/B belong to the publisher, while C/D belong to the reader:

| Action | Operation | Relationship to the next step |
| --- | --- | --- |
| A | Write payload=42 | Program order before B |
| B | Write volatile ready=true | Synchronization with the corresponding subsequent read |
| C | Read ready as true | Program order before D |
| D | Read payload | Publication follows through A→B→C→D |

Happens-before is transitive, so this chain supports a conclusion that “thread two ran a little later” cannot. The example publishes once: ready begins false and the consumer exits after reading this true. Reusing a flag repeatedly without acknowledgment or sequence handling requires a new protocol and proof.

The observed check uses another chain: the reader terminates, main's join returns, and main checks its result. join does not travel backward to repair an earlier invalid access; it is part of completing this test.

**Self-check and answer:** moving A after B removes the original guarantee for that later payload write. Frequent observations of 42 cannot restore the broken proof. Replacing volatile with sleep does not recreate the synchronization edge either.

## Exercises and self-checks

Have two threads each increment a volatile int one hundred thousand times. Then replace the operation with AtomicInteger.incrementAndGet. Do not assert that the incorrect version must fail every time. The useful invariant is that the correct version finishes at two hundred thousand. Explain why setting ready back to false is insufficient to create reliable multi-round communication.

---

[Previous: Observing Allocation Pressure with GC Logs](09-gc-logs.md) · [Contents](../../README.md) · [Next: Monitors and Compound Operations](11-monitors.md)
