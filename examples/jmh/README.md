# JMH Array-Sum Experiment

**English** · [中文](README.zh-CN.md)

Requires JDK 17 and Maven 3.9+. The first build downloads dependencies. JMH is pinned to 1.37 for reproducibility, not as a claim about the latest release.

```bash
cd examples/jmh
mvn clean package
# Startup check only; do not use these numbers for performance conclusions.
java -jar target/benchmarks.jar -f 1 -wi 1 -i 1 -w 100ms -r 100ms -p size=1024
# Full experiment: defaults to 3 forks, 3 warmup and 5 measurement iterations.
java -jar target/benchmarks.jar -prof gc -rf json -rff results.json
```

Each invocation sums a whole array. ns/op is not a per-element cost. JMH consumes return values, and Setup constructs inputs and checks equivalent results.

Record the JDK, CPU, operating system, VM options, workload, and every fork's result. Defaults take several minutes. See the [validation record](../../en/tutorial/validation.md) for the verified scope.
