package hr.fer.ppj.lab2;

import hr.fer.ppj.lab2.PomocneKlase.StanjeDKA;
import hr.fer.ppj.lab2.PomocneKlase.StanjeeNKA;
import hr.fer.ppj.lab2.PomocneKlase.ZnakGramatike;

import java.util.*;
import java.util.stream.Collectors;

public class DKA {

    public List<StanjeDKA> stanja = new ArrayList<>();
    private List<String> sviZnakovi;
    int s = 0;

    public DKA(eNKA eNKA) {
        srediZnakove(eNKA);
        List<ZnakGramatike> sz = new ArrayList<>(GSA.NEZAVRSNI_ZNAKOVI_GRAMATIKE);
        sz.addAll(GSA.ZAVRSNI_ZNAKOVI_GRAMATIKE);
        sz.remove(sz.size() - 1);
        sviZnakovi = sz.stream().map(z -> z.znak).collect(Collectors.toList());

        StanjeDKA q0 = new StanjeDKA();
        dodajZnakove(q0, eNKA.stanja.get(0));
        stanja.add(0, q0);
        stvoriPrijelaz(q0);

        int c = 1;
        List<StanjeDKA> donedid = new ArrayList<>();

        int fromS = 0;
        while (c != stanja.size()) {
            c = stanja.size();
            int fromD = donedid.size();
            for (StanjeDKA stanjeDKA : stanja.subList(fromS, c)) {
                if (!donedid.contains(stanjeDKA))
                    donedid.add(stanjeDKA);
                fromS++;
            }
            for (StanjeDKA stanjeDKA : donedid.subList(fromD, donedid.size()))
                stvoriPrijelaz(stanjeDKA);
        }

    }

    void stvoriPrijelaz(StanjeDKA q0) {
        for (String znak : sviZnakovi) {
            StanjeDKA qn = new StanjeDKA();

            for (StanjeeNKA e : q0.znakovi) {
                if (e.prijelazi.containsKey(znak)) {
                    dodajZnakove(qn, e.prijelazi.get(znak));
                }
            }

            if (qn.znakovi.isEmpty())
                continue;

            if (stanja.contains(qn))
                qn = stanja.get(stanja.indexOf(qn));

            q0.prijelazi.put(znak, qn);
            if (!stanja.contains(qn))
                stanja.add(qn);
        }
    }

    Map<StanjeeNKA, Set<StanjeeNKA>> sredeniZnakovi = new TreeMap<>();

    void srediZnakove(eNKA eNKA) {
        if (sredeniZnakovi.isEmpty()) {
            for (StanjeeNKA stanjeeNKA : eNKA.stanja)
                sredeniZnakovi.put(stanjeeNKA, new TreeSet<>(stanjeeNKA.epsilonPrijelazi));
        }

        int c = 0;
        while (c != sredeniZnakovi.toString().length()) {
            c = sredeniZnakovi.toString().length();
            for (StanjeeNKA stanjeeNKA : eNKA.stanja) {
                StanjeeNKA[] t = new StanjeeNKA[sredeniZnakovi.get(stanjeeNKA).size()];
                sredeniZnakovi.get(stanjeeNKA).toArray(t);
                for (StanjeeNKA nka : t) sredeniZnakovi.get(stanjeeNKA).addAll(sredeniZnakovi.get(nka));
            }
        }
    }

    void dodajZnakove(StanjeDKA q0, StanjeeNKA e) {
        if (sredeniZnakovi.containsKey(e)) {
            if (!q0.znakovi.contains(e))
                q0.znakovi.add(e);
            for (StanjeeNKA stanjeeNKA : sredeniZnakovi.get(e)) {
                if (!q0.znakovi.contains(stanjeeNKA))
                    q0.znakovi.add(stanjeeNKA);
            }
            return;
        }

        Set<StanjeeNKA> toadd = new TreeSet<>();
        List<StanjeeNKA> donedid = new ArrayList<>();
        donedid.add(e);
        while (!donedid.isEmpty()) {
            StanjeeNKA[] estanja = new StanjeeNKA[donedid.size()];
            donedid.toArray(estanja);
            donedid = new ArrayList<>();

            for (StanjeeNKA nka : estanja) {
                if (sredeniZnakovi.containsKey(nka)) {
                    for (StanjeeNKA stanjeeNKA : sredeniZnakovi.get(nka)) {
                        if (!q0.znakovi.contains(stanjeeNKA))
                            q0.znakovi.add(stanjeeNKA);
                    }
                    continue;
                }
                if (nka == null || toadd.contains(nka))
                    continue;

                toadd.add(nka);

                for (StanjeeNKA en : nka.epsilonPrijelazi) {
                    if (donedid.contains(en))
                        continue;

                    donedid.add(en);
                }
            }
        }

        for (StanjeeNKA stanjeeNKA : toadd) {
            if (!q0.znakovi.contains(stanjeeNKA))
                q0.znakovi.add(stanjeeNKA);
        }

        sredeniZnakovi.put(e, toadd);
    }
}
