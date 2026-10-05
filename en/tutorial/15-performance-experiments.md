---
pagetitle: "15 Building Trustworthy Performance Experiments"
---

# 15 Building Trustworthy Performance Experiments

[English](15-performance-experiments.md) · [中文](../../tutorial/15-建立可信的性能实验.md)

“Three times faster” means little without an explicit workload and metric. A single nanoTime interval in a short program can be dominated by initialization, compilation, measurement overhead, or scheduling noise. First establish equivalent results, then design a measurement.

## Build the mechanism model

An interpretable benchmark needs a defined input distribution, known work per operation, and consumption of its results to avoid dead-code elimination. Warmup helps expose steady-state behavior, and separate forks reduce contamination between benchmarks in one VM. Warmup does not guarantee that every dynamic effect has ended; inspect stability rather than assuming it.

JMH provides infrastructure, not a correction for a poorly framed question. If a benchmark method performs one hundred thousand business operations, each reported invocation includes that entire batch. If all inputs are compile-time constants, the optimizer may remove the work you intended to measure.

This chapter starts with a functional comparison. The complete JMH project lives in examples/jmh. The functional check prints no timing and announces no winner, so a successful run is not disguised as a performance result.

## Complete code

Save this as `Demo15.java`. It uses only the JDK 17 standard library and compiles independently.

```java
import java.util.Arrays;
public class Demo15 {
    static long loop(int[] values) {
        long sum = 0;
        for (int value : values) sum += value;
        return sum;
    }
    static long stream(int[] values) {
        return Arrays.stream(values).asLongStream().sum();
    }
    public static void main(String[] args) {
        int[] values = new int[1024];
        Arrays.setAll(values, i -> i % 17);
        long a = loop(values);
        long b = stream(values);
        if (a != b || a != 8166) throw new AssertionError(a + ":" + b);
        System.out.println("same result=" + a);
    }
}
```

## Compile and run

```bash
javac --release 17 -encoding UTF-8 Demo15.java
java Demo15
```

Expected output (default verification mode):

```text
same result=8166
```

## Interpreting the evidence

Both implementations accumulate into long, avoiding a comparison between algorithms with different overflow behavior. The JMH project retains the same calculation, parameterizes array length, prepares input in Setup, and returns both benchmark results for the harness to consume.

Defaults use three forks, three warmup iterations, and five measurement iterations, with one second per iteration. A full run takes several minutes. First use short smoke settings to confirm that the project starts, then run the defaults. Smoke measurements are not evidence for a performance conclusion.

Adding `-prof gc` exposes allocation-related metrics but does not replace production response-time observation. This microbenchmark answers a question about these arrays; it does not model a database, network, or concurrent requests. See the [OpenJDK JMH project](https://github.com/openjdk/jmh) for usage and examples.

## Exercises and self-checks

Compare lengths 16, 1024, and 65536, recording means, uncertainty, and allocation metrics. Check repeatability with the same data, JDK, and machine conditions. Report all forks instead of selecting the fastest result.

## Complete JMH project

Save the following two files at the indicated paths. This Maven project is separate from Demo15. Run `mvn clean package` in the directory containing pom.xml.

### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>learning.jvm</groupId>
  <artifactId>jvm-benchmarks</artifactId>
  <version>1.0-SNAPSHOT</version>
  <properties>
    <maven.compiler.release>17</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <jmh.version>1.37</jmh.version>
  </properties>
  <dependencies>
    <dependency>
      <groupId>org.openjdk.jmh</groupId>
      <artifactId>jmh-core</artifactId>
      <version>${jmh.version}</version>
    </dependency>
    <dependency>
      <groupId>org.openjdk.jmh</groupId>
      <artifactId>jmh-generator-annprocess</artifactId>
      <version>${jmh.version}</version>
      <scope>provided</scope>
    </dependency>
  </dependencies>
  <build>
    <finalName>benchmarks</finalName>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.13.0</version>
        <configuration>
          <annotationProcessorPaths>
            <path>
              <groupId>org.openjdk.jmh</groupId>
              <artifactId>jmh-generator-annprocess</artifactId>
              <version>${jmh.version}</version>
            </path>
          </annotationProcessorPaths>
        </configuration>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
        <version>3.5.3</version>
        <executions>
          <execution>
            <phase>package</phase>
            <goals><goal>shade</goal></goals>
            <configuration>
              <createDependencyReducedPom>false</createDependencyReducedPom>
              <transformers>
                <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                  <mainClass>org.openjdk.jmh.Main</mainClass>
                </transformer>
              </transformers>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

### src/main/java/learning/SumBenchmark.java

```java
package learning;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.*;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(3)
public class SumBenchmark {
    @Param({"16", "1024", "65536"})
    public int size;
    private int[] values;

    @Setup(Level.Trial)
    public void setup() {
        values = new int[size];
        Arrays.setAll(values, i -> i % 17);
        if (loop() != stream()) throw new AssertionError("different results");
    }

    @Benchmark
    public long loop() {
        long sum = 0;
        for (int value : values) sum += value;
        return sum;
    }

    @Benchmark
    public long stream() {
        return Arrays.stream(values).asLongStream().sum();
    }
}
```

```bash
# Smoke check: verifies startup only.
java -jar target/benchmarks.jar -f 1 -wi 1 -i 1 -w 100ms -r 100ms -p size=1024
# Full measurement with allocation profiling.
java -jar target/benchmarks.jar -prof gc -rf json -rff results.json
```

Output includes loop and stream, parameters, scores, and units. Scores depend on the environment; there is no fabricated expected nanosecond value.

---

[Previous: Escape Analysis, Loop Optimization, and Vectorization](14-escape-analysis-and-loops.md) · [Contents](../../README.md) · [Next: Connecting Thread, Heap, and JFR Evidence](16-diagnostics-and-jfr.md)
