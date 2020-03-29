package hr.fer.ppj.lab1;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class LAtemplate {

    static Map<String, List<Automat>> automati = null;
    static String stanje = "@POCETNO_STANJE@";

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        FileInputStream fileIn = new FileInputStream("automati.ser");
        ObjectInputStream in = new ObjectInputStream(fileIn);
        automati = (Map<String, List<Automat>>) in.readObject();
        in.close();
        fileIn.close();

        String tekst = ulaz();

        StringBuilder izlaz = new StringBuilder();
        String grupiraniZnakovi = "";
        int red = 1;
        int i = 0;

        // @M_LEN@

        while (i < tekst.length()) {
            resetirajAutomate();
            int duljina = 0;
            int indexPravila = -1;
            String infoGrupe = null;

            // @STANJA_I_AUTOMATI@

            if (infoGrupe != null) {
                duljina = Integer.parseInt(infoGrupe.split(" ")[0].trim());
                indexPravila = Integer.parseInt(infoGrupe.split(" ")[1].trim());
            }
            grupiraniZnakovi = tekst.substring(i, i + duljina);
            i += duljina;
            if (indexPravila == -1) {
                i++;
                continue;
            }

            // @INDEXI_PRAVILA@

        }
        System.out.println(izlaz.substring(0, izlaz.length() - 1));

    }

    static int maksimalnaGrupaZnakova(String s1, Automat[] a) {
        int duljina = -1;
        int[] indexi = new int[a.length];
        for (int i = 0; i < a.length; i++)
            indexi[i] = 0;

        for (int j = 0; j < s1.length(); j++) {
            for (int k = a.length - 1; k >= 0; k--) {
                prijelazi(a[k], s1.charAt(j));
                if (a[k].trenutnaStanja.contains(a[k].prihvacenoStanje)) {
                    if (duljina < j + 1 - indexi[k])
                        duljina = j + 1 - indexi[k];
                }
                if (a[k].trenutnaStanja.size() == 0) {
                    resetirajAutomat(a[k]);
                    indexi[k] = j + 1;
                }
            }
        }
        return duljina;
    }

    static String nadiGrupuZnakova(String tekst, int maksimalnaDuljina, Automat[] a, int[] indexiPravila,
                                   int index) {
        int duljina = -1;
        int indexPravila = -1;

        int i = tekst.length();
        if (index + maksimalnaDuljina < i)
            i = index + maksimalnaDuljina;

        Automat zadnjiAutomat = null;

        while (true) {
            String text = tekst.substring(index, i);
            for (int j = 0; j < text.length(); j++) {
                for (int k = a.length - 1; k >= 0; k--) {
                    prijelazi(a[k], text.charAt(j));
                    if (a[k].trenutnaStanja.contains(a[k].prihvacenoStanje)) {
                        zadnjiAutomat = a[k];
                        duljina = j + 1;
                        indexPravila = indexiPravila[k];
                    }
                }
            }
            if (zadnjiAutomat != null)
                return duljina + " " + indexPravila;
            i--;
            if (i == index)
                break;
        }
        return null;
    }

    static void resetirajAutomate() {
        for (List<Automat> automatil : automati.values()) {
            for (Automat automat : automatil) {
                automat.trenutnaStanja = new ArrayList<>();
                automat.trenutnaStanja.add(automat.pocetnoStanje);
            }
        }
        for (List<Automat> automatil : automati.values()) {
            for (Automat automat : automatil) {
                List<Integer> tempTrenutnaStanja = null;
                while (!automat.trenutnaStanja.equals(tempTrenutnaStanja)) {
                    tempTrenutnaStanja = automat.trenutnaStanja;
                    epsilonPrijelazi(automat);
                }
            }
        }
    }

    static void resetirajAutomat(Automat automat) {
        automat.trenutnaStanja = new ArrayList<>();
        automat.trenutnaStanja.add(automat.pocetnoStanje);

        List<Integer> tempTrenutnaStanja = null;
        while (!automat.trenutnaStanja.equals(tempTrenutnaStanja)) {
            tempTrenutnaStanja = automat.trenutnaStanja;
            epsilonPrijelazi(automat);
        }
    }

    static void epsilonPrijelazi(Automat automat) {
        Set<Integer> novi = new TreeSet<>();
        if (automat.trenutnaStanja.contains(automat.prihvacenoStanje))
            novi.add(automat.prihvacenoStanje);
        for (Integer ti : automat.trenutnaStanja) {
            if (automat.prijelazi.containsKey(ti))
                novi.add(ti);
            if (automat.epsilon.containsKey(ti))
                novi.addAll(automat.epsilon.get(ti));
        }
        automat.trenutnaStanja = new ArrayList<>(novi);
    }

    static void prijelazi(Automat automat, char c) {
        Set<Integer> novi = new TreeSet<>();
        for (Integer ti : automat.trenutnaStanja) {
            if (automat.epsilon.containsKey(ti))
                novi.add(ti);
            if (automat.prijelazi.containsKey(ti)) {
                if (automat.prijelazi.get(ti).containsKey(c))
                    novi.addAll(automat.prijelazi.get(ti).get(c));
            }
        }
        automat.trenutnaStanja = new ArrayList<>(novi);
        List<Integer> tempTrenutnaStanja = null;
        while (!automat.trenutnaStanja.equals(tempTrenutnaStanja)) {
            tempTrenutnaStanja = automat.trenutnaStanja;
            epsilonPrijelazi(automat);
        }
    }

    static String ulaz() {
        String input = "";
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext())
            input += scanner.nextLine() + "\n";
        scanner.close();
        return input;
    }

}
