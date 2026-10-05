---
pagetitle: "05 Overloading and Dynamic Dispatch"
---

# 05 Overloading and Dynamic Dispatch

[English](05-method-dispatch.md) · [中文](../../tutorial/05-区分重载与动态分派.md)

Which method runs when a service stores a subclass instance in a variable declared with its parent type? Answer in two stages: select a signature, then select the implementation for that signature. Confusing overloading with overriding leads to incorrect predictions about framework callbacks and interface-based code.

## Build the mechanism model

Overload selection happens primarily at compile time according to declared types and applicability rules. An overridden instance method can then be dispatched according to the receiver's actual type. The runtime type of an argument does not automatically replace the overload signature already selected by the compiler.

Common bytecode instructions include invokestatic for static calls, invokeinterface for interface calls, invokevirtual for ordinary virtual calls, and invokespecial for constructors and certain special calls. invokedynamic delegates call-site linking to a bootstrap mechanism. Inspect the actual call site with javap rather than guessing from source keywords alone.

Dynamic dispatch is a semantic requirement. It does not require traversing an entire inheritance tree on every execution. The JIT may optimize using type information while preserving observable behavior.

## Complete code

Save this as `Demo05.java`. It uses only the JDK 17 standard library and compiles independently.

```java
public class Demo05 {
    static class Parent {
        String name() { return "parent"; }
    }
    static class Child extends Parent {
        @Override String name() { return "child"; }
    }
    static String select(Parent value) { return "Parent overload"; }
    static String select(Child value) { return "Child overload"; }
    public static void main(String[] args) {
        Parent value = new Child();
        System.out.println(select(value));
        System.out.println(value.name());
        System.out.println(select((Child) value));
        if (!value.name().equals("child")) throw new AssertionError();
    }
}
```

## Compile and run

```bash
javac --release 17 -encoding UTF-8 Demo05.java
java Demo05
```

Expected output (default verification mode):

```text
Parent overload
child
Child overload
```

## Interpreting the evidence

The first line selects the Parent overload because value is declared as Parent. The second invokes Child's implementation of name. The explicit cast in the third line changes the expression's static type, allowing a different overload to be selected.

Inspect the descriptors following the select calls in `javap -c -p Demo05`. They contain either Parent or Child directly. There is no instruction asking the runtime to choose again among every source-level overload.

Fields do not follow the overriding rules for instance methods. Replacing name with same-named fields creates a different experiment: field selection depends on the declared type. Static methods likewise do not use this example's instance dispatch semantics.

Additional observation commands, run in the directory containing this chapter's class files:

```bash
javap -c -p Demo05
```

## Exercises and self-checks

Add a select overload accepting Object, declare the variable as Object, and predict the result. Then have Child implement an interface, call through an interface reference, and find invokeinterface. Explain whether JIT inlining may change any of these observable selections.

---

[Previous: Isolating Same-Named Types with Class Loaders](04-class-loader-identity.md) · [Contents](../../README.md) · [Next: Exception Tables and Resource Cleanup](06-exceptions-and-resources.md)
