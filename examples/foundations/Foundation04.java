import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
public class Foundation04 {
    static Set<String> reachable(Map<String, java.util.List<String>> edges, String root) {
        Set<String> seen = new HashSet<>();
        ArrayDeque<String> pending = new ArrayDeque<>();
        pending.add(root);
        while (!pending.isEmpty()) {
            String node = pending.removeFirst();
            if (seen.add(node)) pending.addAll(edges.getOrDefault(node, java.util.List.of()));
        }
        return seen;
    }
    public static void main(String[] args) {
        Map<String, java.util.List<String>> graph = Map.of(
            "root", java.util.List.of("A"),
            "A", java.util.List.of("B"),
            "B", java.util.List.of("A"),
            "C", java.util.List.of("D"),
            "D", java.util.List.of("C"));
        Set<String> live = reachable(graph, "root");
        if (!live.equals(Set.of("root", "A", "B"))) throw new AssertionError(live);
        System.out.println("reachable objects=" + (live.size() - 1));
        System.out.println("unreachable cycle=" + (!live.contains("C") && !live.contains("D")));
    }
}
