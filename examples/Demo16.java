import java.util.ArrayList;
import java.util.List;
public class Demo16 {
    static final List<byte[]> retained = new ArrayList<>();
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 16; i++) retained.add(new byte[64 * 1024]);
        System.out.println("blocks=" + retained.size());
        if (args.length > 0 && args[0].equals("--observe")) {
            System.out.println("pid=" + ProcessHandle.current().pid());
            Thread.sleep(60_000);
        }
        if (retained.size() != 16) throw new AssertionError();
    }
}
