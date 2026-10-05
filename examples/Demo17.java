import java.lang.instrument.Instrumentation;
public class Demo17 {
    private static volatile Instrumentation instrumentation;
    public static void premain(String args, Instrumentation inst) {
        instrumentation = inst;
        System.out.println("agent ready");
    }
    public static void main(String[] args) {
        Instrumentation inst = instrumentation;
        if (inst == null) throw new IllegalStateException("run with -javaagent");
        long small = inst.getObjectSize(new byte[0]);
        long large = inst.getObjectSize(new byte[1024]);
        if (small <= 0 || large <= small) throw new AssertionError("unexpected sizes");
        System.out.println("positive shallow size=" + (small > 0));
        System.out.println("larger array=" + (large > small));
    }
}
