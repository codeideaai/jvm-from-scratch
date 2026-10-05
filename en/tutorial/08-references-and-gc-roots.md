---
pagetitle: "08 From Object References to GC Roots"
---

# 08 From Object References to GC Roots

[English](08-references-and-gc-roots.md) · [中文](../../tutorial/08-从对象引用走到垃圾回收根.md)

Removing a cache entry does not immediately reduce heap usage. Three questions are involved: is the object still referenced, is it eligible for collection, and when will a collector actually reclaim it? Separate these questions before using a System.gc call to speculate about a leak.

## Build the mechanism model

Reachability analysis traverses object relationships starting from GC roots. Live references in executing threads and references through static state can keep objects alive. Cycles alone do not defeat this analysis: an entire cycle unreachable from the roots can become eligible for reclamation.

HotSpot object layout typically involves a header, instance fields, and alignment space. Reference width, compressed class pointers, and array-length information affect size. Adding field widths does not give total heap cost, and no single header size is a universal JVM guarantee.

WeakReference permits a referent to be cleared when weakly reachable, but it is not an immediate lifecycle notification protocol. ReferenceQueue reports enqueued reference objects. Explicit clear and enqueue let us test queue-handling behavior without depending on GC timing.

## Complete code

Save this as `Demo08.java`. It uses only the JDK 17 standard library and compiles independently.

```java
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
public class Demo08 {
    public static void main(String[] args) {
        Object strong = new Object();
        ReferenceQueue<Object> queue = new ReferenceQueue<>();
        WeakReference<Object> weak = new WeakReference<>(strong, queue);
        System.out.println("same referent=" + (weak.get() == strong));
        weak.clear();
        System.out.println("cleared=" + (weak.get() == null));
        System.out.println("queue empty=" + (queue.poll() == null));
        System.out.println("enqueued=" + weak.enqueue());
        System.out.println("same reference=" + (queue.poll() == weak));
        java.lang.ref.Reference.reachabilityFence(strong);
    }
}
```

## Compile and run

```bash
javac --release 17 -encoding UTF-8 Demo08.java
java Demo08
```

Expected output (default verification mode):

```text
same referent=true
cleared=true
queue empty=true
enqueued=true
same reference=true
```

## Interpreting the evidence

reachabilityFence keeps strong reachable until the end of the experiment. The observed clearing therefore comes from our explicit clear call; it does not claim that the collector reclaimed the object. clear does not itself enqueue the reference, which explains the third line. enqueue places the WeakReference object on the queue.

A weak-reference cache must also consume its queue and remove stale index entries. Otherwise, keys and reference wrappers can accumulate even after referents disappear. A leak investigation must follow ownership relationships, not merely count objects of a particular class.

Leaving a source-level scope does not map exactly to the end of a live reference. JIT liveness analysis may shorten its lifetime. This distinction matters when Java reachability controls the lifetime of native resources.

## Establish strong reachability before weak references

If first and alias both point to A, setting first=null leaves the path through alias intact. If a static cache retains A, returning from a method does not remove the cache's path. Ask which live starting points can reach it, not how many variable names remain in the source.

This WeakReference experiment checks the wrapper's clear and enqueue APIs. It does not cover root discovery or automatic weak-reference processing timing. If you still cannot explain why objects become collectible, work through [F04's reference graph](../foundations/F04-gc-from-graphs.md) first, then see weak references as a change to how particular edges are processed.

**Self-check:** should the JVM guess that an object in an unbounded cache is no longer useful and discard it? Do not rely on that; strong reachability may retain it, and eviction is the application's responsibility. Conversely, becoming unreachable does not guarantee returning memory to the OS in the next millisecond. Eligibility, actual collection, and returning process memory are separate events.

## Exercises and self-checks

Add a second strong reference called alias. Clearing strong does not make the referent unreachable while alias still retains it. Draw roots → container → entry → business object and identify the edge that must disappear. A separate real-GC experiment should allow a timeout: failure to observe collection is not evidence that the GC rules are wrong.

---

[Previous: Connecting Reflection, Method Handles, and Lambdas](07-reflection-method-handles-lambdas.md) · [Contents](../../README.md) · [Next: Observing Allocation Pressure with GC Logs](09-gc-logs.md)
