import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.IntBinaryOperator;
public class Demo07 {
    public static int add(int a, int b) { return a + b; }
    public static int fail() { throw new IllegalArgumentException("business"); }
    public static void main(String[] args) throws Throwable {
        Method method = Demo07.class.getMethod("add", int.class, int.class);
        MethodHandle handle = MethodHandles.lookup().findStatic(
            Demo07.class, "add", MethodType.methodType(int.class, int.class, int.class));
        IntBinaryOperator lambda = Demo07::add;
        System.out.println("reflection=" + method.invoke(null, 2, 3));
        System.out.println("handle=" + (int) handle.invokeExact(2, 3));
        System.out.println("lambda=" + lambda.applyAsInt(2, 3));
        try {
            Demo07.class.getMethod("fail").invoke(null);
            throw new AssertionError("exception lost");
        } catch (InvocationTargetException e) {
            if (!(e.getCause() instanceof IllegalArgumentException)) throw e;
            System.out.println("cause=" + e.getCause().getMessage());
        }
    }
}
