public class Demo01 {
    static int total(int price, int count, int discount) {
        return price * count - discount;
    }
    public static void main(String[] args) {
        int value = total(12, 3, 5);
        if (value != 31) throw new AssertionError(value);
        System.out.println("total=" + value);
    }
}
