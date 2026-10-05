---
pagetitle: "12 Generics and Compiler Desugaring"
---

# 12 Generics and Compiler Desugaring

[English](12-generics-and-sugar.md) · [中文](../../tutorial/12-拆开泛型与编译器语法糖.md)

List<String> restricts element types in source code, so why can a raw reference insert an integer? Many generic checks occur at compile time. The class file uses erased descriptors and conversions where needed. This helps explain ClassCastException after framework deserialization or reflective injection.

## Build the mechanism model

Erasure does not mean that all generic information disappears. Calls use erased types and the compiler may insert checkcast; Signature attributes can retain declaration information for tools and reflection. A List object generally does not carry its String instantiation as a runtime rule checked on every insertion.

Generic overriding may require bridge methods. A subclass returns String while the erased parent signature returns Object, and the generated bridge connects the signatures. Framework scanners should consider isBridge and isSynthetic to avoid treating bridges as extra business endpoints.

Enhanced for loops, boxing, and try-with-resources are also compiler transformations. Calling a feature syntactic sugar does not make it free: inspect the resulting calls and allocations to understand its cost.

## Complete code

Save this as `Demo12.java`. It uses only the JDK 17 standard library and compiles independently.

```java
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
public class Demo12 {
    interface Source<T> { T get(); }
    static class TextSource implements Source<String> {
        public String get() { return "text"; }
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void main(String[] args) {
        long bridges = java.util.Arrays.stream(TextSource.class.getDeclaredMethods())
            .filter(Method::isBridge).count();
        System.out.println("bridges=" + bridges);
        List<String> values = new ArrayList<>();
        List raw = values;
        raw.add(123);
        try {
            String value = values.get(0);
            throw new AssertionError(value);
        } catch (ClassCastException expected) {
            System.out.println("heap pollution detected");
        }
        if (bridges != 1) throw new AssertionError(bridges);
    }
}
```

## Compile and run

```bash
javac --release 17 -encoding UTF-8 Demo12.java
java Demo12
```

Expected output (default verification mode):

```text
bridges=1
heap pollution detected
```

## Interpreting the evidence

raw.add accepts Object, so it does not reject Integer during insertion. The later values.get result must be cast to String, and that read fails. The exception site is not necessarily the pollution site. Trace backward to unchecked writes when investigating this kind of failure.

Disassemble TextSource to find its String-returning implementation and Object-returning bridge. They share a name but have different descriptors. Source-level overloading rules and class-file method representation are not interchangeable.

SuppressWarnings hides warnings; it does not establish runtime safety. Avoid raw types in ordinary business code. When framework internals need an unchecked conversion, keep it at a boundary that can validate the actual objects.

Additional observation commands, run in the directory containing this chapter's class files:

```bash
javap -c -v -p 'Demo12$TextSource'
javap -c -p Demo12
```

## Exercises and self-checks

Remove SuppressWarnings and compile with `javac -Xlint:unchecked Demo12.java`. Then insert a string through raw.add. Explain why disappearance of this exception does not restore the method's type-safety contract.

---

[Previous: Monitors and Compound Operations](11-monitors.md) · [Contents](../../README.md) · [Next: Observing JIT Compilation and Inlining](13-jit-and-inlining.md)
