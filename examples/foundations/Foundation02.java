public class Foundation02 {
    static class Box { int value; }
    static void change(Box copy) {
        copy.value = 20;
        copy = new Box();
        copy.value = 99;
    }
    public static void main(String[] args) {
        Box first = new Box();
        first.value = 10;
        Box alias = first;
        change(first);
        if (first != alias || first.value != 20) throw new AssertionError();
        System.out.println("same object=" + (first == alias));
        System.out.println("value=" + first.value);
    }
}
