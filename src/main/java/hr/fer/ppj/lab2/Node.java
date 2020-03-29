package hr.fer.ppj.lab2;

import hr.fer.ppj.lab2.PomocneKlase.ZnakGramatike;

import java.util.ArrayList;
import java.util.List;

public class Node {

    private ZnakGramatike znak;
    private List<Node> nodes = new ArrayList<>();

    public Node(ZnakGramatike znak) {
        this.znak = znak;
    }

    public void addNode(Node n) {
        nodes.add(n);
    }

    public ZnakGramatike getZnak() {
        return znak;
    }

    public void insertNode(Node node, int index) {
        nodes.add(index, node);
    }

    public String ispisiStablo(int razina, boolean inOrder) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < razina; i++) {
            sb.append(" ");
        }
        sb.append(znak).append("\n");

        if (inOrder) {
            for (Node node : nodes) {
                sb.append(node.ispisiStablo(razina + 1, true));
            }
        } else {
            for (int i = nodes.size() - 1; i >= 0; i--) {
                sb.append(nodes.get(i).ispisiStablo(razina + 1, false));
            }
        }

        return sb.toString();
    }
}
