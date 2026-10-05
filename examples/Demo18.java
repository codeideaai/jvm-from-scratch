import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
public class Demo18 {
    static class Bounded extends LinkedHashMap<Integer, byte[]> {
        private final int limit;
        Bounded(int limit) {
            super(16, 0.75f, true);
            if (limit <= 0) throw new IllegalArgumentException("limit");
            this.limit = limit;
        }
        @Override protected boolean removeEldestEntry(Map.Entry<Integer, byte[]> eldest) {
            return size() > limit;
        }
    }
    static void fill(Map<Integer, byte[]> cache) {
        for (int i = 0; i < 2000; i++) cache.put(i, new byte[1024]);
    }
    public static void main(String[] args) {
        Map<Integer, byte[]> unlimited = new HashMap<>();
        Bounded bounded = new Bounded(128);
        fill(unlimited); fill(bounded);
        if (unlimited.size() != 2000 || bounded.size() != 128) throw new AssertionError();
        System.out.println("unbounded=" + unlimited.size());
        System.out.println("bounded=" + bounded.size());
        Bounded order = new Bounded(2);
        order.put(1, new byte[1]); order.put(2, new byte[1]);
        order.get(1);
        order.put(3, new byte[1]);
        if (order.containsKey(2) || !order.containsKey(1)) throw new AssertionError();
        System.out.println("least recently used evicted=true");
    }
}
