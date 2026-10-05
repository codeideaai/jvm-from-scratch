---
pagetitle: "F06 Draw Thread Schedules before Discussing Visibility"
---

# F06 Draw Thread Schedules before Discussing Visibility

[English](F06-threads-and-order.md) · [中文](../../foundations/F06-把线程时序画出来再谈可见性.md)

## One process can contain several execution paths

A thread is an execution path within a process. Threads have separate invocation state but can access shared objects through references. Concurrency includes interleaving or overlapping progress, even on one CPU core. Parallel execution on several cores is not required for concurrency bugs.

start requests execution in a new thread; calling run directly is an ordinary call in the current thread. join waits for the target to finish rather than merging threads. Joining before examining results also establishes the required post-termination observation order.

## value++ is a compound operation

Temporarily split it into reading a value, adding one, and writing back. Starting from zero, this interleaving is possible:

| Time | Thread A | Thread B | Shared value |
| --- | --- | --- | --- |
| 1 | Read 0 | | 0 |
| 2 | | Read 0 | 0 |
| 3 | Write 1 | | 1 |
| 4 | | Write 1 | 1 |

Two increments leave only 1. Both reads can observe the latest value at their respective times, so this failure does not require a story about indefinitely stale CPU caches.

We use CyclicBarrier to make both reads finish before either write. The barrier is an experimental rendezvous: both participants must arrive before continuing. A timeout prevents indefinite waiting after an experimental mistake. **This deliberately arranged schedule explains a compound operation; it does not measure random scheduling probabilities.**

## Complete code

Save as `Foundation06.java`.

```java
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
public class Foundation06 {
    static volatile int value;
    static void await(CyclicBarrier gate) {
        try { gate.await(5, TimeUnit.SECONDS); }
        catch (Exception e) { throw new AssertionError(e); }
    }
    public static void main(String[] args) throws Exception {
        CyclicBarrier bothRead = new CyclicBarrier(2);
        Runnable splitIncrement = () -> {
            int local = value;
            await(bothRead);
            value = local + 1;
        };
        Thread a = new Thread(splitIncrement);
        Thread b = new Thread(splitIncrement);
        a.start(); b.start(); a.join(); b.join();
        if (value != 1) throw new AssertionError(value);
        System.out.println("split increment=" + value);
        AtomicInteger count = new AtomicInteger();
        Runnable atomicIncrement = () -> {
            for (int i = 0; i < 1000; i++) count.incrementAndGet();
        };
        Thread c = new Thread(atomicIncrement);
        Thread d = new Thread(atomicIncrement);
        c.start(); d.start(); c.join(); d.join();
        if (count.get() != 2000) throw new AssertionError(count.get());
        System.out.println("atomic increments=" + count.get());
    }
}
```

## Compile and run

```bash
javac --release 17 -encoding UTF-8 Foundation06.java
java -cp . Foundation06
```

Expected output:

```text
split increment=1
atomic increments=2000
```

## Separate three questions

**Atomicity:** must a group of reads and writes complete as one indivisible operation? The volatile field does not combine our separate read and write into an atomic increment. AtomicInteger.incrementAndGet does provide that update, so two sets of 1000 increments finish at 2000.

**Visibility:** under which rules can another thread observe a write? Waiting a few milliseconds is not a synchronization protocol. sleep suspends execution but does not establish the publication relationship you need.

**Ordering:** which actions have a specified ordering relationship? happens-before is not a comparison of wall-clock timestamps from printed lines. It supports reasoning about allowed observations. Main Chapter 10 uses ready to connect publication to a later read.

## Locks and volatile are not interchangeable

Checking positive stock and then decrementing it requires a protocol protecting the combined decision and update. A volatile stock field alone does not prevent two buyers from both passing the check. One common lock around the whole operation, or an appropriate atomic protocol, addresses that invariant.

Conversely, a one-shot payload write followed by volatile ready=true needs a publication relationship, not necessarily a lock around every read. State the invariant before selecting synchronization tools.

**Exercises and answers:** replacing start with a direct run call makes the first invocation wait at the barrier before the same thread can execute the second invocation. It times out because two workers were not started concurrently. Replacing incrementAndGet with separate get and set calls also loses atomic update semantics. Failure then need not appear on every run; the first experiment's always-1 assertion depends on its deliberately arranged barrier schedule.

Once you can explain why fresh values can still produce an incorrect result, continue with publication and monitors in Chapters 10–11.

---

[Previous: Separate Memory Exhaustion, Leaks, and GC Pressure](F05-memory-failures.md) · [Contents](../../README.md) · [Main course](../tutorial/00-introduction.md)
