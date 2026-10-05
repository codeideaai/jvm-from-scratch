---
pagetitle: "11 Monitors and Compound Operations"
---

# 11 Monitors and Compound Operations

[English](11-monitors.md) · [中文](../../tutorial/11-理解监视器与复合操作.md)

Reducing stock requires checking availability and changing the balance as one operation. Visibility of individual reads and writes is insufficient: two threads may both observe enough stock. synchronized protects a sequence that must execute under one monitor.

## Build the mechanism model

Mutual exclusion works only when participants use the same lock. An instance synchronized method locks this; a static synchronized method locks the corresponding Class object. Calls on different instances do not automatically serialize. Prefer a stable, private lock object so unrelated code cannot accidentally join the protocol.

Monitors are reentrant. A thread that already holds a monitor can acquire it again without blocking itself forever. Abrupt exit from a synchronized block must release the monitor. Bytecode for blocks exposes monitorenter and monitorexit, whereas synchronized methods carry a method flag.

Lock implementation and optimization vary across HotSpot releases. A fixed “biased → lightweight → heavyweight” progression is not a universal execution pipeline. Prove the program using synchronization semantics, then study cost on a specified implementation.

## Complete code

Save this as `Demo11.java`. It uses only the JDK 17 standard library and compiles independently.

```java
public class Demo11 {
    static final class Counter {
        private final Object lock = new Object();
        private int value;
        void increment() {
            synchronized (lock) { value++; }
        }
        int value() {
            synchronized (lock) { return value; }
        }
        void nested() {
            synchronized (lock) { increment(); }
        }
    }
    public static void main(String[] args) throws Exception {
        Counter counter = new Counter();
        Runnable work = () -> {
            for (int i = 0; i < 100_000; i++) counter.increment();
        };
        Thread a = new Thread(work);
        Thread b = new Thread(work);
        a.start(); b.start();
        a.join(); b.join();
        counter.nested();
        if (counter.value() != 200001) throw new AssertionError(counter.value());
        System.out.println("count=" + counter.value());
    }
}
```

## Compile and run

```bash
javac --release 17 -encoding UTF-8 Demo11.java
java Demo11
```

Expected output (default verification mode):

```text
count=200001
```

## Interpreting the evidence

Both threads share one Counter and one lock, so the read, addition, and write in value++ occur in one mutually exclusive region. nested calls increment while holding that same monitor, exercising reentrancy.

Releasing the monitor also synchronizes with a later acquisition of it. Locking writers while allowing arbitrary unsynchronized readers does not establish a generally safe protocol. The read method therefore acquires the same lock.

Real programs with several locks need a consistent acquisition order. One thread holding an inventory lock while waiting for an order lock can deadlock with another doing the reverse. Diagnosis needs both held and awaited resources; a state label such as WAITING or BLOCKED alone is insufficient.

Additional observation commands, run in the directory containing this chapter's class files:

```bash
javap -c -p 'Demo11$Counter'
```

## Exercises and self-checks

Replace increment's lock with a fresh `new Object()` on every call and explain why it no longer protects the shared count. Then give each worker its own Counter and relate result isolation to lock granularity. Use javap to inspect monitor release on exceptional exits.

---

[Previous: Publishing Data with Happens-Before](10-happens-before.md) · [Contents](../../README.md) · [Next: Generics and Compiler Desugaring](12-generics-and-sugar.md)
