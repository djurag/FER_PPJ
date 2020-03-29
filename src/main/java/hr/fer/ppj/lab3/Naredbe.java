package hr.fer.ppj.lab3;


public abstract class Naredbe {


    public static abstract class AbstractNaredba extends NezavrsniZnak {

        private boolean unutarPetlje;

        private AbstractNaredba(String ime) {
            super(ime);
        }

        public boolean isUnutarPetlje() {
            return unutarPetlje;
        }

        public void setUnutarPetlje(boolean unutarPetlje) {
            this.unutarPetlje = unutarPetlje;
        }
    }

    public static class SlozenaNaredba extends AbstractNaredba {

        public SlozenaNaredba() {
            super("<slozena_naredba>");
        }

        @Override
        public void izracunajSvojstva() {
            if (sizeDjeca() == 3) {
                ListaNaredbi naredbe = (ListaNaredbi) djeca.get(1);
                naredbe.setUnutarPetlje(isUnutarPetlje());
                naredbe.izracunajSvojstva();
            } else {
                DeklaracijeIFunkcije.ListaDeklaracija deklaracije = (DeklaracijeIFunkcije.ListaDeklaracija) djeca.get(1);
                deklaracije.izracunajSvojstva();

                ListaNaredbi naredbe = (ListaNaredbi) djeca.get(2);
                naredbe.setUnutarPetlje(isUnutarPetlje());
                naredbe.izracunajSvojstva();
            }
        }

        @Override
        public boolean zapocinjeDjelokrug() {
            return true;
        }
    }

    public static class ListaNaredbi extends AbstractNaredba {

        public ListaNaredbi() {
            super("<lista_naredbi>");
        }

        @Override
        public void izracunajSvojstva() {
            if (sizeDjeca() == 1) {
                Naredba naredba = (Naredba) djeca.get(0);
                naredba.setUnutarPetlje(isUnutarPetlje());
                naredba.izracunajSvojstva();
            } else {
                ListaNaredbi lista = (ListaNaredbi) djeca.get(0);
                lista.setUnutarPetlje(isUnutarPetlje());
                lista.izracunajSvojstva();

                Naredba naredba = (Naredba) djeca.get(1);
                naredba.setUnutarPetlje(isUnutarPetlje());
                naredba.izracunajSvojstva();
            }
        }
    }

    public static class Naredba extends AbstractNaredba {

        public Naredba() {
            super("<naredba>");
        }

        @Override
        public void izracunajSvojstva() {
            AbstractNaredba naredba = (AbstractNaredba) djeca.get(0);
            naredba.setUnutarPetlje(isUnutarPetlje());
            naredba.izracunajSvojstva();
        }
    }

    public static class IzrazNaredba extends AbstractNaredba {

        private String tip;

        public IzrazNaredba() {
            super("<izraz_naredba>");
        }


        public String getTip() {
            return tip;
        }

        @Override
        public void izracunajSvojstva() {
            if (sizeDjeca() == 1) {
                tip = "int";
            } else {
                Izraz izraz = (Izraz) djeca.get(0);
                izraz.izracunajSvojstva();

                tip = izraz.getTip();
            }
        }
    }

    public static class NaredbaGrananja extends AbstractNaredba {

        public NaredbaGrananja() {
            super("<naredba_grananja>");
        }

        @Override
        public void izracunajSvojstva() {
            Izraz izraz = (Izraz) djeca.get(2);
            izraz.izracunajSvojstva();

            if (!implicitnoSvodi(izraz.getTip(), "int")) {
                ispisGreskeIPrekid();
            }

            Naredba prva = (Naredba) djeca.get(4);
            prva.setUnutarPetlje(isUnutarPetlje());
            prva.izracunajSvojstva();

            if (sizeDjeca() == 7) {
                Naredba druga = (Naredba) djeca.get(6);
                druga.setUnutarPetlje(isUnutarPetlje());
                druga.izracunajSvojstva();
            }
        }
    }

    public static class NaredbaPetlje extends AbstractNaredba {

        public NaredbaPetlje() {
            super("<naredba_petlje>");
        }

        @Override
        public void izracunajSvojstva() {
            if (sizeDjeca() == 5) {
                Izraz izraz = (Izraz) djeca.get(2);
                izraz.izracunajSvojstva();

                if (!implicitnoSvodi(izraz.getTip(), "int")) {
                    ispisGreskeIPrekid();
                }

                Naredba naredba = (Naredba) djeca.get(4);
                naredba.setUnutarPetlje(true);
                naredba.izracunajSvojstva();
            } else {
                IzrazNaredba izrazNaredba1 = (IzrazNaredba) djeca.get(2);
                izrazNaredba1.izracunajSvojstva();

                IzrazNaredba izrazNaredba2 = (IzrazNaredba) djeca.get(3);
                izrazNaredba2.izracunajSvojstva();

                if (!implicitnoSvodi(izrazNaredba2.getTip(), "int")) {
                    ispisGreskeIPrekid();
                }

                int index = 5;
                if (sizeDjeca() == 7) {
                    index = 6;

                    Izraz izraz = (Izraz) djeca.get(4);
                    izraz.izracunajSvojstva();
                }

                Naredba naredba = (Naredba) djeca.get(index);
                naredba.setUnutarPetlje(true);
                naredba.izracunajSvojstva();
            }
        }
    }

    public static class NaredbaSkoka extends AbstractNaredba {

        public NaredbaSkoka() {
            super("<naredba_skoka>");
        }

        @Override
        public void izracunajSvojstva() {
            ZavrsniZnak znak = (ZavrsniZnak) djeca.get(0);

            if (znak.getTip().equals("KR_CONTINUE") || znak.getTip().equals("KR_BREAK")) {
                if (!isUnutarPetlje()) {
                    ispisGreskeIPrekid();
                }
            } else {
                Djelokrug.Funkcija fun = getDjelokrug().getFunkcija();
                if (fun == null) {
                    ispisGreskeIPrekid();
                }

                if (sizeDjeca() == 2) {
                    assert fun != null;
                    if (!fun.getIzlazni().equals("void")) {
                        ispisGreskeIPrekid();
                    }
                } else {
                    Izraz izraz = (Izraz) djeca.get(1);
                    izraz.izracunajSvojstva();

                    assert fun != null;
                    if (!implicitnoSvodi(izraz.getTip(), fun.getIzlazni())) {
                        ispisGreskeIPrekid();
                    }
                }
            }
        }
    }

    public static class PrijevodnaJedinica extends NezavrsniZnak {

        public PrijevodnaJedinica() {
            super("<prijevodna_jedinica>");
        }

        @Override
        public void izracunajSvojstva() {
            if (sizeDjeca() == 1) {
                VanjskaDeklaracija deklaracija = (VanjskaDeklaracija) djeca.get(0);
                deklaracija.izracunajSvojstva();
            } else {
                PrijevodnaJedinica jedinica = (PrijevodnaJedinica) djeca.get(0);
                jedinica.izracunajSvojstva();

                VanjskaDeklaracija deklaracija = (VanjskaDeklaracija) djeca.get(1);
                deklaracija.izracunajSvojstva();
            }
        }
    }

    public static class VanjskaDeklaracija extends NezavrsniZnak {

        public VanjskaDeklaracija() {
            super("<vanjska_deklaracija>");
        }

        @Override
        public void izracunajSvojstva() {
            NezavrsniZnak znak = (NezavrsniZnak) djeca.get(0);
            znak.izracunajSvojstva();
        }
    }
}
