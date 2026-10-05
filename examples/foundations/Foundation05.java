import java.util.ArrayList;
import java.util.List;
public class Foundation05 {
    public static void main(String[] args) {
        long max = Runtime.getRuntime().maxMemory();
        if (max < 16L * 1024 * 1024 || max > 40L * 1024 * 1024) {
            throw new IllegalStateException("use -Xms16m -Xmx32m -XX:+UseSerialGC");
        }
        List<byte[]> retained = new ArrayList<>(128);
        System.out.println("heap guard passed=true");
        try {
            for (int i = 0; i < 128; i++) retained.add(new byte[1024 * 1024]);
            throw new AssertionError("small heap unexpectedly held all payloads");
        } catch (OutOfMemoryError expected) {
            retained.clear();
            System.out.println("failure=OutOfMemoryError");
            System.out.println("references cleared=" + retained.isEmpty());
        }
    }
}
