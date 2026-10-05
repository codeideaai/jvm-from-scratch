---
pagetitle: "03 Separating Class Loading from Initialization"
---

# 03 Separating Class Loading from Initialization

[English](03-class-initialization.md) · [中文](../../tutorial/03-分清类加载与初始化.md)

A configuration class has static initialization code, but reading one of its constants does not execute it. The missing output may mean that initialization never occurred. We will distinguish making a class available from executing its static initialization logic.

## Build the mechanism model

Loading establishes a runtime representation of a class. Linking includes verification, preparation, and resolution. Preparation establishes static-field storage and initial values, with special treatment for compile-time constants. Initialization executes class-initialization logic. Resolution may be deferred, so these terms do not describe one uninterrupted pipeline that every class must follow immediately.

A reference to a compile-time constant may be replaced by a literal during compilation. That use need not initialize the declaring class. Creating an instance, invoking a declared static method, or accessing a declared nonconstant static field are common initialization triggers.

The JVM coordinates concurrent class initialization, and successful initialization happens once. Failure also leaves an erroneous state: fixing an external condition does not automatically make later access rerun the entire static initializer.

## Complete code

Save this as `Demo03.java`. It uses only the JDK 17 standard library and compiles independently.

```java
public class Demo03 {
    static class Config {
        static final int PORT = 8080;
        static int timeout = initialize();
        static int initialize() {
            System.out.println("Config initialized");
            return 30;
        }
    }
    public static void main(String[] args) throws Exception {
        System.out.println("constant=" + Config.PORT);
        Class.forName("Demo03$Config", false, Demo03.class.getClassLoader());
        System.out.println("loaded without initialization");
        System.out.println("timeout=" + Config.timeout);
        System.out.println("timeout again=" + Config.timeout);
    }
}
```

## Compile and run

```bash
javac --release 17 -encoding UTF-8 Demo03.java
java Demo03
```

Expected output (default verification mode):

```text
constant=8080
loaded without initialization
Config initialized
timeout=30
timeout again=30
```

## Interpreting the evidence

The value 8080 can be incorporated into the caller. The false argument to the three-argument Class.forName explicitly prevents that call from triggering initialization. Only the read of timeout causes initialize to print its message. The second read uses the existing value.

The single-argument `Class.forName(String)` requests initialization by default. When investigating side effects during framework scanning, first identify the overload that is actually used.

If Parent declares a static field and Child merely inherits it, writing Child.field does not necessarily initialize Child. Reason about the declaring class rather than the name before the dot. See [JVMS Chapter 5](https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-5.html).

Additional observation commands, run in the directory containing this chapter's class files:

```bash
javap -c -p Demo03
javap -c -p 'Demo03$Config'
```

## Place the phases back into this program

static makes a field class-related; accessing it does not require constructing Config first. final constrains assignment, but static final alone does not establish a compile-time constant. The type and initializer must satisfy constant-variable rules. PORT uses a primitive type and constant expression; timeout is computed by executing a method.

| Observation point | What it establishes | What it does not establish |
| --- | --- | --- |
| After compiling Demo03 | The caller can embed PORT's constant | Config's static code has executed |
| After three-argument forName with false | Loading was requested without requesting initialization | timeout's business value has been computed |
| First read of timeout | Required initialization runs initialize and produces 30 | Preparation already executed that business method |
| Another read of timeout | Initialized state is reused | Every access reruns initialize |

The default zero associated with preparation differs from the source-level assignment producing 30. Our println occurs in initialization. These trigger rules do not imply that ordinary external access is guaranteed to observe the intermediate zero.

**Common question:** failure to find a class and failure inside a found class's initializer are different paths. Do not keep changing classpath for every class-related failure. Inspect the exception type and cause to distinguish missing bytes from failed initialization logic.

## Exercises and self-checks

Change PORT to Integer or initialize it through a method call, then examine the output order. Make initialize invoke a method that always fails and compare the first and subsequent access errors. Perform this intentionally failing experiment in an isolated example, not a live configuration class.

---

[Previous: Primitive Types and Numeric Boundaries](02-primitive-types.md) · [Contents](../../README.md) · [Next: Isolating Same-Named Types with Class Loaders](04-class-loader-identity.md)
