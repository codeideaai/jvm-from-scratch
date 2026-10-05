import java.io.DataInputStream;
import java.io.InputStream;
public class Foundation03 {
    static int calculate(int x) {
        int y = x + 2;
        return y * 3;
    }
    public static void main(String[] args) throws Exception {
        int result = calculate(4);
        if (result != 18) throw new AssertionError(result);
        System.out.println("result=" + result);
        try (InputStream raw = Foundation03.class.getResourceAsStream("/Foundation03.class")) {
            if (raw == null) throw new IllegalStateException("class resource missing");
            DataInputStream in = new DataInputStream(raw);
            int magic = in.readInt();
            in.readUnsignedShort();
            int major = in.readUnsignedShort();
            if (magic != 0xCAFEBABE || major != 61) throw new AssertionError();
            System.out.printf("magic=%08X%n", magic);
            System.out.println("major=" + major);
        }
    }
}
