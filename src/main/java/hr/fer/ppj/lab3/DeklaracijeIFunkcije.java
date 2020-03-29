package hr.fer.ppj.lab3;

import java.util.ArrayList;
import java.util.List;

public abstract class DeklaracijeIFunkcije {

    public static class DefinicijaFunkcije extends NezavrsniZnak {

        private Djelokrug.Funkcija funkcija;
        private Djelokrug djelokrug;

        public DefinicijaFunkcije() {
            super("<definicija_funkcije>");
        }

        public void dodajDjelokrug(Djelokrug djelokrug) {
            this.djelokrug = djelokrug;
        }

        @Override
        public void izracunajSvojstva() {
            ImeTipa imeTipa = (ImeTipa) djeca.get(0);
            imeTipa.izracunajSvojstva();

            if (imeTipa.getTip().contains("const")) {
                ispisGreskeIPrekid();
            }

            String imeFunkcije = ((ZavrsniZnak) djeca.get(1)).getReprezentacija();
            djelokrug.setIme(imeFunkcije);

            Djelokrug.Identifikator idn = getDjelokrug().nadiIdentifikatorLokalno(imeFunkcije);
            if (idn instanceof Djelokrug.Varijabla) {
                ispisGreskeIPrekid();
            }

            Djelokrug.Funkcija funkcija = getDjelokrug().nadiFunkcijuGlobalno(imeFunkcije);
            if (funkcija != null && funkcija.jeDefinirana()) {
                ispisGreskeIPrekid();
            }

            List<String> ulazni = new ArrayList<>();
            ListaParametara parametri = null;
            if (djeca.get(3) instanceof ZavrsniZnak) {
                ulazni.add("void");
            } else {
                parametri = (ListaParametara) djeca.get(3);
                parametri.izracunajSvojstva();
                ulazni.addAll(parametri.getTipovi());

                for (int i = 0; i < parametri.getTipovi().size(); i++) {
                    Djelokrug.Varijabla var = new Djelokrug.Varijabla(parametri.getImena().get(i), parametri.getTipovi().get(i));
                    djelokrug.dodajIdentifikator(var);
                }
            }
            String izlazni = imeTipa.getTip();

            if (funkcija != null) {
                if (!(funkcija.getUlazni().equals(ulazni)) || !(funkcija.getIzlazni().equals(izlazni))) {
                    ispisGreskeIPrekid();
                }
                funkcija.setDefinirana();
                this.funkcija = funkcija;
                djelokrug.setFunkcija(funkcija);
            } else {
                Djelokrug.Funkcija nova = new Djelokrug.Funkcija(imeFunkcije, ulazni, izlazni);
                nova.setDefinirana();
                this.funkcija = nova;
                djelokrug.setFunkcija(nova);
                getDjelokrug().dodajIdentifikator(nova);
            }

//			System.out.println(djelokrug.getFunkcija());

            Naredbe.SlozenaNaredba naredba = (Naredbe.SlozenaNaredba) djeca.get(5);
            if (parametri != null) {
                for (int i = 0; i < parametri.getTipovi().size(); i++) {
                    Djelokrug.Varijabla var = new Djelokrug.Varijabla(parametri.getImena().get(i), parametri.getTipovi().get(i));
//					System.out.println(djelokrug.nagomilajImena() + " " + var.getIme());

                    if (naredba.getDjelokrug().nadiVarijabluLokalno(var.getIme()) != null) {
//						System.out.println(var.getIme());
                        ispisGreskeIPrekid();
                    }
                    naredba.getDjelokrug().dodajIdentifikator(var);
                }
            }
            naredba.izracunajSvojstva();
        }

        @Override
        public boolean zapocinjeDjelokrug() {
            return true;
        }

        public Djelokrug.Funkcija dohvatiFunkciju() {
            return funkcija;
        }
    }


    public static class ListaParametara extends NezavrsniZnak {

        private List<String> tipovi = new ArrayList<>();
        private List<String> imena = new ArrayList<>();

