import java.io.InputStream;
public class Demo04 {
    public static class Payload {}
    static class Isolated extends ClassLoader {
        Class<?> define(byte[] bytes) {
            return defineClass("Demo04$Payload", bytes, 0, bytes.length);
        }
    }
    public static void main(String[] args) throws Exception {
        byte[] bytes;
        try (InputStream in = Demo04.class.getResourceAsStream("/Demo04$Payload.class")) {
            if (in == null) throw new IllegalStateException("class resource missing");
            bytes = in.readAllBytes();
        }
        Class<?> a = new Isolated().define(bytes);
        Class<?> b = new Isolated().define(bytes);
        if (a == b || !a.getName().equals(b.getName())) throw new AssertionError();
        System.out.println("same name=" + a.getName().equals(b.getName()));
        System.out.println("same type=" + (a == b));
        Object value = a.getConstructor().newInstance();
        try {
            b.cast(value);
            throw new AssertionError("cross-loader cast accepted");
        } catch (ClassCastException expected) {
            System.out.println("cast rejected");
        }
    }
}
