---
pagetitle: "13 Observing JIT Compilation and Inlining"
---

# 13 Observing JIT Compilation and Inlining

[English](13-jit-and-inlining.md) · [中文](../../tutorial/13-观察即时编译与方法内联.md)

A million source-level calls to a small method need not produce a million native call instructions. Inlining brings the callee's logic into the caller's compilation scope. Besides removing call overhead, it can enable constant propagation, range analysis, and other optimizations.

## Build the mechanism model

Interpretation lets execution start promptly while runtime profiling gathers information about hot code. HotSpot tiered compilation balances compilation cost against generated-code quality. Invocation counts and loop backedges can influence compilation; no fixed threshold is a language rule.

Inlining is not determined by source-line count alone. Bytecode size, receiver types, compilation budgets, and profile information all matter. Virtual calls may also inline: a stable receiver profile can support an optimization protected by checks and revocable assumptions.

When assumptions fail, compiled code may be invalidated or execution may deoptimize into a state that preserves program semantics. Deoptimization is part of speculative optimization, not evidence that the source program was rewritten incorrectly.

## Complete code

Save this as `Demo13.java`. It uses only the JDK 17 standard library and compiles independently.

```java
public class Demo13 {
    static volatile long sink;
    static int step(int value) { return value * 3 + 1; }
    static long batch(int count) {
        long sum = 0;
        for (int i = 0; i < count; i++) sum += step(i);
        return sum;
    }
    public static void main(String[] args) {
        for (int i = 0; i < 2000; i++) sink = batch(10000);
        long expected = 3L * 9999 * 10000 / 2 + 10000;
        if (sink != expected) throw new AssertionError(sink);
        System.out.println("sum=" + sink);
    }
}
```

## Compile and run

```bash
javac --release 17 -encoding UTF-8 Demo13.java
java Demo13
```

Expected output (default verification mode):

```text
sum=149995000
```

## Interpreting the evidence

Normal execution checks only the result. PrintCompilation and PrintInlining add many compilation events. Find Demo13::batch and Demo13::step, then examine the enclosing compilation tasks and inlining decisions. Rejection at one compilation level does not imply permanent rejection at every later level.

Compilation logs are not full native assembly. Determining whether a particular machine instruction exists requires appropriate disassembly support for the JDK and platform. We do not infer exact CPU instructions from logs that do not contain them.

A comparison with `-Xint` can confirm that interpretation produces the same calculation. Its elapsed-time difference covers the whole execution, however; it does not isolate method-call overhead.

Additional observation commands, run in the directory containing this chapter's class files:

```bash
java -XX:+UnlockDiagnosticVMOptions -XX:+PrintCompilation -XX:+PrintInlining Demo13 > jit.log 2>&1
```

## Why not optimize everything at startup?

A large program may contain many methods that this run never executes. Optimizing all of them immediately spends startup time and compilation resources without guaranteed benefit. Starting execution, observing hot code, and investing optimization effort selectively motivates JIT compilation.

In common HotSpot tiered configurations, C1 and C2 perform different compilation work. Level 0 denotes interpretation, levels 1–3 relate to C1 configurations, and level 4 usually denotes C2. Methods do not have to progress through every level in numerical order. Options, builds, and implementations can change the path.

Read these two lines from the retained log together:

```log
12    8 %     3       Demo13::batch @ 4 (25 bytes)
                              @ 11   Demo13::step (6 bytes)   inline
```

The percent sign identifies OSR compilation; `@ 4` identifies a bytecode entry position for an already running method. This helps explain how a hot loop can enter compiled code without starting the method again from its beginning. The indented line reports an inlining decision in that compilation context. Sizes 25 and 6 are bytecode bytes, not source lines or execution times.

**Why can warmup change results?** Initial loading, data access, interpretation, compilation, and steady-state execution can belong to different phases. Startup speed and sustained throughput are different goals. A slow first iteration is not automatically a GC problem, and one inline message does not establish global optimality.

## Exercises and self-checks

Add a few branches to step and examine the inlining decisions. Next compare receiver-type distributions while preserving the calculation and workload size. Keep the JDK, CPU, options, and raw logs with your explanation instead of retaining only a timing screenshot.

---

[Previous: Generics and Compiler Desugaring](12-generics-and-sugar.md) · [Contents](../../README.md) · [Next: Escape Analysis, Loop Optimization, and Vectorization](14-escape-analysis-and-loops.md)
