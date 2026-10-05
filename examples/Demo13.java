public class Demo13 {
    static volatile long sink;
    static int step(int value) { return value * 3 + 1; }
    static long batch(int count) {
        long sum = 0;
        for (int i = 0; i < count; i++) sum += step(i);
        return sum;
    }
    public static void main(String[] args) {
        for (int i = 0; i < 2000; i++) sink = batch(10000);
        long expected = 3L * 9999 * 10000 / 2 + 10000;
        if (sink != expected) throw new AssertionError(sink);
        System.out.println("sum=" + sink);
    }
}
