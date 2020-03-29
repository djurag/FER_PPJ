package hr.fer.ppj.lab3;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

public class SemantickiAnalizator {

    private static List<Djelokrug.Funkcija> deklariraneBezDefinicije = new ArrayList<>();
    private static List<Djelokrug.Funkcija> definirane = new ArrayList<>();
    private static List<Djelokrug> sviDjelokruzi = new ArrayList<>();

    public static void main(String[] args) {
        Node stablo = procitajStablo();
//		System.out.println(stablo.ispisiStablo(0, true));
        ((NezavrsniZnak) stablo).izracunajSvojstva();

        provjeriFunkcije();
        System.out.println();
    }

    private static void provjeriFunkcije() {
        List<String> ulazniMain = new ArrayList<>();
        ulazniMain.add("void");
        Djelokrug.Funkcija main = new Djelokrug.Funkcija("main", ulazniMain, "int");

        boolean mainDefinirana = false;
        for (Djelokrug djelokrug : sviDjelokruzi) {
            for (Djelokrug.Identifikator i : djelokrug.getIdentifikatori()) {
                if (!(i instanceof Djelokrug.Funkcija)) {
                    continue;
                }
                Djelokrug.Funkcija fun = (Djelokrug.Funkcija) i;

                if (!fun.jeDefinirana()) {
                    if (!deklariraneBezDefinicije.contains(fun) && !definirane.contains(fun)) {
                        deklariraneBezDefinicije.add(fun);
                    }
                } else {
                    deklariraneBezDefinicije.remove(fun);
                    definirane.add(fun);
                    if (fun.equals(main)) {
                        mainDefinirana = true;
                    }
                }
            }
        }

        if (!mainDefinirana) {
            System.out.println("main");
        } else if (!deklariraneBezDefinicije.isEmpty()) {
            System.out.println("funkcija");
        }
    }

    private static Node procitajStablo() {
        int dubina = 1;
        Stack<Node> stog = new Stack<>();
        Stack<Djelokrug> djelokruzi = new Stack<>();

        NezavrsniZnak pocetni;
        try (Scanner sc = new Scanner(System.in)) {
            pocetni = (NezavrsniZnak) instancirajZnak(sc.nextLine().trim());
            stog.push(pocetni);
            djelokruzi.push(new Djelokrug(null, "globalni"));
            sviDjelokruzi.add(djelokruzi.peek());

            Node prethodni = null;
            Node trenutni;
            while (sc.hasNextLine()) {
                String line = sc.nextLine();

                if (line.trim().isEmpty()) {
                    continue;
                }

                trenutni = instancirajZnak(line.trim());

                if (trenutni == null) {
                    System.out.println("trenutni: " + line);
                }

                if (trenutni instanceof NezavrsniZnak) {
                    NezavrsniZnak nezavrsni = (NezavrsniZnak) trenutni;
                    nezavrsni.setDjelokrug(djelokruzi.peek());

                    if (nezavrsni.zapocinjeDjelokrug()) {
                        Djelokrug noviDjelokrug = new Djelokrug(djelokruzi.peek(), nezavrsni.getIme());
                        if (nezavrsni instanceof DeklaracijeIFunkcije.DefinicijaFunkcije) {
                            ((DeklaracijeIFunkcije.DefinicijaFunkcije) nezavrsni).dodajDjelokrug(noviDjelokrug);
                        }
                        if (nezavrsni instanceof Naredbe.SlozenaNaredba) {
                            nezavrsni.setDjelokrug(noviDjelokrug);
                        }
                        djelokruzi.push(noviDjelokrug);
                        sviDjelokruzi.add(noviDjelokrug);
                    }
                }
                int novaDubina = odrediDubinu(line);
                while (novaDubina < dubina) {
                    Node znak = stog.pop();
                    if (znak instanceof NezavrsniZnak && ((NezavrsniZnak) znak).zapocinjeDjelokrug()) {
                        djelokruzi.pop();
                    }

                    dubina--;
                }
                if (novaDubina > dubina) {
                    stog.push(prethodni);
                    dubina++;
                }
                if (novaDubina == dubina) {
                    stog.peek().dodajDijete(trenutni);
                }
                prethodni = trenutni;
            }
        }

        return pocetni;
    }

