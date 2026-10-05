# Raw Experiment Records

**English** · [中文](README.zh-CN.md)

Recorded on 2026-10-05 using macOS arm64 and Homebrew OpenJDK 17.0.20. Only tutorial-created processes were inspected.

- gc.txt: Demo09 G1 log; timings and counts belong to this run.
- jit.txt: Demo13 compilation and inlining log.
- ea-on.txt and ea-off.txt: Demo14 GC comparison, not proof of exact generated code.
- bytecode-checks.txt: important bytecode features and the missing-agent failure path.
- Thread.print.txt and GC.heap_info.txt: Demo16 observations.
- JFR.start.txt and jfr-summary.txt: one-second smoke recording; article commands use ten seconds.
- jmh-smoke.txt: startup check whose short measurement is unsuitable for ranking performance.

PIDs, paths, and times are observations, not expected output. Binary JFR files and compiled artifacts are not part of the source delivery.
