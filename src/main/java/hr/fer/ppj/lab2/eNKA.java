package hr.fer.ppj.lab2;

import hr.fer.ppj.lab2.PomocneKlase.NezavrsniZnak;
import hr.fer.ppj.lab2.PomocneKlase.StanjeeNKA;
import hr.fer.ppj.lab2.PomocneKlase.ZnakGramatike;

import java.io.Serializable;
import java.util.*;

public class eNKA implements Serializable {
    private static final long serialVersionUID = 1L;

    public List<StanjeeNKA> stanja = new ArrayList<>();
    ZnakGramatike tocka = new ZnakGramatike("*");

    public eNKA() {
        NezavrsniZnak S = GSA.NEZAVRSNI_ZNAKOVI_GRAMATIKE.get(0);
        NezavrsniZnak N = new NezavrsniZnak(S.znak) {
            {
                produkcije.add(new ArrayList<>());
                produkcije.get(0).add(tocka);
                for (ZnakGramatike znak : S.produkcije.get(0))
                    produkcije.get(0).add(znak);
            }
        };

        StanjeeNKA q0 = new StanjeeNKA(N);
        q0.zapocinje.add(GSA.ZAVRSNI_ZNAKOVI_GRAMATIKE.get(GSA.ZAVRSNI_ZNAKOVI_GRAMATIKE.size() - 1));
        stanja.add(0, q0);
        srediPrijelaz(q0);
        srediEpsilonPrijelaz(q0);

        int c = 1;
        while (c != stanja.size()) {
            c = stanja.size();
            StanjeeNKA[] estanja = new StanjeeNKA[c];
            stanja.toArray(estanja);
            for (int i = 0; i < c; i++) {
                if (estanja[i].epsilonPrijelazi.isEmpty())
                    srediEpsilonPrijelaz(estanja[i]);
            }
            if (c == stanja.size()) {
                estanja = new StanjeeNKA[c];
                stanja.toArray(estanja);
                for (int i = 0; i < c; i++) {
                    if (estanja[i].epsilonPrijelazi.isEmpty())
                        srediEpsilonPrijelaz(estanja[i]);
                }
            }
        }
    }

    Map<StanjeeNKA, Set<StanjeeNKA>> sredeniPrijelazi = new TreeMap<>();

    void srediEpsilonPrijelaz(StanjeeNKA q0) {
        if (sredeniPrijelazi.containsKey(q0)) {
            for (StanjeeNKA stanjeeNKA : sredeniPrijelazi.get(q0)) {
                if (!q0.epsilonPrijelazi.contains(stanjeeNKA))
                    q0.epsilonPrijelazi.add(stanjeeNKA);
            }
            return;
        }

        int indexTocke = q0.trenutnoStanje.produkcije.get(0).indexOf(tocka);

        if (indexTocke == q0.trenutnoStanje.produkcije.get(0).size() - 1)
            return;

        Set<StanjeeNKA> toadd = new TreeSet<>();

        for (NezavrsniZnak nezavrsniZnak : GSA.NEZAVRSNI_ZNAKOVI_GRAMATIKE) {
            if (!q0.trenutnoStanje.produkcije.get(0).get(indexTocke + 1).znak.equals(nezavrsniZnak.znak))
                continue;

            for (List<ZnakGramatike> produkcija : nezavrsniZnak.produkcije) {
                NezavrsniZnak n = new NezavrsniZnak(nezavrsniZnak.znak) {
                    {
                        produkcije = new ArrayList<>();
                        produkcije.add(new ArrayList<>(produkcija));
                        produkcije.get(0)
                                .remove(GSA.ZAVRSNI_ZNAKOVI_GRAMATIKE.get(GSA.ZAVRSNI_ZNAKOVI_GRAMATIKE.size() - 1));
                        produkcije.get(0).add(0, tocka);
                    }
                };
                StanjeeNKA qn = new StanjeeNKA(n);

                if (indexTocke + 2 < q0.trenutnoStanje.produkcije.get(0).size())
                    qn.zapocinje.addAll(
                            GSA.ZAPOCINJE_ZNAKOM.get(q0.trenutnoStanje.produkcije.get(0).get(indexTocke + 2).znak));
                else
                    qn.zapocinje.addAll(q0.zapocinje);

                if (!qn.zapocinje.contains(GSA.ZAVRSNI_ZNAKOVI_GRAMATIKE.get(GSA.ZAVRSNI_ZNAKOVI_GRAMATIKE.size() - 1)))
                    qn.zapocinje.add(GSA.ZAVRSNI_ZNAKOVI_GRAMATIKE.get(GSA.ZAVRSNI_ZNAKOVI_GRAMATIKE.size() - 1));

                if (stanja.contains(qn))
                    qn = stanja.get(stanja.indexOf(qn));

                toadd.add(qn);

                if (!stanja.contains(qn)) {
                    stanja.add(qn);
                    srediPrijelaz(qn);
                }
            }
        }

        for (StanjeeNKA stanjeeNKA : toadd) {
            if (!q0.epsilonPrijelazi.contains(stanjeeNKA))
                q0.epsilonPrijelazi.add(stanjeeNKA);
        }

        sredeniPrijelazi.put(q0, toadd);
    }

    void srediPrijelaz(StanjeeNKA q0) {
        if (q0.trenutnoStanje.produkcije.get(0).indexOf(tocka) == q0.trenutnoStanje.produkcije.get(0).size() - 1)
            return;

        NezavrsniZnak n = new NezavrsniZnak(q0.trenutnoStanje.znak) {
            {
                produkcije = new ArrayList<>();
                produkcije.add(new ArrayList<>(q0.trenutnoStanje.produkcije.get(0)));
                produkcije.get(0).remove(tocka);
                produkcije.get(0).add(q0.trenutnoStanje.produkcije.get(0).indexOf(tocka) + 1, tocka);
            }
        };

        StanjeeNKA qn = new StanjeeNKA(n);
        qn.zapocinje = new ArrayList<>(q0.zapocinje);

        if (stanja.contains(qn))
            qn = stanja.get(stanja.indexOf(qn));

        q0.prijelazi.put(
                q0.trenutnoStanje.produkcije.get(0).get(q0.trenutnoStanje.produkcije.get(0).indexOf(tocka) + 1).znak,
                qn);

        if (!stanja.contains(qn)) {
            stanja.add(qn);
            srediPrijelaz(qn);
        }
    }
}
