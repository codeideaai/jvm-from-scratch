---
pagetitle: "14 Escape Analysis, Loop Optimization, and Vectorization"
---

# 14 Escape Analysis, Loop Optimization, and Vectorization

[English](14-escape-analysis-and-loops.md) · [中文](../../tutorial/14-理解逃逸分析循环优化与向量化.md)

Source code may allocate an object on every loop iteration while producing less heap allocation than expected. The final machine code may not need a complete object at all. “Allocation may disappear” and “the object is guaranteed to be stack allocated” are different claims.

## Build the mechanism model

Escape analysis examines whether an object can be accessed outside the scope under analysis. With sufficient information, scalar replacement can represent its fields as values without preserving a complete allocation. Analysis and optimization are separate: proving non-escape does not guarantee that every associated allocation is eliminated.

Inlining enlarges the scope visible to analysis, connecting constructors, accessors, and callers. Publishing an object to a static field or passing it to opaque code can inhibit optimization. A local new expression does not automatically imply non-escape.

Loop optimizations include moving invariant work, eliminating range checks, and unrolling. Automatic vectorization additionally depends on data dependencies, aliasing, element types, and the target instruction set. An intrinsic is special compiler handling for a known method's semantics. It is not ordinary line-by-line translation of the Java method body, nor a promise of identical instructions on all platforms.

## Complete code

Save this as `Demo14.java`. It uses only the JDK 17 standard library and compiles independently.

```java
public class Demo14 {
    record Point(int x, int y) {}
    static volatile long sink;
    static long calculate(int count) {
        long sum = 0;
        for (int i = 0; i < count; i++) {
            Point point = new Point(i, i + 1);
            sum += point.x() + point.y();
        }
        return sum;
    }
    public static void main(String[] args) {
        for (int i = 0; i < 3000; i++) sink = calculate(1000);
        if (sink != 1_000_000L) throw new AssertionError(sink);
        System.out.println("sum=" + sink);
    }
}
```

## Compile and run

```bash
javac --release 17 -encoding UTF-8 Demo14.java
java Demo14
```

Expected output (default verification mode):

```text
sum=1000000
```

## Interpreting the evidence

Point exists only to contribute to the current iteration's sum and is not stored in an external container, making it an interesting optimization candidate. Correct output does not prove scalar replacement occurred. It establishes the semantics both optimized and unoptimized execution must retain.

Run with the same heap configuration with and without `-XX:-DoEscapeAnalysis`, then inspect GC logs or allocation profiling such as the later JMH example. A difference may involve related optimizations, so the flag is not a perfectly isolated experiment on one mechanism.

For vectorization, a separate element-wise array addition experiment can be combined with generated code and hardware measurements. This Point example proves neither SIMD execution nor stack allocation. Some diagnostic printing flags require a debug JDK build and should not be presented as universally available product-build options.

Additional observation commands, run in the directory containing this chapter's class files:

```bash
java -Xms32m -Xmx32m -Xlog:gc:file=ea-on.log Demo14
java -Xms32m -Xmx32m -XX:-DoEscapeAnalysis -Xlog:gc:file=ea-off.log Demo14
```

## Exercises and self-checks

Publish each Point through a static volatile Point field and measure allocation again. Explain how publication changes the analysis scope. Then move i near int limits and check whether field addition overflows before widening; do not mistake changed integer semantics for an optimizer bug.

---

[Previous: Observing JIT Compilation and Inlining](13-jit-and-inlining.md) · [Contents](../../README.md) · [Next: Building Trustworthy Performance Experiments](15-performance-experiments.md)