        public ListaParametara() {
            super("<lista_parametara>");
        }

        public List<String> getTipovi() {
            return tipovi;
        }

        public List<String> getImena() {
            return imena;
        }

        @Override
        public void izracunajSvojstva() {
            int index = 0;
            if (sizeDjeca() == 3) {
                ListaParametara lista = (ListaParametara) djeca.get(0);
                lista.izracunajSvojstva();
                tipovi.addAll(lista.getTipovi());
                imena.addAll(lista.getImena());
                index = 2;
            }

            DeklaracijaParametra deklaracija = (DeklaracijaParametra) djeca.get(index);
            deklaracija.izracunajSvojstva();

            if (imena.contains(deklaracija.getImeParam())) {
                ispisGreskeIPrekid();
            }

            tipovi.add(deklaracija.getTip());
            imena.add(deklaracija.getImeParam());
        }
    }

    public static class DeklaracijaParametra extends NezavrsniZnak {

        private String tip;
        private String imeParam;

        public DeklaracijaParametra() {
            super("<deklaracija_parametra>");
        }

        public String getTip() {
            return tip;
        }

        public String getImeParam() {
            return imeParam;
        }

        @Override
        public void izracunajSvojstva() {
            ImeTipa imeTipa = (ImeTipa) djeca.get(0);
            imeTipa.izracunajSvojstva();

            if (imeTipa.getTip().equals("void")) {
                ispisGreskeIPrekid();
            }

            this.imeParam = ((ZavrsniZnak) djeca.get(1)).getReprezentacija();

            if (sizeDjeca() == 2) {
                this.tip = imeTipa.getTip();
            } else {
                this.tip = "niz(" + imeTipa.getTip() + ")";
            }
        }
    }

    public static class ListaDeklaracija extends NezavrsniZnak {

        public ListaDeklaracija() {
            super("<lista_deklaracija>");
        }

        @Override
        public void izracunajSvojstva() {
            if (sizeDjeca() == 1) {
                Deklaracija deklaracija = (Deklaracija) djeca.get(0);
                deklaracija.izracunajSvojstva();
            } else {
                ListaDeklaracija lista = (ListaDeklaracija) djeca.get(0);
                lista.izracunajSvojstva();

                Deklaracija deklaracija = (Deklaracija) djeca.get(1);
                deklaracija.izracunajSvojstva();
            }
        }
    }

    public static class Deklaracija extends NezavrsniZnak {

        public Deklaracija() {
            super("<deklaracija>");
        }

        @Override
        public void izracunajSvojstva() {
            ImeTipa imeTipa = (ImeTipa) djeca.get(0);
            imeTipa.izracunajSvojstva();

            ListaInitDeklaratora lista = (ListaInitDeklaratora) djeca.get(1);
            lista.setNTip(imeTipa.getTip());
            lista.izracunajSvojstva();
        }
    }


    public static class ListaInitDeklaratora extends NezavrsniZnak {

        private String nTip;

        public ListaInitDeklaratora() {
            super("<lista_init_deklaratora>");
        }

        public void setNTip(String nTip) {
            this.nTip = nTip;
        }

        @Override
        public void izracunajSvojstva() {
            int index = 0;
            if (sizeDjeca() == 3) {
                ListaInitDeklaratora lista = (ListaInitDeklaratora) djeca.get(0);
                lista.setNTip(nTip);
                lista.izracunajSvojstva();

                index = 2;
            }

            InitDeklarator deklarator = (InitDeklarator) djeca.get(index);
            deklarator.setNTip(nTip);
            deklarator.izracunajSvojstva();

        }
    }

    public static class InitDeklarator extends NezavrsniZnak {

        private String nTip;

        public InitDeklarator() {
            super("<init_deklarator>");
        }

        public void setNTip(String nTip) {
            this.nTip = nTip;
        }