    private static Node instancirajZnak(String ime) {
        if (ime.isEmpty()) {
            return null;
        }

        switch (ime) {
            case "<primarni_izraz>":
                return new NezavrsniZnak.PrimarniIzraz();
            case "<postfiks_izraz>":
                return new NezavrsniZnak.PostfiksIzraz();
            case "<lista_argumenata>":
                return new NezavrsniZnak.ListaArgumenata();
            case "<unarni_izraz>":
                return new NezavrsniZnak.UnarniIzraz();
            case "<cast_izraz>":
                return new NezavrsniZnak.CastIzraz();
            case "<ime_tipa>":
                return new NezavrsniZnak.ImeTipa();
            case "<specifikator_tipa>":
                return new NezavrsniZnak.SpecifikatorTipa();
            case "<multiplikativni_izraz>":
                return new NezavrsniZnak.MultiplikativniIzraz();
            case "<aditivni_izraz>":
                return new NezavrsniZnak.AditivniIzraz();
            case "<odnosni_izraz>":
                return new NezavrsniZnak.OdnosniIzraz();
            case "<jednakosni_izraz>":
                return new NezavrsniZnak.JednakosniIzraz();
            case "<bin_i_izraz>":
                return new NezavrsniZnak.BiniIzraz();
            case "<bin_xili_izraz>":
                return new NezavrsniZnak.BinXiliIzraz();
            case "<bin_ili_izraz>":
                return new NezavrsniZnak.BinIliIzraz();
            case "<log_i_izraz>":
                return new NezavrsniZnak.LogiIzraz();
            case "<log_ili_izraz>":
                return new NezavrsniZnak.LogIliIzraz();
            case "<izraz_pridruzivanja>":
                return new NezavrsniZnak.IzrazPridruzivanja();
            case "<izraz>":
                return new NezavrsniZnak.Izraz();
            case "<unarni_operator>":
                return new NezavrsniZnak.UnarniOperator();

            case "<slozena_naredba>":
                return new Naredbe.SlozenaNaredba();
            case "<lista_naredbi>":
                return new Naredbe.ListaNaredbi();
            case "<naredba>":
                return new Naredbe.Naredba();
            case "<izraz_naredba>":
                return new Naredbe.IzrazNaredba();
            case "<naredba_grananja>":
                return new Naredbe.NaredbaGrananja();
            case "<naredba_petlje>":
                return new Naredbe.NaredbaPetlje();
            case "<naredba_skoka>":
                return new Naredbe.NaredbaSkoka();
            case "<prijevodna_jedinica>":
                return new Naredbe.PrijevodnaJedinica();
            case "<vanjska_deklaracija>":
                return new Naredbe.VanjskaDeklaracija();

            case "<definicija_funkcije>":
                return new DeklaracijeIFunkcije.DefinicijaFunkcije();
            case "<lista_parametara>":
                return new DeklaracijeIFunkcije.ListaParametara();
            case "<deklaracija_parametra>":
                return new DeklaracijeIFunkcije.DeklaracijaParametra();
            case "<lista_deklaracija>":
                return new DeklaracijeIFunkcije.ListaDeklaracija();
            case "<deklaracija>":
                return new DeklaracijeIFunkcije.Deklaracija();
            case "<lista_init_deklaratora>":
                return new DeklaracijeIFunkcije.ListaInitDeklaratora();
            case "<init_deklarator>":
                return new DeklaracijeIFunkcije.InitDeklarator();
            case "<izravni_deklarator>":
                return new DeklaracijeIFunkcije.IzravniDeklarator();
            case "<inicijalizator>":
                return new DeklaracijeIFunkcije.Inicijalizator();
            case "<lista_izraza_pridruzivanja>":
                return new DeklaracijeIFunkcije.ListaIzrazaPridruzivanja();
        }

        String[] parts = ime.split(" ", 3);
        if (parts.length == 3) {
            return new ZavrsniZnak(parts[0], Integer.parseInt(parts[1]), parts[2]);
        }

        for (String part : parts) {
            System.out.println(part);

        }
        System.out.println("----");

        return null;
    }

    private static int odrediDubinu(String line) {
        int index = 0;
        while (line.charAt(index) == ' ') {
            index++;
        }
        return index;
    }
}
