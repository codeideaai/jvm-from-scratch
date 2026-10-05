---
pagetitle: "19 Extension Mechanisms: Compile-Time Generation and Native Execution"
---

# 19 Extension Mechanisms: Compile-Time Generation and Native Execution

[English](19-extension-mechanisms.md) · [中文](../../tutorial/19-扩展篇编译期生成与本地执行.md)

We have connected javac, class files, JIT compilation, and agents. Several extension mechanisms are easy to conflate: annotation processors generate code, JNI calls native code, Graal compiles code, Truffle supports language implementations, and Native Image creates a native program at build time. They operate at different stages and preserve different contracts.

Consider an expression-evaluation service: users supply rules, the service loads them, and then it evaluates them. This chapter examines where these technologies might fit and where requirements conflict with their boundaries. It does not claim to have run those external toolchains.

## Annotation processors: moving repetitive work to compilation

Suppose each business model needs a field index. Runtime reflection could enumerate fields, while an annotation processor could generate an index class during javac compilation. Its input is the source-level element model; outputs can include source files, resources, and diagnostics.

JSR 269 processing can involve several rounds. Generated sources participate in later rounds, and a final round completes processing. Use Filer for managed output rather than bypassing the compiler with arbitrary file writes. Repeated generation of the same path can cause conflicts. A processor's boolean result indicates whether it claims the relevant annotations, not whether processing succeeded.

Annotation processing is not a supported general API for rewriting existing classes. Modifying compiler ASTs through internal APIs is a separate approach with different compatibility risks. Chapter 15's JMH project uses annotation processing to generate benchmark infrastructure. A dependency on jmh-core alone generally does not create discoverable benchmarks.

For the expression service, a processor can generate access code for models known at compile time. Rules entered by users later are not part of that javac invocation and are not automatically processed by the existing build-time processor.

## JNI: crossing the managed-code boundary

Suppose the service needs an existing C library. A native Java declaration is linked to an implementation through loading and naming conventions or registration. JNIEnv is thread-associated, so a pointer obtained for one thread is not a universal handle for other threads. Native code must manage local references, global references, and exception state.

Local references have a lifetime. Long-term retention of a Java object requires the appropriate global-reference mechanism and timely release. Global references affect reachability: leaking one can keep Java heap objects alive. Long native loops that create many local references also need deliberate management rather than waiting indefinitely for method return.

Passing an array to native code does not create a permanently valid raw pointer. APIs may copy or pin data and require a paired release. Critical array access has restrictions on blocking and other calls that must be respected.

Native code does not receive all Java array-bound and memory-safety protections. A native-library crash can terminate the process. For tiny arithmetic operations, measure boundary crossing and conversion costs rather than comparing only the time inside a C function.

## Graal and Truffle: compiler and language framework

Graal is compiler technology. A compiler written in Java need not produce Java bytecode. Truffle provides a framework for language interpreters and cooperates with optimizing compilation to specialize programs using interpreter structure and execution information.

For the expression service, first define the semantics of an expression AST interpreter, then consider whether Truffle fits the language implementation. This is different from compiling an ordinary Java service into a native executable.

Optimizations depend on stable assumptions. New types or behavior can invalidate prior specialization. When reading about an optimizing interpreter, ask which state is treated as constant, which inputs remain dynamic, and how execution preserves semantics after assumptions fail.

## Native Image: build-time reachability and dynamic behavior

Native Image analyzes reachable code at build time and generates a native program with runtime support for facilities such as memory management. It is neither a packaging of every JVM capability unchanged nor a conversion into a program that no longer needs garbage collection. See [Native Image Basics](https://www.graalvm.org/latest/reference-manual/native-image/basics/) for its model and constraints.

If the service constructs objects through string-based reflective names, ordinary control-flow analysis may not discover the targets. Appropriate reachability metadata may be required. Resources, JNI, and proxies likewise need version-specific handling. Success on a regular JVM does not establish full behavior in a native image.

Initialization timing is another boundary. Moving environment-sensitive configuration reads to image build time can freeze build-machine values into the artifact. Decide which classes may initialize during the build and which must wait until execution.

The experimental HotSpot jaotc path is distinct from Native Image. JDK 17 removed the experimental jaotc tool and the Graal JIT compiler then bundled with the JDK; see [JEP 410](https://openjdk.org/jeps/410). Historical jaotc commands are not required experiments for this JDK 17 series.

## Choosing a mechanism for the service

| Requirement | Mechanism to investigate | Boundary to validate first |
| --- | --- | --- |
| Access code for fixed models | Annotation processing | Multiple rounds, incremental builds, useful diagnostics |
| Reusing a native library | JNI | Data lifetime, exceptions, thread contracts |
| Implementing an expression language | Truffle | Interpreter semantics and invalidated specialization |
| Changing startup and deployment characteristics | Native Image | Reflection and resource metadata, initialization, build cost |
| Studying hot-code compilation | Graal/JIT | Distribution, platform support, measurement conditions |

These are candidates to investigate, not unconditional recommendations. Determine whether the bottleneck concerns startup, steady-state throughput, live memory, or a native dependency before selecting tools. For ordinary Java rule evaluation, the bytecode, JIT, GC, and JFR methods already established can answer many questions.

## Design exercises

Write two requirement sets: one permits uploading arbitrary JARs for dynamic loading, while the other fixes all rules before release. Explain their different implications for build-time reachability. List three settings that must be read at runtime and describe the error caused by reading them during a build.

Add one external toolchain at a time, recording its version, commands, and minimum functional regression checks. Do not change compiler, collector, image format, and business algorithm simultaneously and then attribute the combined result to just one change.

---

[Previous: Case Study: Diagnosing Unbounded Cache Growth](18-bounded-cache.md) · [Contents](../../README.md)