        @Override
        public void izracunajSvojstva() {

            IzravniDeklarator izravni = (IzravniDeklarator) djeca.get(0);
            izravni.setNTip(nTip);
            izravni.izracunajSvojstva();

            if (sizeDjeca() == 1) {
                if (izravni.getTip().contains("const") && !izravni.getTip().contains("funkcija")) {
                    ispisGreskeIPrekid();
                }
            } else {
                Inicijalizator inicijalizator = (Inicijalizator) djeca.get(2);
                inicijalizator.izracunajSvojstva();

                if (jeBrojevniTip(izravni.getTip())) { //T ili const(T)
                    if (inicijalizator.getTip() == null) {
                        ispisGreskeIPrekid();
                    }

                    if (!implicitnoSvodi(inicijalizator.getTip(), izravni.getTip().replace("const(", "").replace(")", ""))) {
                        ispisGreskeIPrekid();
                    }
                } else if (jeBrojevniNiz(izravni.getTip())) {
                    if (inicijalizator.getSize() == null) {
                        ispisGreskeIPrekid();
                    }
                    if (inicijalizator.getSize() > izravni.getSize()) {
                        ispisGreskeIPrekid();
                    }

                    for (String t : inicijalizator.getTipovi()) {
                        if (!implicitnoSvodi(t, izravni.getTip().substring(4, izravni.getTip().length() - 1))) {
                            ispisGreskeIPrekid();
                        }
                    }
                } else {
                    ispisGreskeIPrekid();
                }
            }
        }
    }

    public static class IzravniDeklarator extends NezavrsniZnak {

        private String nTip;
        private String tip;
        private Integer brElem;

        public IzravniDeklarator() {
            super("<izravni_deklarator>");
        }

        public void setNTip(String nTip) {
            this.nTip = nTip;
        }

        public String getTip() {
            return tip;
        }

        public Integer getSize() {
            return brElem;
        }

        @Override
        public void izracunajSvojstva() {
            ZavrsniZnak znak = (ZavrsniZnak) djeca.get(0);

            if (sizeDjeca() == 1) {
                tip = nTip;

                if (nTip.equals("void")) {
                    ispisGreskeIPrekid();
                }

//				Djelokrug.Varijabla var = getDjelokrug().nadiVarijabluLokalno(znak.getReprezentacija());
//				if(var != null) {
//					ispisGreskeIPrekid();
//				}

                Djelokrug.Identifikator idn = getDjelokrug().nadiIdentifikatorLokalno(znak.getReprezentacija());
                if (idn != null) {
                    ispisGreskeIPrekid();
                }

                Djelokrug.Varijabla nova = new Djelokrug.Varijabla(znak.getReprezentacija(), tip);
                getDjelokrug().dodajIdentifikator(nova);
            } else if (((ZavrsniZnak) djeca.get(1)).getTip().equals("L_UGL_ZAGRADA")) {
                if (nTip.equals("void")) {
                    ispisGreskeIPrekid();
                }

                tip = "niz(" + nTip + ")";
                try {
                    //			brElem = Integer.parseInt(((ZavrsniZnak)djeca.get(2)).getReprezentacija());
                    brElem = procitajBroj(((ZavrsniZnak) djeca.get(2)).getReprezentacija());
                } catch (NumberFormatException e) {
                    ispisGreskeIPrekid();
                }
                Djelokrug.Varijabla var = getDjelokrug().nadiVarijabluLokalno(znak.getReprezentacija());
                if (var != null || brElem <= 0 || brElem > 1024) {
                    ispisGreskeIPrekid();
                }

                Djelokrug.Varijabla nova = new Djelokrug.Varijabla(znak.getReprezentacija(), tip);
                getDjelokrug().dodajIdentifikator(nova);
            } else {


//				Djelokrug.Funkcija fun = getDjelokrug().nadiFunkcijuGlobalno(znak.getReprezentacija());
                Djelokrug.Funkcija fun = getDjelokrug().nadiFunkcijuLokalno(znak.getReprezentacija());

                List<String> ulazni = new ArrayList<>();
                if (djeca.get(2) instanceof ZavrsniZnak) {
                    ulazni.add("void");
                } else {
                    ListaParametara lista = (ListaParametara) djeca.get(2);
                    lista.izracunajSvojstva();
                    ulazni.addAll(lista.getTipovi());
                }
                Djelokrug.Funkcija nova = new Djelokrug.Funkcija(znak.getReprezentacija(), ulazni, nTip);
                if (fun == null) {
                    getDjelokrug().dodajIdentifikator(nova);
                } else if (!fun.equals(nova)) {
                    ispisGreskeIPrekid();
                }

                tip = nova.toString();
            }
        }
    }

