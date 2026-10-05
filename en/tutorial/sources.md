---
pagetitle: "Technical References"
---

# Technical References

[English](sources.md) · [中文](../../tutorial/技术文档与版本说明.md)

## Specifications and official references

- [JVMS 17](https://docs.oracle.com/javase/specs/jvms/se17/html/index.html): runtime types, class loading, and bytecode semantics.
- [JLS 17](https://docs.oracle.com/javase/specs/jls/se17/html/index.html): expressions, generics, initialization, and memory model.
- [JDK 17 java command](https://docs.oracle.com/en/java/javase/17/docs/specs/man/java.html): logging and diagnostic options.
- [OpenJDK JMH](https://github.com/openjdk/jmh): benchmark infrastructure and annotation processing.
- [JEP 416](https://openjdk.org/jeps/416): the JDK 18 reflection implementation change.
- [JEP 410](https://openjdk.org/jeps/410): removal of the bundled experimental AOT/JIT compiler in JDK 17.
- [GraalVM Native Image](https://www.graalvm.org/latest/reference-manual/native-image/basics/): native-image concepts; the latest documentation can change, so match it to the actual tool version.

Specifications establish semantic guarantees. HotSpot diagnostic output supplies implementation observations. Version statements, observations, and code checks remain separate; one run is not a permanent rule for every JVM.

## Language editions

English is the default edition. Chinese article paths are preserved, and each article links to its counterpart. Both editions use identical Java, JMH, and expected-output blocks. Explanations are translated; program behavior is not localized. Update both editions when changing an experiment.

[Contents](../../README.md)
