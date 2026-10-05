import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
public class Demo12 {
    interface Source<T> { T get(); }
    static class TextSource implements Source<String> {
        public String get() { return "text"; }
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void main(String[] args) {
        long bridges = java.util.Arrays.stream(TextSource.class.getDeclaredMethods())
            .filter(Method::isBridge).count();
        System.out.println("bridges=" + bridges);
        List<String> values = new ArrayList<>();
        List raw = values;
        raw.add(123);
        try {
            String value = values.get(0);
            throw new AssertionError(value);
        } catch (ClassCastException expected) {
            System.out.println("heap pollution detected");
        }
        if (bridges != 1) throw new AssertionError(bridges);
    }
}
