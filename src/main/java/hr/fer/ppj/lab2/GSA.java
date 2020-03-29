package hr.fer.ppj.lab2;

import hr.fer.ppj.lab2.PomocneKlase.*;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.stream.Collectors;

public class GSA {

    static List<PomocneKlase.NezavrsniZnak> NEZAVRSNI_ZNAKOVI_GRAMATIKE = new ArrayList<>();
    static List<ZavrsniZnak> ZAVRSNI_ZNAKOVI_GRAMATIKE = new ArrayList<>();
    static List<ZavrsniZnak> SINK_ZAVRSNI_ZNAKOVI = new ArrayList<>();
    static Map<String, List<ZnakGramatike>> ZAPOCINJE_ZNAKOM = new TreeMap<>();
    static Map<String, List<ZnakGramatike>> ZAPOCINJE_IZRAVNO_ZNAKOM = new TreeMap<>();

    public static void main(String[] args) throws Exception {
        ulaz();
        dodajNoviPocetni();
        srediZapocinje();

        eNKA eNKA = new eNKA();
        System.out.println("enka " + eNKA.stanja.size());

        DKA DKA = new DKA(eNKA);
        System.out.println("dka " + DKA.stanja.size());

        Tablica akcija = napraviTablicuAkcija(DKA);
        Tablica novoStanje = napraviTablicuNovoStanje(DKA);

        FileOutputStream fileOut = new FileOutputStream("tablicaAkcija.ser");
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(akcija);
        out.close();
        fileOut.close();

        fileOut = new FileOutputStream("tablicaNovoStanje.ser");
        out = new ObjectOutputStream(fileOut);
        out.writeObject(novoStanje);
        out.close();
        fileOut.close();
    }

    static Tablica napraviTablicuAkcija(DKA DKA) {
        Tablica tablica = new Tablica("Akcija");

        for (ZavrsniZnak z : ZAVRSNI_ZNAKOVI_GRAMATIKE) {
            List<String> t = new ArrayList<>();
            for (int i = 0; i < DKA.stanja.size(); i++)
                t.add("-");
            tablica.polja.put(z.znak, t);
        }

        for (int i = 0; i < DKA.stanja.size(); i++) {
            for (StanjeeNKA nz : DKA.stanja.get(i).znakovi) {
                for (int j = 0; j < nz.trenutnoStanje.produkcije.get(0).size(); j++) {
                    if (nz.trenutnoStanje.produkcije.get(0).get(j).znak.equals("*")) {
                        if (j < nz.trenutnoStanje.produkcije.get(0).size() - 1) {
                            if (nz.trenutnoStanje.produkcije.get(0).get(j + 1) instanceof ZavrsniZnak) {
                                tablica.polja.get(nz.trenutnoStanje.produkcije.get(0).get(j + 1).znak).set(i,
                                        "Pomakni("
                                                + DKA.stanja.indexOf(DKA.stanja.get(i).prijelazi
                                                .get(nz.trenutnoStanje.produkcije.get(0).get(j + 1).znak))
                                                + ")");
                            }
                        } else {
                            for (String s : nz.zapocinje.stream().map(z -> z.znak).collect(Collectors.toList())) {
                                String ss = nz.trenutnoStanje.produkcije.get(0).stream().map(p -> p.znak)
                                        .collect(Collectors.toList()).toString().replace("[", "").replace("]", "")
                                        .replace(",", "").replace("*", "").trim();
                                if (ss.isEmpty())
                                    ss = "\"\"";

                                if (nz.trenutnoStanje.znak.equals("<%>"))
                                    tablica.polja.get(s).set(i, "Prihvati()");
                                else
                                    tablica.polja.get(s).set(i,
                                            "Reduciraj(" + nz.trenutnoStanje.znak + " ::= " + ss + ")");
                            }
                        }
                    }
                }
            }
        }

        return tablica;
    }

    static Tablica napraviTablicuNovoStanje(DKA DKA) {
        Tablica tablica = new Tablica("NovoStanje");

        for (NezavrsniZnak z : NEZAVRSNI_ZNAKOVI_GRAMATIKE) {
            List<String> t = new ArrayList<>();
            for (int i = 0; i < DKA.stanja.size(); i++)
                t.add("-");
            tablica.polja.put(z.znak, t);
        }

        for (int i = 0; i < DKA.stanja.size(); i++) {
            for (String z : DKA.stanja.get(i).prijelazi.keySet()) {
                if (NEZAVRSNI_ZNAKOVI_GRAMATIKE.stream().map(n -> n.znak).collect(Collectors.toList()).contains(z))
                    tablica.polja.get(z).set(i,
                            "Stavi(" + DKA.stanja.indexOf(DKA.stanja.get(i).prijelazi.get(z)) + ")");
            }
        }

        return tablica;
    }

    @SuppressWarnings("serial")
    static void dodajNoviPocetni() {
        NezavrsniZnak noviPocetni = new NezavrsniZnak("<%>");
        noviPocetni.produkcije.add(new ArrayList<ZnakGramatike>() {
            {
                add(NEZAVRSNI_ZNAKOVI_GRAMATIKE.get(0));
            }
        });
        NEZAVRSNI_ZNAKOVI_GRAMATIKE.add(0, noviPocetni);
    }

