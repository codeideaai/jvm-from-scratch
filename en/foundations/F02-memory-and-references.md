---
pagetitle: "F02 Draw Memory Areas and Object References"
---

# F02 Draw Memory Areas and Object References

[English](F02-memory-and-references.md) · [中文](../../foundations/F02-画清内存区域与对象引用.md)

## Distinguish a box from a reference to it

Read `Box first = new Box()` as two actions: create a Box object and assign a reference to first. A reference is a value the program uses to access an object, not a public address on which arbitrary arithmetic is allowed. `Box alias = first` copies that value; it does not copy the Box fields or call a second constructor.

Name the original object A: first → A and alias → A. Changing A.value is visible through both paths in this single-threaded example. Reassigning first does not redirect alias. Cross-thread visibility requires additional synchronization and is a later topic.

## A memory map organized by questions

| Area or concept | Question it helps answer | Common misconception |
| --- | --- | --- |
| Per-thread PC and JVM stack | Where is this thread, and what are its active calls? | All threads share one invocation stack |
| Frame | What parameters, locals, and intermediate state belong to one invocation? | One frame per class |
| Heap | How are instances and arrays stored and reclaimed? | The heap is all process memory |
| Method area, a specification concept | How are class-level structures and runtime constant pools represented? | It always means permanent generation |
| HotSpot metaspace | How does HotSpot manage class metadata in native memory? | Every static value resides here |
| Code cache, native allocations, thread stacks | Why can process memory exceed a modest heap? | Xmx caps all of them |

Do not force every Java value into a permanent physical box. A local can contain a reference to a heap object, while a field can contain a reference to another object. JIT compilation can place values in registers or eliminate allocations. This is a model of execution and retention, not an exact physical map.

The **JVM runtime data areas** differ from the **Java Memory Model, JMM**. The former describes execution structures; the latter constrains multithreaded observations. Chapter 10's happens-before belongs to the latter.

## Complete code

Save as `Foundation02.java`.

```java
public class Foundation02 {
    static class Box { int value; }
    static void change(Box copy) {
        copy.value = 20;
        copy = new Box();
        copy.value = 99;
    }
    public static void main(String[] args) {
        Box first = new Box();
        first.value = 10;
        Box alias = first;
        change(first);
        if (first != alias || first.value != 20) throw new AssertionError();
        System.out.println("same object=" + (first == alias));
        System.out.println("value=" + first.value);
    }
}
```

## Compile and run

```bash
javac --release 17 -encoding UTF-8 Foundation02.java
java -cp . Foundation02
```

Expected output:

```text
same object=true
value=20
```

## Draw three moments in one call

Before change, first and alias in main both refer to A, whose value is 10. Entering change creates a new invocation whose copy parameter receives a copy of first's reference value, also pointing to A.

`copy.value = 20` follows that reference and modifies A. `copy = new Box()` creates B and redirects only change's own copy variable. The caller's first and alias are unchanged. The final assignment writes 99 into B; the printed value comes from A and is therefore 20.

When change returns, its invocation state is no longer needed. B may become eligible for collection if no other reachable path exists, while A remains in use through main. No GC is requested here, and nothing proves B has been reclaimed. **Returning from a call** and **reclaiming an object** are different events.

Java always passes arguments by value. Primitive arguments copy primitive values; reference arguments copy reference values. Saying “pass an object” can mistakenly suggest that a callee can rebind its caller's variable. This example makes the distinction explicit.

**Exercises and answers:** removing `copy.value = 20` leaves the printed value at 10. Creating alias with `new Box()` makes identity comparison false and breaks the original assertion. Equal field contents do not imply `==`: identity and content equality are separate questions.

Before main Chapter 08, be able to distinguish deleting one variable from removing every reachable path to an object.

---

[Previous: Run Your First Java Program](F01-first-program.md) · [Contents](../../README.md) · [Next: Trace Frames and Read a Class File](F03-read-bytecode.md)
