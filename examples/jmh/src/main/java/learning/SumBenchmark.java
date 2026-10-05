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
