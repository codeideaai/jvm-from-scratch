import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
public class Foundation06 {
    static volatile int value;
    static void await(CyclicBarrier gate) {
        try { gate.await(5, TimeUnit.SECONDS); }
        catch (Exception e) { throw new AssertionError(e); }
    }
    public static void main(String[] args) throws Exception {
        CyclicBarrier bothRead = new CyclicBarrier(2);
        Runnable splitIncrement = () -> {
            int local = value;
            await(bothRead);
            value = local + 1;
        };
        Thread a = new Thread(splitIncrement);
        Thread b = new Thread(splitIncrement);
        a.start(); b.start(); a.join(); b.join();
        if (value != 1) throw new AssertionError(value);
        System.out.println("split increment=" + value);
        AtomicInteger count = new AtomicInteger();
        Runnable atomicIncrement = () -> {
            for (int i = 0; i < 1000; i++) count.incrementAndGet();
        };
        Thread c = new Thread(atomicIncrement);
        Thread d = new Thread(atomicIncrement);
        c.start(); d.start(); c.join(); d.join();
        if (count.get() != 2000) throw new AssertionError(count.get());
        System.out.println("atomic increments=" + count.get());
    }
}
