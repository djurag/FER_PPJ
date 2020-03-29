package hr.fer.ppj.lab1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Automat implements Serializable {
    private static final long serialVersionUID = 1L;

    String regularniIzraz;
    int nStanja;
    int pocetnoStanje;
    int prihvacenoStanje;
    List<Integer> trenutnaStanja;
    Map<Integer, List<Integer>> epsilon;
    Map<Integer, Map<Character, List<Integer>>> prijelazi;

    public Automat() {
        regularniIzraz = "";
        nStanja = 0;
        pocetnoStanje = -1;
        prihvacenoStanje = -1;
        trenutnaStanja = new ArrayList<>();
        epsilon = new TreeMap<>();
        prijelazi = new TreeMap<>();
    }

    static class ParStanja {
        int lijevo_stanje;
        int desno_stanje;

        public ParStanja(int lijevo_stanje, int desno_stanje) {
            this.lijevo_stanje = lijevo_stanje;
            this.desno_stanje = desno_stanje;
        }
    }

    static void dodaj_epsilon_prijelaz(Automat automat, int lijevo_stanje, int desno_stanje) {
        automat.epsilon.computeIfAbsent(lijevo_stanje, k -> new ArrayList<>());
        automat.epsilon.get(lijevo_stanje).add(desno_stanje);
    }

    static void dodaj_prijelaz(Automat automat, int lijevo_stanje, int desno_stanje, char c) {
        automat.prijelazi.computeIfAbsent(lijevo_stanje, k -> new TreeMap<>());
        automat.prijelazi.get(lijevo_stanje).computeIfAbsent(c, k -> new ArrayList<>());
        automat.prijelazi.get(lijevo_stanje).get(c).add(desno_stanje);
    }

    static int novo_stanje(Automat automat) {
        automat.nStanja = automat.nStanja + 1;
        return automat.nStanja - 1;
    }

    static boolean je_operator(String izraz, int i) {
        int br = 0;
        while (i - 1 >= 0 && izraz.charAt(i - 1) == '\\') {
            br = br + 1;
            i = i - 1;
        }
        return br % 2 == 0;
    }

    static ParStanja pretvori(String izraz, Automat automat) {
        List<String> izbori = new ArrayList<>();
        int lI = 0;
        int br_zagrada = 0;
        for (int i = 0; i < izraz.length(); i++) {
            if (izraz.charAt(i) == '(' && je_operator(izraz, i))
                br_zagrada++;
            else if (izraz.charAt(i) == ')' && je_operator(izraz, i))
                br_zagrada--;
            else if (br_zagrada == 0 && izraz.charAt(i) == '|' && je_operator(izraz, i)) {
                izbori.add(izraz.substring(lI, i));
                lI = i + 1;
            }
        }
        if (!izbori.isEmpty())
            izbori.add(izraz.substring(lI));

        int lijevo_stanje = novo_stanje(automat);
        int desno_stanje = novo_stanje(automat);

        if (!izbori.isEmpty()) {
            for (String s : izbori) {
                ParStanja privremeno = pretvori("" + s, automat);
                dodaj_epsilon_prijelaz(automat, lijevo_stanje, privremeno.lijevo_stanje);
                dodaj_epsilon_prijelaz(automat, privremeno.desno_stanje, desno_stanje);
            }
        } else {
            boolean prefiksirano = false;
            int zadnje_stanje = lijevo_stanje;
            for (int i = 0; i < izraz.length(); i++) {
                int a, b;
                if (prefiksirano) {
                    prefiksirano = false;
                    char prijelazni_znak;
                    if (izraz.charAt(i) == 't')
                        prijelazni_znak = '\t';
                    else if (izraz.charAt(i) == 'n')
                        prijelazni_znak = '\n';
                    else if (izraz.charAt(i) == '_')
                        prijelazni_znak = ' ';
                    else
                        prijelazni_znak = izraz.charAt(i);

                    a = novo_stanje(automat);
                    b = novo_stanje(automat);
                    dodaj_prijelaz(automat, a, b, prijelazni_znak);
                } else {
                    if (izraz.charAt(i) == '\\') {
                        prefiksirano = true;
                        continue;
                    }
                    if (izraz.charAt(i) != '(') {
                        a = novo_stanje(automat);
                        b = novo_stanje(automat);
                        if (izraz.charAt(i) == '$')
                            dodaj_epsilon_prijelaz(automat, a, b);
                        else
                            dodaj_prijelaz(automat, a, b, izraz.charAt(i));
                    } else {
                        char[] ss = izraz.substring(i).toCharArray();
                        int bz = 0;
                        int j = 0;
                        for (int ii = 0; ii < ss.length; ii++) {
                            if (ss[ii] == '(')
                                bz++;
                            if (ss[ii] == ')')
                                bz--;
                            if (bz == 0) {
                                j = ii;
                                break;
                            }
                        }
                        j += i;
                        ParStanja privremeno = pretvori(izraz.substring(i + 1, j), automat);
                        a = privremeno.lijevo_stanje;
                        b = privremeno.desno_stanje;
                        i = j;
                    }
                }

                if (i + 1 < izraz.length() && izraz.charAt(i + 1) == '*') {
                    int x = a;
                    int y = b;
                    a = novo_stanje(automat);
                    b = novo_stanje(automat);
                    dodaj_epsilon_prijelaz(automat, a, x);
                    dodaj_epsilon_prijelaz(automat, y, b);
                    dodaj_epsilon_prijelaz(automat, a, b);
                    dodaj_epsilon_prijelaz(automat, y, x);
                    i++;
                }

                dodaj_epsilon_prijelaz(automat, zadnje_stanje, a);
                zadnje_stanje = b;
            }
            dodaj_epsilon_prijelaz(automat, zadnje_stanje, desno_stanje);
        }
        return new ParStanja(lijevo_stanje, desno_stanje);
    }
}
