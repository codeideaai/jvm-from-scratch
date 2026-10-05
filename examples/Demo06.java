public class Demo06 {
    static class Resource implements AutoCloseable {
        final String name;
        Resource(String name) { this.name = name; }
        @Override public void close() {
            System.out.println("close " + name);
            throw new IllegalStateException("close-" + name);
        }
    }
    public static void main(String[] args) {
        try (Resource a = new Resource("A"); Resource b = new Resource("B")) {
            throw new IllegalArgumentException("business");
        } catch (IllegalArgumentException e) {
            System.out.println("primary=" + e.getMessage());
            for (Throwable other : e.getSuppressed()) {
                System.out.println("suppressed=" + other.getMessage());
            }
            if (e.getSuppressed().length != 2) throw new AssertionError(e);
        }
    }
}
