import java.math.BigDecimal;
public class Demo02 {
    public static void main(String[] args) {
        System.out.println(Integer.MAX_VALUE + 1);
        System.out.println((byte) 130);
        System.out.println(50_000 * 50_000);
        System.out.println(50_000L * 50_000);
        System.out.println(0.1 + 0.2 == 0.3);
        BigDecimal sum = new BigDecimal("0.1").add(new BigDecimal("0.2"));
        if (sum.compareTo(new BigDecimal("0.3")) != 0) throw new AssertionError(sum);
        System.out.println(sum);
        try {
            Math.addExact(Integer.MAX_VALUE, 1);
            throw new AssertionError("overflow accepted");
        } catch (ArithmeticException expected) {
            System.out.println("overflow rejected");
        }
    }
}
