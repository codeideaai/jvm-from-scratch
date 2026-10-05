---
pagetitle: "07 Connecting Reflection, Method Handles, and Lambdas"
---

# 07 Connecting Reflection, Method Handles, and Lambdas

[English](07-reflection-method-handles-lambdas.md) · [中文](../../tutorial/07-连接反射方法句柄与Lambda.md)

Frameworks invoke methods selected through configuration, while lambdas let programs pass behavior as values. Both involve indirect invocation, but at different levels. We will call the same addition function through reflection, a method handle, and a lambda, then distinguish what this experiment establishes from what it cannot prove.

## Build the mechanism model

A reflective Method describes method metadata. Its invoke API uses Object-shaped arguments and results, adapting primitive values as necessary. A MethodHandle has a specific MethodType. invokeExact requires the call-site type to match exactly; invoke permits defined adaptations.

javac commonly represents lambda creation using invokedynamic, with linking described by BootstrapMethods. invokedynamic is a programmable linkage mechanism, not a request to repeat reflective lookup on every call. A lambda also need not allocate a new object on every evaluation or be an ordinary anonymous inner class with a stable name.

This experiment compares behavior, not speed. Caching of reflective metadata, inlining opportunities, argument shapes, and JDK version can all affect performance. A historical invocation-count threshold is not a Java language guarantee.

## Complete code

Save this as `Demo07.java`. It uses only the JDK 17 standard library and compiles independently.

```java
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.IntBinaryOperator;
public class Demo07 {
    public static int add(int a, int b) { return a + b; }
    public static int fail() { throw new IllegalArgumentException("business"); }
    public static void main(String[] args) throws Throwable {
        Method method = Demo07.class.getMethod("add", int.class, int.class);
        MethodHandle handle = MethodHandles.lookup().findStatic(
            Demo07.class, "add", MethodType.methodType(int.class, int.class, int.class));
        IntBinaryOperator lambda = Demo07::add;
        System.out.println("reflection=" + method.invoke(null, 2, 3));
        System.out.println("handle=" + (int) handle.invokeExact(2, 3));
        System.out.println("lambda=" + lambda.applyAsInt(2, 3));
        try {
            Demo07.class.getMethod("fail").invoke(null);
            throw new AssertionError("exception lost");
        } catch (InvocationTargetException e) {
            if (!(e.getCause() instanceof IllegalArgumentException)) throw e;
            System.out.println("cause=" + e.getCause().getMessage());
        }
    }
}
```

## Compile and run

```bash
javac --release 17 -encoding UTF-8 Demo07.java
java Demo07
```

Expected output (default verification mode):

```text
reflection=5
handle=5
lambda=5
cause=business
```

## Interpreting the evidence

The int cast before invokeExact helps determine the call site's return type. Removing it does not mean the JVM will infer every missing adaptation. An incompatible signature produces WrongMethodTypeException, which differs from an exception thrown by the target function itself.

Reflection wraps a target failure in InvocationTargetException. A framework that adds another abstraction layer should preserve its cause. Method-handle invocation does not use that reflective wrapper for target exceptions.

Run `javap -v -p Demo07` and look for InvokeDynamic and BootstrapMethods. Distinguish LambdaMetafactory entries from string-concatenation bootstrap entries. Merely finding invokedynamic does not establish that the source construct was a lambda. The reflection implementation changed in JDK 18; see [JEP 416](https://openjdk.org/jeps/416).

Additional observation commands, run in the directory containing this chapter's class files:

```bash
javap -v -p Demo07
```

## Separate three levels on the first reading

Begin with the ordinary static call add(2, 3), which produces 5. Reflection's getMethod locates a descriptor using a name and parameter types; invoke performs the call. lookup/findStatic creates a precisely typed handle; invokeExact calls it. `Demo07::add` produces behavior conforming to IntBinaryOperator. The shared business function does not make these representations identical.

| Path | First step | Second step |
| --- | --- | --- |
| Reflection | Locate and retain a Method | Invoke the target through it |
| Method handle | Lookup with a MethodType | Invoke according to the call-site type |
| Lambda/method reference | Establish a functional-interface value | Invoke its interface operation |

Why is reflection's receiver argument null here? A static add does not require a Demo07 instance receiver. Why does returning 5 not establish equal speed? It checks behavior, not controlled costs of lookup, adaptation, or optimization.

On a first pass, learn reflective lookup and exception wrapping. Revisit invokedynamic and BootstrapMethods later rather than letting one advanced term block the memory and GC chapters.

## Exercises and self-checks

Change the first invokeExact argument to 2L and observe exact type checking. Then use handle.asType to request an explicit adaptation and investigate which conversions are supported. Do not turn a single run's elapsed time into a ranking of reflection and lambdas.

---

[Previous: Exception Tables and Resource Cleanup](06-exceptions-and-resources.md) · [Contents](../../README.md) · [Next: From Object References to GC Roots](08-references-and-gc-roots.md)
