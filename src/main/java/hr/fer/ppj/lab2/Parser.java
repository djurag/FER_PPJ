package hr.fer.ppj.lab2;

import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import hr.fer.ppj.lab2.PomocneKlase.Tablica;
import hr.fer.ppj.lab2.PomocneKlase.ZavrsniZnak;
import hr.fer.ppj.lab2.PomocneKlase.ZnakGramatike;

public class Parser {

    private Tablica tablicaAkcija;
    private Tablica tablicaStanja;
    private Stack<ParStanjeNode> stog;

    private Node stablo;

    public Parser(Tablica tablicaAkcija, Tablica tablicaStanja) {
        this.tablicaAkcija = tablicaAkcija;
        this.tablicaStanja = tablicaStanja;
    }

    public Node parsirajTekst(List<String> redoviTeksta) {
        initialize();

        List<ZavrsniZnak> ulazniZnakovi = dohvatiZnakove(redoviTeksta);
        int i = 0;
        while (i != ulazniZnakovi.size()) {
            int stanje = stog.peek().getStanje();
            ZavrsniZnak trenutniZnak = ulazniZnakovi.get(i);

            String akcija = tablicaAkcija.get(trenutniZnak).get(stanje).trim();

            if ("-".equals(akcija)) {
                System.out.println("GRESKAAAA - napravi obradu");
                break;
            }
            if (akcija.startsWith("Pomakni")) {
                napraviPomakni(akcija, redoviTeksta.get(i));
                i++;
            } else if (akcija.startsWith("Reduciraj")) {
                napraviReduciraj(akcija);
            } else if (akcija.equals("Prihvati()")) {
                break;
            }
        }

        return stablo;
    }

    private void napraviReduciraj(String akcija) {
        akcija = akcija.replace("Reduciraj(", "").replace(")", "");

        String[] dijelovi = akcija.split(" ::= ");
        String lijeviDio = dijelovi[0].trim();
        String desniDio = dijelovi[1].trim();

        Node noviNode = new Node(new ZnakGramatike(lijeviDio));

        String[] znakovi = desniDio.split("\\s+");

        if (znakovi.length != 1 || !znakovi[0].equals("\"\"")) {
            for (int i = znakovi.length - 1; i >= 0; i--) {
                ParStanjeNode saStoga = stog.pop();
                noviNode.addNode(saStoga.getNode());
            }
        } else {
            noviNode.addNode(new Node(new ZnakGramatike("$")));
        }
        String akcijaStavi = tablicaStanja.get(lijeviDio).get(stog.peek().getStanje());
        int novoStanje = Integer.parseInt(akcijaStavi.replace("Stavi(", "").replace(")", ""));
        stog.push(new ParStanjeNode(novoStanje, noviNode));
        stablo = noviNode;
    }

    private void napraviPomakni(String akcija, String redTeksta) {
        akcija = akcija.replace("Pomakni(", "").replace(")", "");

        int stanje = Integer.parseInt(akcija);
        ParStanjeNode noviPar = new ParStanjeNode(stanje, new Node(new ZnakGramatike(redTeksta)));
        stog.push(noviPar);
    }

    private void initialize() {
        stog = new Stack<>();
        stog.push(new ParStanjeNode(0, null));
    }

    private List<ZavrsniZnak> dohvatiZnakove(List<String> retci) {
        return retci.stream().map(s -> new ZavrsniZnak(s.split("\\s")[0])).collect(Collectors.toList());
    }

}
