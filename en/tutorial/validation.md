---
pagetitle: "Validation Record"
---

# Validation Record

[English](validation.md) · [中文](../../tutorial/验证记录.md)

Original experiment date: 2026-10-05. Environment: macOS arm64, Homebrew OpenJDK 17.0.20 and javac 17.0.20. The articles target Java 17; they do not require this exact patch release.

## Main examples

Source was extracted directly from the complete Java blocks in Chapters 01–18, compiled in independent temporary directories using `javac --release 17 -encoding UTF-8`, and executed against each article's expected output. All 18 original examples passed. Verification did not merely compile a separate copy from examples.

The bilingual verifier checks both editions against the same example files and executes each edition independently. Chapter 15 also checks that the embedded JMH source and pom.xml match the project. Concurrency checks use stable final conditions; they do not require an incorrect racy program to fail on every run.

Chapter 17 builds a temporary agent JAR and verifies premain and the application assertions. Running without -javaagent fails explicitly.

## Additional observations

| Check | Observed result | Limit of the evidence |
| --- | --- | --- |
| javap | Version 61, integer arithmetic, calls, exception table, addSuppressed, lambda bootstrap, monitors, bridge flags | Bytecode is not native assembly |
| Demo09 G1 | Completed with a 32 MiB heap and generated pause logs | Counts and timings are not fixed expectations |
| Demo13 JIT | Logs contain batch, step, and inlining decisions | Other platforms may make different decisions |
| Demo14 comparison | Default and disabled escape-analysis configurations completed | GC differences alone do not prove specific generated code |
| Demo16 jcmd | Thread.print and GC.heap_info succeeded | Only the newly started tutorial process was targeted |
| Demo16 JFR | One-second smoke recording and jfr summary succeeded | Article instructions use ten seconds; this was not production profiling |
| JMH 1.37 | Maven 3.9.9 build and separate-fork smoke run succeeded | 100 ms warmup and measurement verify startup, not performance |

[Raw text logs](../../validation/README.md) retain the original observations. Initially, sandbox restrictions prevented Attach and JMH loopback communication. Both succeeded when allowed to operate on the tutorial's own processes. No Java source change was required.

## Bilingual site checks

Quarto 1.10.18 rendered all 46 pages. Language attributes, counterpart links, and local navigation targets passed checks. All 36 article editions compiled and ran independently with matching Java/XML/output blocks. Browser checks confirmed the English default and switching Chapter 03 to its Chinese counterpart with the Chinese sidebar. No public deployment is configured.

## Unverified claims

The full multi-fork JMH measurement was not run, and no performance winner is claimed. Other architectures and JDK versions were not tested. Chapter 19 does not include tested JNI libraries, Truffle languages, or Native Image builds.

## Reproduce

```bash
python3 tools/verify_article_code.py
python3 tools/check_links.py
python3 tools/check_bilingual.py
bash examples/run.sh 17
cd examples/jmh
mvn clean package
java -jar target/benchmarks.jar -f 1 -wi 1 -i 1 -w 100ms -r 100ms -p size=1024
```

Checks use actual exit statuses, documented output, and assertions. Rerun observation commands on your own environment and retain the logs.

[Contents](../../README.md)

## Beginner revision: 2026-10-05

Six bilingual foundation lessons were added and eight main chapters expanded (01, 03, 07, 08, 09, 10, 13, 16). All 12 foundation editions and all 36 main editions compiled and ran independently: 48 executions in total. Additional checks cover command-line arguments, the eight-instruction operand-stack trace, rejection of an incorrect heap configuration, and the deliberately arranged thread schedule.

F05 runs only in a newly launched JVM with -Xms16m -Xmx32m and Serial GC. Its allocation attempts are bounded, and the example refuses a larger heap before allocating the payloads. No system-wide memory exhaustion, native-thread exhaustion, or production process inspection is performed.

The check shell initially resolved /usr/bin/java instead of the installed JDK. Verification then explicitly selected the existing Homebrew JDK 17 tool directory via PATH; no system Java installation was changed. This is the environment mismatch discussed in F01.

The site now contains 30 language pairs: two homepages, 26 article pairs, and three guide/appendix pairs. All 60 rendered pages passed language, counterpart-link, and local-target checks. Browser checks confirmed the English homepage, the F04 reference graph, and switching to the corresponding Chinese lesson and sidebar. Original JMH smoke results remain historical observations; JMH code was unchanged and full performance experiments were not rerun.

[Beginner route](beginner-guide.md)
