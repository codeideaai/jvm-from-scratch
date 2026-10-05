---
pagetitle: "18 Case Study: Diagnosing Unbounded Cache Growth"
---

# 18 Case Study: Diagnosing Unbounded Cache Growth

[English](18-bounded-cache.md) · [中文](../../tutorial/18-综合实战定位无界缓存增长.md)

A high cache hit rate does not establish memory safety. If key cardinality grows continuously without eviction, a Map turns short-lived results into long-lived objects. This chapter reproduces growth with a finite workload and adds a bounded access-order cache.

## Build the mechanism model

Start with a hypothesis: request volume grows, and post-collection live data grows too, suggesting long-lived retention. Seeing many byte arrays is insufficient; follow the references back to their container and keys. Increasing Xmx can delay failure without changing growth as a function of key count.

We compare HashMap, which retains all distinct keys here, with access-order LinkedHashMap using removeEldestEntry to bound the retained entries. An entry-count limit is not an exact byte budget. Real caches also need value-size policies, expiration, and concurrent loading behavior.

This is not a production concurrent cache. It is single-threaded, with explicit limits on request count and value size, so the mechanism is visible without triggering an out-of-memory failure.

## Complete code

Save this as `Demo18.java`. It uses only the JDK 17 standard library and compiles independently.

```java
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
public class Demo18 {
    static class Bounded extends LinkedHashMap<Integer, byte[]> {
        private final int limit;
        Bounded(int limit) {
            super(16, 0.75f, true);
            if (limit <= 0) throw new IllegalArgumentException("limit");
            this.limit = limit;
        }
        @Override protected boolean removeEldestEntry(Map.Entry<Integer, byte[]> eldest) {
            return size() > limit;
        }
    }
    static void fill(Map<Integer, byte[]> cache) {
        for (int i = 0; i < 2000; i++) cache.put(i, new byte[1024]);
    }
    public static void main(String[] args) {
        Map<Integer, byte[]> unlimited = new HashMap<>();
        Bounded bounded = new Bounded(128);
        fill(unlimited); fill(bounded);
        if (unlimited.size() != 2000 || bounded.size() != 128) throw new AssertionError();
        System.out.println("unbounded=" + unlimited.size());
        System.out.println("bounded=" + bounded.size());
        Bounded order = new Bounded(2);
        order.put(1, new byte[1]); order.put(2, new byte[1]);
        order.get(1);
        order.put(3, new byte[1]);
        if (order.containsKey(2) || !order.containsKey(1)) throw new AssertionError();
        System.out.println("least recently used evicted=true");
    }
}
```

## Compile and run

```bash
javac --release 17 -encoding UTF-8 Demo18.java
java Demo18
```

Expected output (default verification mode):

```text
unbounded=2000
bounded=128
least recently used evicted=true
```

## Interpreting the evidence

The first assertions establish the capacity invariant. The final scenario tests eviction order: after accessing 1, key 2 is the least recently used. Checking only size could miss an accidental FIFO implementation.

The output reports entry counts, not a fabricated measurement of precise heap bytes. Arrays, boxed keys, Map nodes, and backing tables each have costs. Relate the preceding size experiment to this reference graph, while using dedicated heap analysis for more complex retained graphs.

As a follow-up, add an observation window and gather thread and heap evidence using Chapter 16. Then change only the cache policy and repeat the key distribution. Acceptance should include correct eviction, equivalent results, bounded live data, and acceptable latency. Fewer Full GCs alone is insufficient.

## Exercises and self-checks

Add a rejection check for a negative capacity. Next vary value size by key and explain the weakness of an entry-count limit. Design a byte-budget policy and account for the old weight when replacing an existing key. Finally, write a short incident report with symptom, hypothesis, evidence, repair, and regression criteria.

---

[Previous: A Minimal Java Agent for Object Sizes](17-java-agent.md) · [Contents](../../README.md) · [Next: Extension Mechanisms: Compile-Time Generation and Native Execution](19-extension-mechanisms.md)
