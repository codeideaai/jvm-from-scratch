public class Demo09 {
    static volatile byte[] sink;
    public static void main(String[] args) {
        long checksum = 0;
        for (int i = 0; i < 2000; i++) {
            byte[] block = new byte[128 * 1024];
            block[0] = (byte) i;
            sink = block;
            checksum += sink[0];
        }
        if (checksum != 152) throw new AssertionError(checksum);
        System.out.println("checksum=" + checksum);
    }
}
