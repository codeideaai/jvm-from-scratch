public class Demo03 {
    static class Config {
        static final int PORT = 8080;
        static int timeout = initialize();
        static int initialize() {
            System.out.println("Config initialized");
            return 30;
        }
    }
    public static void main(String[] args) throws Exception {
        System.out.println("constant=" + Config.PORT);
        Class.forName("Demo03$Config", false, Demo03.class.getClassLoader());
        System.out.println("loaded without initialization");
        System.out.println("timeout=" + Config.timeout);
        System.out.println("timeout again=" + Config.timeout);
    }
}
