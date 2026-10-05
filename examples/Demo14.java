public class Demo14 {
    record Point(int x, int y) {}
    static volatile long sink;
    static long calculate(int count) {
        long sum = 0;
        for (int i = 0; i < count; i++) {
            Point point = new Point(i, i + 1);
            sum += point.x() + point.y();
        }
        return sum;
    }
    public static void main(String[] args) {
        for (int i = 0; i < 3000; i++) sink = calculate(1000);
        if (sink != 1_000_000L) throw new AssertionError(sink);
        System.out.println("sum=" + sink);
    }
}
