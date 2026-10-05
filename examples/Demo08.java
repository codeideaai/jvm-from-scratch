import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
public class Demo08 {
    public static void main(String[] args) {
        Object strong = new Object();
        ReferenceQueue<Object> queue = new ReferenceQueue<>();
        WeakReference<Object> weak = new WeakReference<>(strong, queue);
        System.out.println("same referent=" + (weak.get() == strong));
        weak.clear();
        System.out.println("cleared=" + (weak.get() == null));
        System.out.println("queue empty=" + (queue.poll() == null));
        System.out.println("enqueued=" + weak.enqueue());
        System.out.println("same reference=" + (queue.poll() == weak));
        java.lang.ref.Reference.reachabilityFence(strong);
    }
}
