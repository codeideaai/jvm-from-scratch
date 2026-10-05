public class Demo10 {
    static int payload;
    static volatile boolean ready;
    static volatile int observed;
    public static void main(String[] args) throws Exception {
        Thread reader = new Thread(() -> {
            long deadline = System.nanoTime() + 5_000_000_000L;
            while (!ready) {
                if (System.nanoTime() - deadline >= 0) return;
                Thread.onSpinWait();
            }
            observed = payload;
        });
        reader.start();
        payload = 42;
        ready = true;
        reader.join();
        if (observed != 42) throw new AssertionError("publication timed out or failed");
        System.out.println("observed=" + observed);
    }
}
