package hr.fer.ppj.lab2;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class PomocneKlase {

    static class Tablica implements Serializable {
        private static final long serialVersionUID = 1L;

        String ime;
        Map<String, List<String>> polja = new TreeMap<>();

        public Tablica(String ime) {
            this.ime = ime;
        }

        public List<String> get(ZnakGramatike znak) {
            return polja.get(znak.znak);
        }

        public List<String> get(String kljuc) {
            return polja.get(kljuc);
        }
    }

    static class ZnakGramatike implements Serializable {
        String znak;

        public ZnakGramatike(String znak) {
            this.znak = znak;
        }

        @Override
        public String toString() {
            return znak;
        }
    }

    static class NezavrsniZnak extends ZnakGramatike {
        List<List<ZnakGramatike>> produkcije = new ArrayList<>();

        public NezavrsniZnak(String znak) {
            super(znak);
        }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder("\n" + znak + "\n");
            for (List<ZnakGramatike> zg : produkcije) {
                StringBuilder zgs = new StringBuilder();
                for (ZnakGramatike zz : zg)
                    zgs.append(zz.znak).append(" ");
                s.append("  ").append(zgs).append("\n");
            }
            return s.toString();
        }
    }

    static class ZavrsniZnak extends ZnakGramatike {
        public ZavrsniZnak(String znak) {
            super(znak);
        }

        @Override
        public String toString() {
            return znak;
        }
    }

    static class StanjeeNKA implements Serializable, Comparable<StanjeeNKA> {
        NezavrsniZnak trenutnoStanje;
        List<ZnakGramatike> zapocinje = new ArrayList<>();
        List<StanjeeNKA> epsilonPrijelazi = new ArrayList<>();
        Map<String, StanjeeNKA> prijelazi = new TreeMap<>();

        Set<StanjeeNKA> sviEpsilonPrijelaziSvihPrijelaza = new TreeSet<>();

        public StanjeeNKA(NezavrsniZnak nezavrsniZnak) {
            trenutnoStanje = nezavrsniZnak;
        }

        @Override
        public boolean equals(Object obj) {
            return ((StanjeeNKA) obj).trenutnoStanje.toString().equals(trenutnoStanje.toString())
                    && ((StanjeeNKA) obj).zapocinje.toString().equals(zapocinje.toString());
        }

        @Override
        public String toString() {
            return "\ntrenutno stanje: " + trenutnoStanje.znak + " -> "
                    + trenutnoStanje.produkcije.get(0).stream().map(zn -> zn.znak).collect(Collectors.toList()) + ". {"
                    + zapocinje + "}" + "\nepsilon prijelazi prema:" + epsilonPrijelaziToString() + "\nprijelazi prema:"
                    + prijelaziToString() + "\n";
        }

        String ToString() {
            return "\ntrenutno stanje: " + trenutnoStanje.znak + " -> "
                    + trenutnoStanje.produkcije.get(0).stream().map(zn -> zn.znak).collect(Collectors.toList()) + ". {"
                    + zapocinje + "}";
        }

        String epsilonPrijelaziToString() {
            return epsilonPrijelazi.stream().map(s -> "\n    " + s.ToString().substring(18).split("\n")[0])
                    .collect(Collectors.toList()).toString();
        }

        String prijelaziToString() {
            return prijelazi.keySet().stream()
                    .map(s -> "\n " + s + " : " + prijelazi.get(s).ToString().substring(18).split("\n")[0])
                    .collect(Collectors.toList()).toString();
        }

        @Override
        public int compareTo(StanjeeNKA o) {
            return toString().compareTo(o.toString());
        }
    }

    static class StanjeDKA implements Serializable {
        List<StanjeeNKA> znakovi = new ArrayList<>();
        Map<String, StanjeDKA> prijelazi = new TreeMap<>();

        @Override
        public String toString() {
            return "\ntrenutno stanje:\n " + ToString() + "\nprijelazi prema:\n " + prijelazi.keySet().stream()
                    .map(k -> k + ":\n " + prijelazi.get(k).ToString()).collect(Collectors.toList());
        }

        String ToString() {
            return "" + znakovi.stream()
                    .map(z -> " " + z.trenutnoStanje.znak + " -> "
                            + z.trenutnoStanje.produkcije.get(0).stream().map(zn -> zn.znak)
                            .collect(Collectors.toList())
                            + ". {" + z.zapocinje + "}" + "\n")
                    .collect(Collectors.toList());
        }

        @Override
        public boolean equals(Object obj) {
            StanjeDKA o = (StanjeDKA) obj;

            if (znakovi.size() != o.znakovi.size())
                return false;

            for (StanjeeNKA znak : znakovi) {
                if (!o.znakovi.contains(znak))
                    return false;
            }

            return true;
        }
    }
}
