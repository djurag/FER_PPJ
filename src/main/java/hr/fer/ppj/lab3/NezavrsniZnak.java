package hr.fer.ppj.lab3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class NezavrsniZnak extends Node {

    private String ime;
    private Djelokrug djelokrug;

    private static final List<String> brojevniTipovi = new ArrayList<>();

    static {
        brojevniTipovi.add("int");
        brojevniTipovi.add("char");
        brojevniTipovi.add("const(int)");
        brojevniTipovi.add("const(char)");
    }

    public NezavrsniZnak(String ime) {
        this.ime = ime;
    }

    public String getIme() {
        return ime;
    }

    public void setDjelokrug(Djelokrug djelokrug) {
        this.djelokrug = djelokrug;
    }

    public abstract void izracunajSvojstva();

    public boolean zapocinjeDjelokrug() {
        return false;
    }

    public Djelokrug getDjelokrug() {
        return djelokrug;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ime).append(" ::=");
        for (Node n : djeca) {
            if (n instanceof ZavrsniZnak) {
                sb.append(" ").append(n.toString());
            } else if (n instanceof NezavrsniZnak) {
                sb.append(" ").append(((NezavrsniZnak) n).getIme());
            }
        }

        return sb.toString();
    }

    public void ispisGreskeIPrekid() {
        System.out.println(this.toString());
        System.exit(0);
    }

    public static boolean jeBrojevniTip(String tip) {
        return brojevniTipovi.contains(tip);
    }

    public static boolean jeBrojevniNiz(String tip) {
        for (String s : brojevniTipovi) {
            if (tip.equals("niz(" + s + ")")) return true;
        }

        return false;
    }

    public static int procitajBroj(String broj) {
        if (broj.startsWith("0x")) {
            return Integer.parseInt(broj.substring(2), 16);
        }

        if (broj.startsWith("0") && broj.length() != 1) {
            return Integer.parseInt(broj.substring(1), 8);
        }

        return Integer.parseInt(broj);
    }

    public static boolean eksplicitnoSvodi(String prvi, String drugi) {
        if (prvi.equals("void") || drugi.equals("void")) {
            return false;
        }

        if (prvi.contains("niz") || drugi.contains("niz")) {
            return false;
        }

        if ((prvi.equals("const(int)") || prvi.equals("int")) && (drugi.equals("char") || drugi.equals("const(char)"))) {
            return true;
        }

        return implicitnoSvodi(prvi, drugi);
    }

    public static boolean implicitnoSvodi(String prvi, String drugi) {

        if (prvi.contains("funkcija") || drugi.contains("funkcija")) {
            return false;
        }

        if (prvi.equals(drugi)) {
            return true;
        }

        if (prvi.equals("char") && drugi.equals("int")) {
            return true;
        }

        if (prvi.startsWith("const(")) {
            String novi = prvi.substring(6, prvi.length() - 1);
            return implicitnoSvodi(novi, drugi);
        }

        if (drugi.startsWith("const(")) {
            String novi = drugi.substring(6, drugi.length() - 1);
            return implicitnoSvodi(prvi, novi);

        }

        if (prvi.equals("niz(char)") && drugi.equals("niz(const(char))")) {
            return true;
        }

        if (prvi.equals("niz(int)") && drugi.equals("niz(const(int))")) {
            return true;
        }
        return prvi.equals("niz(char)") && (drugi.equals("niz(int)") || drugi.equals("niz(const(int))"));
    }

    public static abstract class AbstractIzraz extends NezavrsniZnak {
        protected String tip;
        protected boolean l_izraz;

        private AbstractIzraz(String ime) {
            super(ime);
        }

        public String getTip() {
            return tip;
        }

        public boolean isL_izraz() {
            return l_izraz;
        }
    }

    public static class PrimarniIzraz extends AbstractIzraz {

        public PrimarniIzraz() {
            super("<primarni_izraz>");
        }

        @Override
        public void izracunajSvojstva() {
            ZavrsniZnak znak = (ZavrsniZnak) this.djeca.get(0);
            switch (znak.getTip()) {
                case "IDN":
//				Djelokrug.Varijabla var = getDjelokrug().nadiVarijabluGlobalno(znak.getReprezentacija());
//				Djelokrug.Funkcija fun = getDjelokrug().nadiFunkcijuGlobalno(znak.getReprezentacija());

                    Djelokrug.Identifikator idn = getDjelokrug().nadiIdentifikatorGlobalno(znak.getReprezentacija());

                    if (idn == null) {
                        ispisGreskeIPrekid();
                    }

                    if (idn instanceof Djelokrug.Varijabla) {
                        Djelokrug.Varijabla var = (Djelokrug.Varijabla) idn;
                        this.tip = var.getTip();
                        //this.l_izraz = true;
                        this.l_izraz = var.isLIzraz();
                    } else {
                        assert idn != null;
                        this.tip = idn.toString();
                        this.l_izraz = false;
                    }
                    break;
                case "BROJ":
                    tip = "int";
                    l_izraz = false;

                    try {
                        procitajBroj(znak.getReprezentacija());
                    } catch (Exception e) {
                        ispisGreskeIPrekid();
                    }

                    break;
                case "ZNAK":
                    tip = "char";
                    l_izraz = false;
                    if (!provjeraIspravnostiZnaka(znak.getReprezentacija())) {
                        ispisGreskeIPrekid();
                    }
                    break;
                case "NIZ_ZNAKOVA":
                    tip = "niz(const(char))";
                    l_izraz = false;
                    if (!provjeraIspravnostiNiza(znak.getReprezentacija())) {
                        ispisGreskeIPrekid();
                    }
                    break;
                case "L_ZAGRADA":
                    Izraz izraz = (Izraz) djeca.get(1);
                    izraz.izracunajSvojstva();
                    this.tip = izraz.getTip();
                    this.l_izraz = izraz.isL_izraz();
                    break;
            }
        }

        private boolean provjeraIspravnostiNiza(String s) {
            String niz = s.substring(1, s.length() - 1); //mice navodnike ""

            for (int i = 0; i < niz.length(); i++) {
                char c = niz.charAt(i);

                if (c == '\\') {
                    if (i + 1 == niz.length()) {
                        return false;
                    }
                    char c2 = niz.charAt(++i);

                    if (!(c2 == 't' || c2 == 'n' || c2 == '0' || c2 == '\'' || c2 == '"' || c2 == '\\')) {
                        return false;
                    }
                } else if (c == '"') {
                    return false;
                }
            }

            return true;
        }


        private boolean provjeraIspravnostiZnaka(String s) {
            String znak = s.substring(1, s.length() - 1); //mice navodnike ''

            if (znak.length() == 1 && !znak.equals("\\")) {
                return true;
            } else if (znak.length() == 2) {
                if (znak.charAt(0) != '\\') {
                    return false;
                } else {
                    char c = znak.charAt(1);
                    return (c == 't' || c == 'n' || c == '0' || c == '\'' || c == '"' || c == '\\');
                }
            } else {
                return false;
            }
        }
    }

    public static class PostfiksIzraz extends AbstractIzraz {

        public PostfiksIzraz() {
            super("<postfiks_izraz>");
        }

        @Override
        public void izracunajSvojstva() {
            if (sizeDjeca() == 1) {
                PrimarniIzraz izraz = (PrimarniIzraz) djeca.get(0);
                izraz.izracunajSvojstva();

                this.tip = izraz.getTip();
                this.l_izraz = izraz.isL_izraz();
            } else if (sizeDjeca() == 2) {
                this.tip = "int";
                this.l_izraz = false;

                PostfiksIzraz postfiks = (PostfiksIzraz) djeca.get(0);
                postfiks.izracunajSvojstva();

                if (!postfiks.isL_izraz() || !implicitnoSvodi(postfiks.getTip(), "int")) {
                    ispisGreskeIPrekid();
                }
            } else if (djeca.get(2) instanceof Izraz) {
                PostfiksIzraz postfiks = (PostfiksIzraz) djeca.get(0);
                postfiks.izracunajSvojstva();

                if (!postfiks.getTip().startsWith("niz(")) {
                    ispisGreskeIPrekid();
                }

                Izraz izraz = (Izraz) djeca.get(2);
                izraz.izracunajSvojstva();

                if (!implicitnoSvodi(izraz.getTip(), "int")) {
                    ispisGreskeIPrekid();
                }

                String tipX = postfiks.getTip().substring(4, postfiks.getTip().length() - 1);
                this.tip = tipX;
                this.l_izraz = !tipX.startsWith("const") && !tip.contains("niz") && !tip.contains("funkcija");
            } else {
                PostfiksIzraz postfiks = (PostfiksIzraz) djeca.get(0);
                postfiks.izracunajSvojstva();

                l_izraz = false;

                List<String> ulazni = null;
                String izlazni = null;
                try {
                    String[] parts = postfiks.getTip().split(" ::= ");
                    ulazni = Arrays.asList(parts[0].replace("funkcija(", "").split(", "));
                    izlazni = parts[1].substring(0, parts[1].length() - 1);
                } catch (Exception e) {
                    ispisGreskeIPrekid();
                }

                if (sizeDjeca() == 3) {
                    assert ulazni != null;
                    if (!ulazni.get(0).equals("void")) {
                        ispisGreskeIPrekid();
                    }
                } else {
                    ListaArgumenata argumenti = (ListaArgumenata) djeca.get(2);
                    argumenti.izracunajSvojstva();

                    assert ulazni != null;
                    if (argumenti.getTipovi().size() != ulazni.size()) {
                        ispisGreskeIPrekid();
                    }
                    for (int i = 0; i < argumenti.getTipovi().size(); i++) {
                        String argTip = argumenti.getTipovi().get(i);
                        String paramTip = ulazni.get(i);

                        if (!implicitnoSvodi(argTip, paramTip)) {
                            ispisGreskeIPrekid();
                        }
                    }
                }

                this.tip = izlazni;
            }
        }
    }

    public static class ListaArgumenata extends NezavrsniZnak {
        List<String> tipovi = new ArrayList<>();

        public ListaArgumenata() {
            super("<lista_argumenata>");
        }

        public List<String> getTipovi() {
            return tipovi;
        }

        @Override
        public void izracunajSvojstva() {
            if (sizeDjeca() == 1) {
                IzrazPridruzivanja izraz = (IzrazPridruzivanja) djeca.get(0);
                izraz.izracunajSvojstva();

                tipovi.add(izraz.getTip());
            } else {
                ListaArgumenata argumenti = (ListaArgumenata) djeca.get(0);
                argumenti.izracunajSvojstva();

                tipovi.addAll(argumenti.getTipovi());

                IzrazPridruzivanja izraz = (IzrazPridruzivanja) djeca.get(2);
                izraz.izracunajSvojstva();

                tipovi.add(izraz.getTip());
            }
        }
    }

    public static class UnarniOperator extends NezavrsniZnak {

        public UnarniOperator() {
            super("<unarni_operator>");
        }

        @Override
        public void izracunajSvojstva() {
        }

    }

    public static class UnarniIzraz extends AbstractIzraz {

        public UnarniIzraz() {
            super("<unarni_izraz>");
        }

        @Override
        public void izracunajSvojstva() {
            if (sizeDjeca() == 1) {
                PostfiksIzraz izraz = (PostfiksIzraz) djeca.get(0);
                izraz.izracunajSvojstva();
                this.tip = izraz.getTip();
                this.l_izraz = izraz.isL_izraz();
            } else if (djeca.get(0) instanceof ZavrsniZnak) {
                tip = "int";
                l_izraz = false;

                UnarniIzraz izraz = (UnarniIzraz) djeca.get(1);
                izraz.izracunajSvojstva();

                if (!izraz.isL_izraz() || !implicitnoSvodi(izraz.tip, "int")) {
                    ispisGreskeIPrekid();
                }
            } else {
                tip = "int";
                l_izraz = false;

                CastIzraz izraz = (CastIzraz) djeca.get(1);
                izraz.izracunajSvojstva();

                if (!implicitnoSvodi(izraz.tip, "int")) {
                    ispisGreskeIPrekid();
                }
            }
        }
    }

    public static class CastIzraz extends AbstractIzraz {

        public CastIzraz() {
            super("<cast_izraz>");
        }

        @Override
        public void izracunajSvojstva() {
            if (sizeDjeca() == 1) {
                UnarniIzraz izraz = (UnarniIzraz) djeca.get(0);
                izraz.izracunajSvojstva();

                this.tip = izraz.getTip();
                this.l_izraz = izraz.isL_izraz();
            } else {
                ImeTipa imeTipa = (ImeTipa) djeca.get(1);
                CastIzraz castIzraz = (CastIzraz) djeca.get(3);

                imeTipa.izracunajSvojstva();
                castIzraz.izracunajSvojstva();

                this.tip = imeTipa.getTip();
                this.l_izraz = false;

                if (!eksplicitnoSvodi(castIzraz.getTip(), imeTipa.getTip())) {
                    ispisGreskeIPrekid();
                }
            }
        }
    }

    public static class ImeTipa extends AbstractIzraz {

        public ImeTipa() {
            super("<ime_tipa>");
        }

        @Override
        public void izracunajSvojstva() {
            if (sizeDjeca() == 1) {
                SpecifikatorTipa spec = (SpecifikatorTipa) djeca.get(0);
                spec.izracunajSvojstva();

                this.tip = spec.getTip();
            } else {
                SpecifikatorTipa spec = (SpecifikatorTipa) djeca.get(1);
                spec.izracunajSvojstva();
                this.tip = "const(" + spec.getTip() + ")";

                if (spec.getTip().equals("void")) {
                    ispisGreskeIPrekid();
                }
            }
        }
    }

    public static class SpecifikatorTipa extends AbstractIzraz {

        public SpecifikatorTipa() {
            super("<specifikator_tipa>");
        }

        @Override
        public void izracunajSvojstva() {
            ZavrsniZnak znak = (ZavrsniZnak) djeca.get(0);
            this.tip = znak.getReprezentacija();
        }
    }

    public static class MultiplikativniIzraz extends AbstractIzraz {

        public MultiplikativniIzraz() {
            super("<multiplikativni_izraz>");
        }

        @Override
        public void izracunajSvojstva() {
            if (sizeDjeca() == 1) {
                CastIzraz izraz = (CastIzraz) djeca.get(0);
                izraz.izracunajSvojstva();
                this.tip = izraz.getTip();
                this.l_izraz = izraz.isL_izraz();
            } else {
                this.tip = "int";
                this.l_izraz = false;

                MultiplikativniIzraz mulIzraz = (MultiplikativniIzraz) djeca.get(0);
                mulIzraz.izracunajSvojstva();

                if (!implicitnoSvodi(mulIzraz.getTip(), "int")) {
                    ispisGreskeIPrekid();
                }

                CastIzraz castIzraz = (CastIzraz) djeca.get(2);
                castIzraz.izracunajSvojstva();

                if (!implicitnoSvodi(castIzraz.getTip(), "int")) {
                    ispisGreskeIPrekid();
                }
            }
        }
    }

    public static class AditivniIzraz extends AbstractIzraz {

        public AditivniIzraz() {
            super("<aditivni_izraz>");
        }

        @Override
        public void izracunajSvojstva() {
            if (sizeDjeca() == 1) {
                MultiplikativniIzraz mulIzraz = (MultiplikativniIzraz) djeca.get(0);
                mulIzraz.izracunajSvojstva();
                this.tip = mulIzraz.getTip();
                this.l_izraz = mulIzraz.isL_izraz();
            } else {
                this.tip = "int";
                this.l_izraz = false;

                AditivniIzraz adIzraz = (AditivniIzraz) djeca.get(0);
                adIzraz.izracunajSvojstva();

                if (!implicitnoSvodi(adIzraz.getTip(), "int")) {
                    ispisGreskeIPrekid();
                }

                MultiplikativniIzraz mulIzraz = (MultiplikativniIzraz) djeca.get(2);
                mulIzraz.izracunajSvojstva();

                if (!implicitnoSvodi(mulIzraz.getTip(), "int")) {
                    ispisGreskeIPrekid();
                }
            }
        }
    }

    public static class OdnosniIzraz extends AbstractIzraz {

        public OdnosniIzraz() {
            super("<odnosni_izraz>");
        }

        @Override
        public void izracunajSvojstva() {
            if (sizeDjeca() == 1) {
                AditivniIzraz adIzraz = (AditivniIzraz) djeca.get(0);
                adIzraz.izracunajSvojstva();

                this.tip = adIzraz.getTip();
                this.l_izraz = adIzraz.isL_izraz();
            } else {
                this.tip = "int";
                this.l_izraz = false;

                OdnosniIzraz odIzraz = (OdnosniIzraz) djeca.get(0);
                odIzraz.izracunajSvojstva();

                if (!implicitnoSvodi(odIzraz.getTip(), "int")) {
                    ispisGreskeIPrekid();
                }

                AditivniIzraz adIzraz = (AditivniIzraz) djeca.get(2);
                adIzraz.izracunajSvojstva();

                if (!implicitnoSvodi(adIzraz.getTip(), "int")) {
                    ispisGreskeIPrekid();
                }
            }
        }
    }

    public static class JednakosniIzraz extends AbstractIzraz {

        public JednakosniIzraz() {
            super("<jednakosni_izraz>");
        }

        @Override
        public void izracunajSvojstva() {
            if (sizeDjeca() == 1) {
                OdnosniIzraz odnosni = (OdnosniIzraz) djeca.get(0);
                odnosni.izracunajSvojstva();

                this.tip = odnosni.getTip();
                this.l_izraz = odnosni.isL_izraz();
            } else {
                this.tip = "int";
                this.l_izraz = false;

                JednakosniIzraz jednakosni = (JednakosniIzraz) djeca.get(0);
                jednakosni.izracunajSvojstva();

                if (!implicitnoSvodi(jednakosni.getTip(), "int")) {
                    ispisGreskeIPrekid();
                }

                OdnosniIzraz odnosni = (OdnosniIzraz) djeca.get(2);
                odnosni.izracunajSvojstva();

                if (!implicitnoSvodi(odnosni.getTip(), "int")) {
                    ispisGreskeIPrekid();
                }
            }
        }
    }

    public static class BiniIzraz extends AbstractIzraz {

        public BiniIzraz() {
            super("<bin_i_izraz>");
        }

        @Override
        public void izracunajSvojstva() {
            if (sizeDjeca() == 1) {
                JednakosniIzraz jednakosni = (JednakosniIzraz) djeca.get(0);
                jednakosni.izracunajSvojstva();

                this.tip = jednakosni.getTip();
                this.l_izraz = jednakosni.isL_izraz();
            } else {
                this.tip = "int";
                this.l_izraz = false;

                BiniIzraz bini = (BiniIzraz) djeca.get(0);
                bini.izracunajSvojstva();

                if (!implicitnoSvodi(bini.getTip(), "int")) {
                    ispisGreskeIPrekid();
                }

                JednakosniIzraz jednakosni = (JednakosniIzraz) djeca.get(2);
                jednakosni.izracunajSvojstva();

                if (!implicitnoSvodi(jednakosni.getTip(), "int")) {
                    ispisGreskeIPrekid();
                }
            }
        }
    }

    public static class BinXiliIzraz extends AbstractIzraz {

        public BinXiliIzraz() {
            super("<bin_xili_izraz>");
        }

        @Override
        public void izracunajSvojstva() {
            if (sizeDjeca() == 1) {
                BiniIzraz bini = (BiniIzraz) djeca.get(0);
                bini.izracunajSvojstva();

                this.tip = bini.getTip();
                this.l_izraz = bini.isL_izraz();
            } else {
                this.tip = "int";
                this.l_izraz = false;

                BinXiliIzraz binXili = (BinXiliIzraz) djeca.get(0);
                binXili.izracunajSvojstva();

                if (!implicitnoSvodi(binXili.getTip(), "int")) {
                    ispisGreskeIPrekid();
                }

                BiniIzraz bini = (BiniIzraz) djeca.get(2);
                bini.izracunajSvojstva();

                if (!implicitnoSvodi(bini.getTip(), "int")) {
                    ispisGreskeIPrekid();
                }
            }
        }
    }

    public static class BinIliIzraz extends AbstractIzraz {

        public BinIliIzraz() {
            super("<bin_ili_izraz>");
        }

        @Override
        public void izracunajSvojstva() {
            if (sizeDjeca() == 1) {
                BinXiliIzraz binXili = (BinXiliIzraz) djeca.get(0);
                binXili.izracunajSvojstva();

                this.tip = binXili.getTip();
                this.l_izraz = binXili.isL_izraz();
            } else {
                this.tip = "int";
                this.l_izraz = false;

                BinIliIzraz binIli = (BinIliIzraz) djeca.get(0);
                binIli.izracunajSvojstva();

                if (!implicitnoSvodi(binIli.getTip(), "int")) {
                    ispisGreskeIPrekid();
                }

                BinXiliIzraz binXili = (BinXiliIzraz) djeca.get(2);
                binXili.izracunajSvojstva();

                if (!implicitnoSvodi(binXili.getTip(), "int")) {
                    ispisGreskeIPrekid();
                }
            }
        }
    }

    public static class LogiIzraz extends AbstractIzraz {

        public LogiIzraz() {
            super("<log_i_izraz>");
        }

        @Override
        public void izracunajSvojstva() {
            if (sizeDjeca() == 1) {
                BinIliIzraz binIli = (BinIliIzraz) djeca.get(0);
                binIli.izracunajSvojstva();

                this.tip = binIli.getTip();
                this.l_izraz = binIli.isL_izraz();
            } else {
                this.tip = "int";
                this.l_izraz = false;

                LogiIzraz logi = (LogiIzraz) djeca.get(0);
                logi.izracunajSvojstva();

                if (!implicitnoSvodi(logi.getTip(), "int")) {
                    ispisGreskeIPrekid();
                }

                BinIliIzraz binIli = (BinIliIzraz) djeca.get(2);
                binIli.izracunajSvojstva();

                if (!implicitnoSvodi(binIli.getTip(), "int")) {
                    ispisGreskeIPrekid();
                }
            }
        }
    }

    public static class LogIliIzraz extends AbstractIzraz {

        public LogIliIzraz() {
            super("<log_ili_izraz>");
        }

        @Override
        public void izracunajSvojstva() {
            if (sizeDjeca() == 1) {
                LogiIzraz logi = (LogiIzraz) djeca.get(0);
                logi.izracunajSvojstva();

                this.tip = logi.getTip();
                this.l_izraz = logi.isL_izraz();
            } else {
                this.tip = "int";
                this.l_izraz = false;

                LogIliIzraz logIli = (LogIliIzraz) djeca.get(0);
                logIli.izracunajSvojstva();

                if (!implicitnoSvodi(logIli.getTip(), "int")) {
                    ispisGreskeIPrekid();
                }

                LogiIzraz logi = (LogiIzraz) djeca.get(2);
                logi.izracunajSvojstva();

                if (!implicitnoSvodi(logi.getTip(), "int")) {
                    ispisGreskeIPrekid();
                }
            }
        }
    }

    public static class IzrazPridruzivanja extends AbstractIzraz {

        public IzrazPridruzivanja() {
            super("<izraz_pridruzivanja>");
        }

        @Override
        public void izracunajSvojstva() {
            if (sizeDjeca() == 1) {
                LogIliIzraz logIli = (LogIliIzraz) djeca.get(0);
                logIli.izracunajSvojstva();
                this.tip = logIli.getTip();
                this.l_izraz = logIli.isL_izraz();
            } else {
                PostfiksIzraz postfiks = (PostfiksIzraz) djeca.get(0);
                postfiks.izracunajSvojstva();

                this.tip = postfiks.getTip();
                this.l_izraz = false;

                if (!postfiks.isL_izraz()) {
                    ispisGreskeIPrekid();
                }

                IzrazPridruzivanja pridruzivanje = (IzrazPridruzivanja) djeca.get(2);
                pridruzivanje.izracunajSvojstva();

                if (!implicitnoSvodi(pridruzivanje.getTip(), postfiks.getTip())) {
                    ispisGreskeIPrekid();
                }
            }
        }
    }

    public static class Izraz extends AbstractIzraz {

        public Izraz() {
            super("<izraz>");
        }

        @Override
        public void izracunajSvojstva() {
            if (sizeDjeca() == 1) {
                IzrazPridruzivanja pridruzivanje = (IzrazPridruzivanja) djeca.get(0);
                pridruzivanje.izracunajSvojstva();
                this.tip = pridruzivanje.getTip();
                this.l_izraz = pridruzivanje.isL_izraz();
            } else {
                Izraz izraz = (Izraz) djeca.get(0);
                izraz.izracunajSvojstva();

                IzrazPridruzivanja pridruzivanje = (IzrazPridruzivanja) djeca.get(2);
                pridruzivanje.izracunajSvojstva();

                this.tip = pridruzivanje.getTip();
                this.l_izraz = false;
            }
        }
    }
}
