package hr.fer.ppj.lab3;

import java.util.ArrayList;
import java.util.List;

public class Djelokrug {

    private Djelokrug roditelj;
    private String ime;
    private List<Identifikator> identifikatori = new ArrayList<>();
    private Funkcija funkcija;

    public Djelokrug(Djelokrug roditelj, String ime) {
        this.roditelj = roditelj;
        this.ime = ime;
        if (roditelj != null) {
            this.funkcija = roditelj.getFunkcija();
        }
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public Funkcija getFunkcija() {
        if (funkcija != null) {
            return funkcija;
        }

        if (roditelj == null) {
            return null;
        }

        return roditelj.getFunkcija();
    }

    public void setFunkcija(Funkcija funkcija) {
        this.funkcija = funkcija;
    }

    public String getIme() {
        return ime;
    }

    public Djelokrug getRoditelj() {
        return roditelj;
    }

    public Varijabla nadiVarijabluLokalno(String ime) {
        for (Identifikator i : identifikatori) {
            if (i instanceof Varijabla && i.getIme().equals(ime)) {
                return (Varijabla) i;
            }
        }
        return null;
    }

    public Varijabla nadiVarijabluGlobalno(String ime) {
        Varijabla var = nadiVarijabluLokalno(ime);

        if (var != null) {
            return var;
        }
        if (roditelj == null) {
            return null;
        }

        return roditelj.nadiVarijabluGlobalno(ime);
    }

    public Funkcija nadiFunkcijuLokalno(String ime) {
        for (Identifikator i : identifikatori) {
            if (i instanceof Funkcija && i.getIme().equals(ime)) {
                return (Funkcija) i;
            }
        }

        return null;
    }

    public Funkcija nadiFunkcijuGlobalno(String ime) {
        for (Identifikator i : identifikatori) {
            if (i instanceof Funkcija && i.getIme().equals(ime)) {
                return (Funkcija) i;
            }
        }

        if (roditelj == null)
            return null;

        return roditelj.nadiFunkcijuGlobalno(ime);
    }

    public Identifikator nadiIdentifikatorGlobalno(String ime) {
        for (Identifikator i : identifikatori) {
            if (i.getIme().equals(ime)) {
                return i;
            }
        }

        if (roditelj == null)
            return null;

        return roditelj.nadiIdentifikatorGlobalno(ime);
    }

    public Identifikator nadiIdentifikatorLokalno(String ime) {
        for (Identifikator i : identifikatori) {
            if (i.getIme().equals(ime)) {
                return i;
            }
        }

        return null;
    }

    public void dodajIdentifikator(Identifikator i) {
//		System.out.println(nagomilajImena() + ": " + i);
        identifikatori.add(i);
    }

    public String nagomilajImena() {
        if (roditelj == null) {
            return ime;
        }

        return roditelj.nagomilajImena() + "|" + ime;
    }

    public List<Identifikator> getIdentifikatori() {
        return identifikatori;
    }

    public static class Identifikator {
        private String ime;

        public Identifikator(String ime) {
            this.ime = ime;
        }

        public String getIme() {
            return ime;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Identifikator other = (Identifikator) obj;
            if (ime == null) {
                return other.ime == null;
            } else return ime.equals(other.ime);
        }
    }

    public static class Varijabla extends Identifikator {
        private String vrijednost;
        private String tip;

        public Varijabla(String ime, String tip) {
            super(ime);
            this.tip = tip;
        }

        public boolean isLIzraz() {
            return !tip.contains("const") && !tip.contains("niz") && !tip.contains("funkcija");
        }

        public Varijabla(String ime, String tip, String vrijednost) {
            super(ime);
            this.tip = tip;
            this.vrijednost = vrijednost;
        }

        public String getVrijednost() {
            return vrijednost;
        }

        public void setVrijednost(String vrijednost) {
            this.vrijednost = vrijednost;
        }

        public String getTip() {
            return tip;
        }

        @Override
        public String toString() {
            return tip + " " + getIme();
        }
    }

    public static class Funkcija extends Identifikator {
        private boolean definirana;
        private List<String> ulazni;
        private String izlazni;

        public Funkcija(String ime, List<String> ulazni, String izlazni) {
            super(ime);
            this.ulazni = ulazni;
            this.izlazni = izlazni;
        }

        public void setDefinirana() {
            this.definirana = true;
        }

        public boolean jeDefinirana() {
            return definirana;
        }


        public String getIzlazni() {
            return izlazni;
        }

        public List<String> getUlazni() {
            return ulazni;
        }


        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append("funkcija(");
            for (int i = 0; i < ulazni.size(); i++) {
                if (i != 0) {
                    sb.append(", ");
                }

                sb.append(ulazni.get(i));
            }
            sb.append(" ::= ");
            sb.append(izlazni).append(")");

            return sb.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
            Funkcija other = (Funkcija) obj;
            if (izlazni == null) {
                if (other.izlazni != null)
                    return false;
            } else if (!izlazni.equals(other.izlazni))
                return false;
            if (ulazni == null) {
                return other.ulazni == null;
            } else return ulazni.equals(other.ulazni);
        }
    }
}

