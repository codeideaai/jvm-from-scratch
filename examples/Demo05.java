public class Demo05 {
    static class Parent {
        String name() { return "parent"; }
    }
    static class Child extends Parent {
        @Override String name() { return "child"; }
    }
    static String select(Parent value) { return "Parent overload"; }
    static String select(Child value) { return "Child overload"; }
    public static void main(String[] args) {
        Parent value = new Child();
        System.out.println(select(value));
        System.out.println(value.name());
        System.out.println(select((Child) value));
        if (!value.name().equals("child")) throw new AssertionError();
    }
}
