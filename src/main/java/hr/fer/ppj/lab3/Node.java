package hr.fer.ppj.lab3;

import java.util.ArrayList;
import java.util.List;

public class Node {

    protected List<Node> djeca = new ArrayList<>();

    public void dodajDijete(Node dijete) {
        djeca.add(dijete);
    }

    public int sizeDjeca() {
        return djeca.size();
    }

    public List<Node> getDjeca() {
        return djeca;
    }

    public String ispisiStablo(int razina, boolean inOrder) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < razina; i++) {
            sb.append(" ");
        }
        sb.append(toString()).append("\n");

        if (inOrder) {
            for (Node node : djeca) {
                sb.append(node.ispisiStablo(razina + 1, inOrder));
            }
        } else {
            for (int i = djeca.size() - 1; i >= 0; i--) {
                sb.append(djeca.get(i).ispisiStablo(razina + 1, inOrder));
            }
        }

        return sb.toString();
    }

}

