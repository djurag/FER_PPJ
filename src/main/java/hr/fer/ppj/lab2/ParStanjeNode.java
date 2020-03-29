package hr.fer.ppj.lab2;

public class ParStanjeNode {

    private int stanje;
    private Node node;

    public ParStanjeNode(int stanje, Node node) {
        super();
        this.stanje = stanje;
        this.node = node;
    }

    public int getStanje() {
        return stanje;
    }

    public void setStanje(int stanje) {
        this.stanje = stanje;
    }

    public Node getNode() {
        return node;
    }
}
