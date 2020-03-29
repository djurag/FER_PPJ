package hr.fer.ppj.lab2;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import hr.fer.ppj.lab2.PomocneKlase.Tablica;

public class SA {
    public static void main(String[] args) throws Exception {
        Tablica tablicaAkcija = new Tablica("Akcija");
        Tablica tablicaNovoStanje = new Tablica("NovoStanje");

        FileInputStream fileIn = new FileInputStream("tablicaAkcija.ser");
        ObjectInputStream in = new ObjectInputStream(fileIn);
        tablicaAkcija = (Tablica) in.readObject();
        in.close();
        fileIn.close();

        fileIn = new FileInputStream("tablicaNovoStanje.ser");
        in = new ObjectInputStream(fileIn);
        tablicaNovoStanje = (Tablica) in.readObject();
        in.close();
        fileIn.close();

        Parser p = new Parser(tablicaAkcija, tablicaNovoStanje);

        List<String> lines = new ArrayList<>();
        Scanner sc = new Scanner(System.in);
        while (sc.hasNext())
            lines.add(sc.nextLine());
        sc.close();
        lines.add("$");

        Node stablo = p.parsirajTekst(lines);
        System.out.println(stablo.ispisiStablo(0, false));
    }
}
