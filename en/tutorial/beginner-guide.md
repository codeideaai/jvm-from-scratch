---
pagetitle: "Beginner Learning Path"
---

# Beginner Learning Path

[English](beginner-guide.md) · [中文](../../tutorial/新手学习路线与补齐说明.md)

## Before you begin

This series introduces the JVM through concepts, worked reasoning, and runnable experiments. Understand the terms and prerequisites before running code and interpreting its output. Use the table below to find the relevant foundation lesson whenever a concept is unclear.

Here, beginner means new to the JVM. If you have never written Java, first learn variables, if, loops, methods, classes, and objects. F01 gets a program compiled and running; it does not pretend to replace a complete language course.

## Concepts and prerequisites

| Topic | Prerequisite question | Supporting lesson |
| --- | --- | --- |
| Frames, operand stacks, constant pools in 01 | What are they, and how do values move? | F02/F03 and 01's slower walkthrough |
| Loading and initialization in 03–04 | Are bytes, runtime classes, and objects the same? | F01/F03 and 03's phase table |
| Reflection, handles, and lambdas together in 07 | Does finding a target equal invoking it? | Layered reading in 07; reflection first |
| Weak references early in 08 | How does ordinary strong reachability work? | F02/F04 and 08's strong-reference example |
| G1 early in 09 | Algorithms versus collectors; live versus cumulative allocation | F04/F05 and 09's real-log annotation |
| Happens-before in 10 | Why do visibility and atomicity differ? | F06 and 10's four-action chain |
| JIT output in 13 | Why compile, when switch, and what is OSR? | Execution phases and annotated log lines |
| jcmd/JFR in 16 | Which line matters, and which hypothesis does it test? | Annotated stack and symptom-to-evidence table |

## A route that need not follow numeric order

| Stage | Sequence | Exit check |
| --- | --- | --- |
| Run and trace a program | F01 → F02 → F03 | Explain source, class, process, and trace an operand stack |
| Understand execution | 01 → 02 → 03 → 05 → 06, then 04 | Distinguish initialization, invocation, and instance creation |
| Understand retention and failure | F04 → F05 → 08 → 09 → 18 | Draw retaining paths and separate leaks, OOM, and GC pressure |
| Understand shared state | F06 → 10 → 11 → 12 | Draw a lost update and justify a publication chain |
| Begin diagnosis | 16 → 13 → 14 → 15 | Choose evidence for a hypothesis rather than trust one timing |
| Second-pass extensions | Handles/lambdas in 07, then 17 and 19 | Identify the problem each mechanism solves before its internals |

Work through a section by predicting, running, and explaining. Chapters per day are not an understanding metric. Return to a foundation lesson when terminology blocks progress rather than repeatedly memorizing the same paragraph.

## A compact glossary

| Term | Initial meaning | Start here |
| --- | --- | --- |
| JDK / HotSpot | Tool-and-runtime distribution / one JVM implementation | F01 |
| Classpath | Search roots and entries used to locate classes | F01 |
| Reference / object | A value used to access an instance / the instance | F02 |
| Frame / heap | One invocation's execution state / managed instance and array storage | F02–F03 |
| Descriptor / constant pool | Encoded type signature / indexed class-file entries | F03 |
| Reachable / GC roots | Accessible along references from roots / traversal starting points | F04 |
| STW / concurrent GC | Application pause phase / collector work overlapping application execution | F04 |
| OOM / leak | Failed allocation / unnecessary retention | F05 |
| Atomicity / visibility | Indivisibility / rules governing observations | F06 |
| JIT / OSR | Runtime compilation / compiled entry into an already executing method | 13 |

## Questions that reveal understanding

1. If two variables reference one object and one is assigned null, can the other still access it? Yes: rebinding one variable does not remove the other path.
2. Does allocating 250 MiB cumulatively require a 250 MiB heap? Not necessarily: simultaneous live data and collection behavior determine the pressure.
3. Does volatile combine separate reads and writes into atomic addition? No; F06 fixes an interleaving that exposes the difference.
4. Does OutOfMemoryError prove a leak? No; genuinely needed live data can exceed a small configured capacity.
5. Does decreasing occupancy after GC prove that all unused memory returned to the OS? No; those are different claims.
6. Does a call in javap prove that native execution retains that call? No; JIT inlining may change the implementation.

If an answer is unclear, revisit the associated graph or schedule rather than memorizing the sentence.

[Start F01](../foundations/F01-first-program.md) · [Contents](../../README.md)