    public static class Inicijalizator extends NezavrsniZnak {

        private String tip;
        private Integer brElem;
        private List<String> tipovi;

        public Inicijalizator() {
            super("<inicijalizator>");
        }

        public String getTip() {
            return tip;
        }

        public Integer getSize() {
            return brElem;
        }

        public List<String> getTipovi() {
            return tipovi;
        }

        @Override
        public void izracunajSvojstva() {
            if (sizeDjeca() == 1) {
                IzrazPridruzivanja izraz = (IzrazPridruzivanja) djeca.get(0);
                izraz.izracunajSvojstva();

                //pokusava se dohvatiti niz znakova jer je to poseban slucaj
                ZavrsniZnak znak = dohvatiNizZnakova(izraz);

                if (znak != null && znak.getTip().equals("NIZ_ZNAKOVA")) {
                    String niz = obradiNiz(znak.getReprezentacija());
                    brElem = niz.length() + 1;
                    tipovi = new ArrayList<>();
                    tip = "niz(const(char))";
                    for (int i = 0; i < brElem; i++) {
                        tipovi.add("char");
                    }
                } else {
                    tip = izraz.getTip();
                }
            } else {
                ListaIzrazaPridruzivanja lista = (ListaIzrazaPridruzivanja) djeca.get(1);
                lista.izracunajSvojstva();
                brElem = lista.getSize();
                tipovi = lista.getTipovi();

            }
        }

        private ZavrsniZnak dohvatiNizZnakova(AbstractIzraz izraz) {
            if (izraz.getDjeca().size() != 1) {
                return null;
            }

            if (izraz.getDjeca().get(0) instanceof ZavrsniZnak) {
                return (ZavrsniZnak) izraz.getDjeca().get(0);
            }

            if (izraz.getDjeca().get(0) instanceof AbstractIzraz) {
                return dohvatiNizZnakova((AbstractIzraz) izraz.getDjeca().get(0));
            }

            return null;
        }

        private String obradiNiz(String s) {
            StringBuilder niz = new StringBuilder();

            for (int i = 1; i < s.length() - 1; i++) {
                if (s.charAt(i) != '\\') {
                    niz.append(s.charAt(i));
                } else {
                    niz.append(s.charAt(++i));
                }
            }

            return niz.toString();
        }
    }


    public static class ListaIzrazaPridruzivanja extends NezavrsniZnak {

        private List<String> tipovi = new ArrayList<>();
        private int brElem;

        public ListaIzrazaPridruzivanja() {
            super("<lista_izraza_pridruzivanja>");
        }

        public List<String> getTipovi() {
            return tipovi;
        }

        public int getSize() {
            return brElem;
        }

        @Override
        public void izracunajSvojstva() {
            if (sizeDjeca() == 1) {
                IzrazPridruzivanja izraz = (IzrazPridruzivanja) djeca.get(0);
                izraz.izracunajSvojstva();

                tipovi.add(izraz.getTip());
                brElem = 1;
            } else {
                ListaIzrazaPridruzivanja lista = (ListaIzrazaPridruzivanja) djeca.get(0);
                lista.izracunajSvojstva();

                tipovi.addAll(lista.getTipovi());
                brElem = lista.getSize();

                IzrazPridruzivanja izraz = (IzrazPridruzivanja) djeca.get(2);
                izraz.izracunajSvojstva();

                tipovi.add(izraz.getTip());
                brElem += 1;
            }
        }
    }
}
