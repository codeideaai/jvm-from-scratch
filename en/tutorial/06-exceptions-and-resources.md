---
pagetitle: "06 Exception Tables and Resource Cleanup"
---

# 06 Exception Tables and Resource Cleanup

[English](06-exceptions-and-resources.md) · [中文](../../tutorial/06-沿异常表理解资源关闭.md)

If processing fails and closing its resources also fails, which exception should reach the caller? Throwing a cleanup exception directly from a handwritten finally block can hide the original business failure. try-with-resources defines a precise order and suppression policy.

## Build the mechanism model

The JVM searches the exception table using the position of the failure and the handler's matching rules. A match transfers control to the handler. Otherwise, the current method exits abruptly and the search continues in its caller. The table describes protected ranges and handler locations; it is not a catch test inserted before every ordinary instruction.

The compiler expands try-with-resources into resource-management control flow. Resources close in reverse declaration order. If the body has already thrown, cleanup exceptions are added as suppressed exceptions to that primary failure. If the body succeeds and cleanup fails, a cleanup exception becomes the primary one.

Finally normally executes, but it is not a persistence guarantee against forced process termination or VM crashes. In particular, returning from finally can replace an earlier return value or exception.

## Complete code

Save this as `Demo06.java`. It uses only the JDK 17 standard library and compiles independently.

```java
public class Demo06 {
    static class Resource implements AutoCloseable {
        final String name;
        Resource(String name) { this.name = name; }
        @Override public void close() {
            System.out.println("close " + name);
            throw new IllegalStateException("close-" + name);
        }
    }
    public static void main(String[] args) {
        try (Resource a = new Resource("A"); Resource b = new Resource("B")) {
            throw new IllegalArgumentException("business");
        } catch (IllegalArgumentException e) {
            System.out.println("primary=" + e.getMessage());
            for (Throwable other : e.getSuppressed()) {
                System.out.println("suppressed=" + other.getMessage());
            }
            if (e.getSuppressed().length != 2) throw new AssertionError(e);
        }
    }
}
```

## Compile and run

```bash
javac --release 17 -encoding UTF-8 Demo06.java
java Demo06
```

Expected output (default verification mode):

```text
close B
close A
primary=business
suppressed=close-B
suppressed=close-A
```

## Interpreting the evidence

Both resources close. Failure while closing B does not prevent the generated control flow from closing A. The body's IllegalArgumentException remains primary, preserving the original business failure.

Use detailed javap output to find Exception table and addSuppressed. A protected range includes its from offset and excludes its to offset. Offsets depend on the generated class, so inspect your own file rather than memorizing numbers.

Logging only getMessage loses stack information, causes, and suppressed exceptions. Application logging should retain the complete Throwable. This example prints messages only to provide deterministic teaching assertions.

Additional observation commands, run in the directory containing this chapter's class files:

```bash
javap -c -v Demo06
```

## Exercises and self-checks

Remove the exception in the body and catch RuntimeException instead. Which cleanup exception becomes primary? Then make A's constructor throw. Explain why B is never created and why a resource whose initialization did not complete is not added to the cleanup sequence.

---

[Previous: Overloading and Dynamic Dispatch](05-method-dispatch.md) · [Contents](../../README.md) · [Next: Connecting Reflection, Method Handles, and Lambdas](07-reflection-method-handles-lambdas.md)
