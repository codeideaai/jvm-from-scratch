---
pagetitle: "17 A Minimal Java Agent for Object Sizes"
---

# 17 A Minimal Java Agent for Object Sizes

[English](17-java-agent.md) · [中文](../../tutorial/17-编写最小JavaAgent观察对象尺寸.md)

Monitoring tools can receive Instrumentation before the application's main method runs. We will build an agent that only observes: verify startup order and query shallow object size. This separates agent loading from the additional complexity of rewriting bytecode.

## Build the mechanism model

A startup agent identifies its entry point through Premain-Class in a JAR manifest and is loaded with -javaagent. Its entry point can receive Instrumentation for transformer registration, size queries, and other supported operations. Dynamic attachment uses a different entry point and capability conditions and is outside this experiment.

getObjectSize reports an implementation-specific approximation of shallow size. An array's own storage is included, but objects referenced by its elements generally are not. Traversing and deduplicating all reachable objects to measure a graph is a different task.

A real ClassFileTransformer must account for names, loaders, retransformation conditions, and bytecode verification. Changing a method body and arbitrarily changing a loaded class's structure are different capabilities. Attaching an agent does not imply permission to add or remove any field at will.

## Complete code

Save this as `Demo17.java`. It uses only the JDK 17 standard library and compiles independently.

```java
import java.lang.instrument.Instrumentation;
public class Demo17 {
    private static volatile Instrumentation instrumentation;
    public static void premain(String args, Instrumentation inst) {
        instrumentation = inst;
        System.out.println("agent ready");
    }
    public static void main(String[] args) {
        Instrumentation inst = instrumentation;
        if (inst == null) throw new IllegalStateException("run with -javaagent");
        long small = inst.getObjectSize(new byte[0]);
        long large = inst.getObjectSize(new byte[1024]);
        if (small <= 0 || large <= small) throw new AssertionError("unexpected sizes");
        System.out.println("positive shallow size=" + (small > 0));
        System.out.println("larger array=" + (large > small));
    }
}
```

## Compile and run

```bash
javac --release 17 -encoding UTF-8 Demo17.java
printf 'Premain-Class: Demo17\n\n' > manifest.mf
jar --create --file demo17-agent.jar --manifest manifest.mf Demo17.class
java -javaagent:demo17-agent.jar Demo17
```

Expected output (default verification mode):

```text
agent ready
positive shallow size=true
larger array=true
```

## Interpreting the evidence

The example does not hard-code object sizes such as 16 or 24, so it does not turn one pointer-compression and alignment configuration into a universal rule. You can print small and large temporarily and record the VM options alongside them.

The agent and application use one class to keep the experiment self-contained. Production agents often use separate modules and minimize premain dependencies to avoid prematurely loading many application classes.

This chapter verifies premain and a size query; it does not implement instrumentation. Injecting timing into every method would also require exceptional exits, recursion, transformer dependencies, and repeated transformation to be handled. Probes can affect inlining and performance, so account for observer overhead when interpreting results.

## Exercises and self-checks

Run without -javaagent and confirm that the program fails explicitly instead of reporting a fictitious zero-byte size. Compare the shallow size of Object[] with the sizes of byte[] elements it references. Explain why enlarging an element array does not change the outer reference array's length.

---

[Previous: Connecting Thread, Heap, and JFR Evidence](16-diagnostics-and-jfr.md) · [Contents](../../README.md) · [Next: Case Study: Diagnosing Unbounded Cache Growth](18-bounded-cache.md)
