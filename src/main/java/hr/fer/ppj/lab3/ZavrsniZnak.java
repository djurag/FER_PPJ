package hr.fer.ppj.lab3;

public class ZavrsniZnak extends Node {

    private String tip;
    private int redak;
    private String reprezentacija;

    public ZavrsniZnak(String tip, int redak, String reprezentacija) {
        this.tip = tip;
        this.redak = redak;
        this.reprezentacija = reprezentacija;
    }

    public String getTip() {
        return tip;
    }

    public int getRedak() {
        return redak;
    }

    public String getReprezentacija() {
        return reprezentacija;
    }

    @Override
    public String toString() {
        return tip + "(" + redak + "," + reprezentacija + ")";
    }
}