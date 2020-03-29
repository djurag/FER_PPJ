package hr.fer.ppj.lab1;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class GLA {

    public static class Pravilo {
        String stanje;
        String regularniIzraz;
        List<String> akcije = new ArrayList<>();
    }

    static Map<String, String> REGULARNE_DEFINICIJE = new TreeMap<>();
    static List<String> STANJA = new ArrayList<>();
    static List<String> LEKSICKE_JEDINKE = new ArrayList<>();
    static List<Pravilo> PRAVILA = new ArrayList<>();
    static Map<String, List<Automat>> AUTOMATI = new TreeMap<>();

    public static void main(String[] args) throws Exception {
        ulaz();

        StringBuilder laIzlaz = new StringBuilder();
        for (String s : Files.readAllLines(Paths.get("LAtemplate.java")))
            laIzlaz.append(s).append("\n");

        laIzlaz = new StringBuilder(laIzlaz.toString().replace("@POCETNO_STANJE@", STANJA.get(0)));
        laIzlaz = new StringBuilder(laIzlaz.toString().replace("// @M_LEN@", maksimalnaGrupaZnakova()));
        laIzlaz = new StringBuilder(laIzlaz.toString().replace("// @STANJA_I_AUTOMATI@", automatiPoStanjima()));
        laIzlaz = new StringBuilder(laIzlaz.toString().replace("// @INDEXI_PRAVILA@", pravilaIAkcije()));

        System.out.println(laIzlaz);
    }

    static String maksimalnaGrupaZnakova() {
        String izlaz = "int maksimalnaDuljina = maksimalnaGrupaZnakova(tekst, new Automat[] { \n";
        StringBuilder allAutomati = new StringBuilder();
        for (String s : AUTOMATI.keySet()) {
            for (int i = 0; i < AUTOMATI.get(s).size(); i++)
                allAutomati.append("automati.get(\"").append(s).append("\").get(").append(i).append("),");
        }
        izlaz += allAutomati.substring(0, allAutomati.length() - 1) + " });\n";
        return izlaz;
    }

    static String automatiPoStanjima() {
        StringBuilder izlaz = new StringBuilder();
        for (String s : STANJA) {
            izlaz.append("if (stanje.equals(\"").append(s).append("\")) {\n");
            StringBuilder allAutomati = new StringBuilder();
            StringBuilder allIPravila = new StringBuilder();
            for (int i = 0; i < PRAVILA.size(); i++) {
                if (!PRAVILA.get(i).stanje.equals(s))
                    continue;
                allIPravila.append(i).append(",");
            }

            for (int i = 0; i < AUTOMATI.get(s).size(); i++)
                allAutomati.append("automati.get(\"").append(s).append("\").get(").append(i).append("),");

            izlaz.append("infoGrupe = nadiGrupuZnakova(tekst, maksimalnaDuljina, new Automat[] { ").append(allAutomati.substring(0, allAutomati.length() - 1)).append(" }, new int[] { ").append(allIPravila.substring(0, allIPravila.length() - 1)).append(" }, i);\n}\n");
        }
        return izlaz.toString();
    }

    static String pravilaIAkcije() {
        StringBuilder izlaz = new StringBuilder();
        for (int i = 0; i < PRAVILA.size(); i++) {
            izlaz.append("if (indexPravila == ").append(i).append(") {\n");
            StringBuilder izlaz2 = new StringBuilder();
            for (String s : PRAVILA.get(i).akcije) {
                if (s.contains("UDJI_U_STANJE"))
                    izlaz.append("stanje = \"").append(s.split(" ")[1].trim()).append("\";\n");
                if (s.contains("NOVI_REDAK"))
                    izlaz.append("red++;\n");
                if (s.contains("VRATI_SE")) {
                    izlaz.append("i -= duljina;\n");
                    izlaz.append("i += ").append(s.split(" ")[1].trim()).append(";\n");
                    izlaz.append("grupiraniZnakovi = grupiraniZnakovi.substring(0,").append(s.split(" ")[1].trim()).append(");\n");
                }
                if (LEKSICKE_JEDINKE.contains(s.trim()))
                    izlaz2.append("izlaz.append(\"").append(s.trim()).append(" \" + red + \" \" + grupiraniZnakovi + \"\\n\");\n");
            }
            izlaz.append(izlaz2).append("}\n");
        }
        return izlaz.toString();
    }

    static void ulaz() throws Exception {
        int korak = 0;
        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNext()) {
            String s = scanner.nextLine();

            if (korak == 0 && s.charAt(0) != '{')
                korak++;
            if (korak == 1 && !(s.charAt(0) == '%' && s.charAt(1) == 'X'))
                korak++;
            if (korak == 2 && !(s.charAt(0) == '%' && s.charAt(1) == 'L'))
                korak++;

            if (korak == 0) {
                String v = s.split(" ")[1];
                for (String tr : REGULARNE_DEFINICIJE.keySet())
                    v = v.replace("{" + tr + "}", "(" + REGULARNE_DEFINICIJE.get(tr) + ")");
                REGULARNE_DEFINICIJE.put(s.split(" ")[0].substring(1, s.indexOf(' ') - 1), v);
            }

            if (korak == 1) {
                STANJA.addAll(Arrays.asList(s.substring(3).split(" ")));
            }

            if (korak == 2) {
                Collections.addAll(LEKSICKE_JEDINKE, s.substring(3).split(" "));
            }

            if (korak == 3) {
                if (s.charAt(0) == '{' || s.charAt(0) == '}')
                    continue;

                if (s.charAt(0) == '<') {
                    Pravilo p = new Pravilo();
                    p.stanje = s.substring(1, s.indexOf('>'));
                    p.regularniIzraz = s.substring(s.indexOf('>') + 1);
                    for (String tr : REGULARNE_DEFINICIJE.keySet())
                        p.regularniIzraz = p.regularniIzraz.replace("{" + tr + "}",
                                "(" + REGULARNE_DEFINICIJE.get(tr) + ")");
                    PRAVILA.add(p);
                } else {
                    Pravilo p = PRAVILA.get(PRAVILA.size() - 1);
                    p.akcije.add(s);
                }
            }
        }
        scanner.close();

        for (Pravilo pravilo : PRAVILA) {
            Automat automat = new Automat();
            Automat.ParStanja ps = Automat.pretvori(pravilo.regularniIzraz, automat);
            automat.pocetnoStanje = ps.lijevo_stanje;
            automat.prihvacenoStanje = ps.desno_stanje;
            automat.trenutnaStanja.add(ps.lijevo_stanje);
            automat.regularniIzraz = pravilo.regularniIzraz;

            AUTOMATI.computeIfAbsent(pravilo.stanje, k -> new ArrayList<>());

            AUTOMATI.get(pravilo.stanje).add(automat);
        }

        FileOutputStream fileOut = new FileOutputStream("automati.ser");
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(AUTOMATI);
        out.close();
        fileOut.close();
    }
}
