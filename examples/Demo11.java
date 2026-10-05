public class Demo11 {
    static final class Counter {
        private final Object lock = new Object();
        private int value;
        void increment() {
            synchronized (lock) { value++; }
        }
        int value() {
            synchronized (lock) { return value; }
        }
        void nested() {
            synchronized (lock) { increment(); }
        }
    }
    public static void main(String[] args) throws Exception {
        Counter counter = new Counter();
        Runnable work = () -> {
            for (int i = 0; i < 100_000; i++) counter.increment();
        };
        Thread a = new Thread(work);
        Thread b = new Thread(work);
        a.start(); b.start();
        a.join(); b.join();
        counter.nested();
        if (counter.value() != 200001) throw new AssertionError(counter.value());
        System.out.println("count=" + counter.value());
    }
}
