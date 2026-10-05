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
