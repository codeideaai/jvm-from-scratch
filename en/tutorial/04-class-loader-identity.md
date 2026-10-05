---
pagetitle: "04 Isolating Same-Named Types with Class Loaders"
---

# 04 Isolating Same-Named Types with Class Loaders

[English](04-class-loader-identity.md) · [中文](../../tutorial/04-用类加载器隔离同名类型.md)

Two plugin classes can have the same fully qualified name and still be incompatible in a cast. Runtime type identity includes the defining class loader as well as the name. We will define the same bytes through two loaders and compare their Class objects.

## Build the mechanism model

The usual ClassLoader.loadClass flow first reuses already loaded classes and follows parent delegation. Shared APIs provided by a parent can therefore retain a common type identity. A loader's parent relationship is a delegation relationship, not necessarily inheritance between the Java classes implementing the loaders.

To isolate the identity question, this example calls defineClass through a small custom wrapper for one fixed class name. It is not a complete plugin loader and does not implement a general child-first strategy. Real plugin systems should isolate only selected business packages while loading shared interfaces through a common ancestor.

Unloading is also tied to loader reachability. Retained plugin instances, Class objects, thread context loaders, or registration entries may keep an old loader alive. Closing a loader does not mean that the JVM has already unloaded all its classes.

## Complete code

Save this as `Demo04.java`. It uses only the JDK 17 standard library and compiles independently.

```java
import java.io.InputStream;
public class Demo04 {
    public static class Payload {}
    static class Isolated extends ClassLoader {
        Class<?> define(byte[] bytes) {
            return defineClass("Demo04$Payload", bytes, 0, bytes.length);
        }
    }
    public static void main(String[] args) throws Exception {
        byte[] bytes;
        try (InputStream in = Demo04.class.getResourceAsStream("/Demo04$Payload.class")) {
            if (in == null) throw new IllegalStateException("class resource missing");
            bytes = in.readAllBytes();
        }
        Class<?> a = new Isolated().define(bytes);
        Class<?> b = new Isolated().define(bytes);
        if (a == b || !a.getName().equals(b.getName())) throw new AssertionError();
        System.out.println("same name=" + a.getName().equals(b.getName()));
        System.out.println("same type=" + (a == b));
        Object value = a.getConstructor().newInstance();
        try {
            b.cast(value);
            throw new AssertionError("cross-loader cast accepted");
        } catch (ClassCastException expected) {
            System.out.println("cast rejected");
        }
    }
}
```

## Compile and run

```bash
javac --release 17 -encoding UTF-8 Demo04.java
java Demo04
```

Expected output (default verification mode):

```text
same name=true
same type=false
cast rejected
```

## Interpreting the evidence

We are comparing whether two Class objects represent the same runtime type, not whether two ordinary objects are equal. Identical input bytes do not merge the type identities. The example avoids printing loader addresses or full exception messages so that incidental runtime details do not affect its expected output.

Payload has a public no-argument constructor and does not access private members of its enclosing class. Loading a nested class in isolation does not automatically establish cross-loader nestmate access; this experiment deliberately excludes that separate issue.

When an error effectively says “X cannot be cast to X,” recording both class loaders and code sources is often more helpful than repeatedly checking the names. The identity rule is specified in [JVMS 5.3](https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-5.html#jvms-5.3).

## Exercises and self-checks

Use the same cached Class for both a and b and confirm that the cast succeeds. Next, try defining the same name twice in one Isolated loader. Observe LinkageError and explain why identical bytes do not permit duplicate definition.

---

[Previous: Separating Class Loading from Initialization](03-class-initialization.md) · [Contents](../../README.md) · [Next: Overloading and Dynamic Dispatch](05-method-dispatch.md)