    static void srediZapocinje() {
        for (NezavrsniZnak z : NEZAVRSNI_ZNAKOVI_GRAMATIKE) {
            ZAPOCINJE_ZNAKOM.put(z.znak, zapocinje(z, new ArrayList<>()));
            ZAPOCINJE_IZRAVNO_ZNAKOM.put(z.znak, zapocinjeIzravno(z));
        }
        for (ZavrsniZnak z : ZAVRSNI_ZNAKOVI_GRAMATIKE) {
            ZAPOCINJE_ZNAKOM.put(z.znak, zapocinje(z, new ArrayList<>()));
            ZAPOCINJE_IZRAVNO_ZNAKOM.put(z.znak, zapocinjeIzravno(z));
        }
    }

    static List<ZnakGramatike> zapocinjeIzravno(ZnakGramatike znak) {
        List<ZnakGramatike> znakoviGramatike = new ArrayList<>();
        if (znak instanceof ZavrsniZnak)
            znakoviGramatike.add(znak);
        else {
            NezavrsniZnak nz = (NezavrsniZnak) znak;
            for (List<ZnakGramatike> zgs : nz.produkcije) {
                for (ZnakGramatike zg : zgs) {
                    if (zg.znak.equals("*"))
                        continue;
                    if (zg instanceof NezavrsniZnak)
                        break;
                    else {
                        if (!znakoviGramatike.contains(zg) && !zg.znak.equals("$"))
                            znakoviGramatike.add(zg);
                        break;
                    }
                }
            }
        }
        if (!znakoviGramatike.contains(ZAVRSNI_ZNAKOVI_GRAMATIKE.get(ZAVRSNI_ZNAKOVI_GRAMATIKE.size() - 1)))
            znakoviGramatike.add(ZAVRSNI_ZNAKOVI_GRAMATIKE.get(ZAVRSNI_ZNAKOVI_GRAMATIKE.size() - 1));
        return znakoviGramatike;
    }

    static List<ZnakGramatike> zapocinje(ZnakGramatike znak, List<ZnakGramatike> zapList) {
        List<ZnakGramatike> znakoviGramatike = new ArrayList<>();
        if (znak instanceof ZavrsniZnak)
            znakoviGramatike.add(znak);
        else {
            NezavrsniZnak nz = (NezavrsniZnak) znak;
            for (List<ZnakGramatike> zgs : nz.produkcije) {
                for (ZnakGramatike zg : zgs) {
                    if (zg.znak.equals("*") || zapList.contains(zg))
                        continue;
                    if (zg instanceof NezavrsniZnak) {
                        zapList.add(zg);
                        List<ZnakGramatike> zs = zapocinje(zg, zapList);
                        for (ZnakGramatike z : zs) {
                            if (!znakoviGramatike.contains(z) && !z.znak.equals("$"))
                                znakoviGramatike.add(z);
                        }
                    } else {
                        if (!znakoviGramatike.contains(zg) && !zg.znak.equals("$"))
                            znakoviGramatike.add(zg);
                        break;
                    }
                }
            }
        }
        if (!znakoviGramatike.contains(ZAVRSNI_ZNAKOVI_GRAMATIKE.get(ZAVRSNI_ZNAKOVI_GRAMATIKE.size() - 1)))
            znakoviGramatike.add(ZAVRSNI_ZNAKOVI_GRAMATIKE.get(ZAVRSNI_ZNAKOVI_GRAMATIKE.size() - 1));
        return znakoviGramatike;
    }

    static void ulaz() {
        Scanner scanner = new Scanner(System.in);

        String linija = scanner.nextLine();
        for (int i = 0; i < linija.substring(3).split(" ").length; i++)
            NEZAVRSNI_ZNAKOVI_GRAMATIKE.add(new NezavrsniZnak(linija.substring(3).split(" ")[i]));

        linija = scanner.nextLine();
        for (int i = 0; i < linija.substring(3).split(" ").length; i++)
            ZAVRSNI_ZNAKOVI_GRAMATIKE.add(new ZavrsniZnak(linija.substring(3).split(" ")[i]));

        linija = scanner.nextLine();
        for (int i = 0; i < linija.substring(3).split(" ").length; i++)
            SINK_ZAVRSNI_ZNAKOVI.add(new ZavrsniZnak(linija.substring(3).split(" ")[i]));

        ZAVRSNI_ZNAKOVI_GRAMATIKE.add(new ZavrsniZnak("$"));

        NezavrsniZnak trenutniZnak = null;
        while (scanner.hasNext()) {
            linija = scanner.nextLine();

            if (linija.charAt(0) != ' ') {
                for (NezavrsniZnak nz : NEZAVRSNI_ZNAKOVI_GRAMATIKE) {
                    if (nz.znak.equals(linija.trim())) {
                        trenutniZnak = nz;
                        break;
                    }
                }
            } else {
                List<ZnakGramatike> p = new ArrayList<>();
                for (String s : linija.substring(1).split(" ")) {
                    for (ZnakGramatike zg : NEZAVRSNI_ZNAKOVI_GRAMATIKE) {
                        if (zg.znak.equals(s)) {
                            p.add(zg);
                            break;
                        }
                    }
                    for (ZnakGramatike zg : ZAVRSNI_ZNAKOVI_GRAMATIKE) {
                        if (zg.znak.equals(s)) {
                            p.add(zg);
                            break;
                        }
                    }
                }
                assert trenutniZnak != null;
                trenutniZnak.produkcije.add(p);
            }
        }

        scanner.close();
    }
}
