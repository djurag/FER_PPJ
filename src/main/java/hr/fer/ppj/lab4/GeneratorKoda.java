package hr.fer.ppj.lab4;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class GeneratorKoda {
    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        List<String> lines = new ArrayList<>();
        while (sc.hasNext())
            lines.add(sc.nextLine());
        sc.close();

        lines = lines.stream().map(String::trim).filter(l -> !l.startsWith("<")).map(l -> l.split(" ")[2]).collect(Collectors.toList());

        Section main = new Section(null);
        main.frisc = "\tMOVE 40000, SP\n\tCALL F_main\n\tHALT\n\n";
        Section all = srediSection(main, lines);
        main.inside.add(all);

        //System.out.println(prevoditelj(main));
        Files.write(Paths.get("a.frisc"), prevoditelj(main).getBytes());
    }

    static String prevoditelj(Section main) {
        String vars = main.izdvojiVarijable();
        for (Section i : main.inside) {
            i.prevedi(vars);
            main.frisc += i.frisc;
        }
        return main.frisc + "\n\n" + main.dw;
    }

    static Section srediSection(Section outside, List<String> lines) {
        Section currentSection = new Section(outside);
        List<String> inside = new ArrayList<>();
        int insideZagrade = 0;

        for (String znak : lines) {
            if (currentSection.c.isEmpty()) {
                switch (znak) {
                    case "int":
                    case "char":
                    case "void":
                        currentSection.c += znak + " ";
                        currentSection.type = "deklaracija";
                        break;
                    case "if":
                    case "else":
                        currentSection.c += znak + " ";
                        currentSection.type = "uvijet";
                        break;
                    case "for":
                        currentSection.c += znak + " ";
                        currentSection.type = "forPetlja";
                        break;
                    case "while":
                        currentSection.c += znak + " ";
                        currentSection.type = "whilePetlja";
                        break;
                    case "return":
                        currentSection.c += znak + " ";
                        currentSection.type = "naredba";
                        break;
                    default:
                        currentSection.c += znak + " ";
                        break;
                }
            } else if (currentSection.type.equals("deklaracija")) {
                if (znak.equals("{")) {
                    currentSection.c += znak;
                    if (!currentSection.c.contains("="))
                        currentSection.type = "funkcija";
                    insideZagrade = 1;
                } else if (znak.equals(";")) {
                    currentSection.c += znak;
                    if (currentSection.c.contains("="))
                        currentSection.type = "deklaracijaVarijable";
                    else if (currentSection.c.contains("("))
                        currentSection.type = "deklaracijaFunkcije";
                    currentSection.outside.inside.add(currentSection);
                    currentSection = new Section(outside);
                } else
                    currentSection.c += znak + " ";
            } else if (currentSection.type.equals("funkcija")) {
                if (znak.equals("{")) {
                    insideZagrade++;
                    inside.add(znak);
                } else if (znak.equals("}")) {
                    insideZagrade--;
                    if (insideZagrade == 0) {
                        currentSection.c += znak;
                        currentSection.inside.add(srediSection(currentSection, inside));
                        currentSection.outside.inside.add(currentSection);
                        inside.clear();
                        insideZagrade = 0;
                        currentSection = new Section(outside);
                    } else
                        inside.add(znak);
                } else
                    inside.add(znak);
            } else if (currentSection.type.equals("naredba")) {
                if (znak.equals(";")) {
                    currentSection.c += znak;
                    currentSection.outside.inside.add(currentSection);
                    currentSection = new Section(outside);
                } else
                    currentSection.c += znak + " ";
            } else if (currentSection.type.equals("uvijet") || currentSection.type.equals("forPetlja") || currentSection.type.equals("whilePetlja")) {
                if (insideZagrade > 0) {
                    if (znak.equals("{")) {
                        insideZagrade++;
                        inside.add(znak);
                    } else if (znak.equals("}")) {
                        insideZagrade--;
                        if (insideZagrade == 0) {
                            currentSection.c += znak;
                            currentSection.inside.add(srediSection(currentSection, inside));
                            currentSection.outside.inside.add(currentSection);
                            inside.clear();
                            insideZagrade = 0;
                            currentSection = new Section(outside);
                        } else
                            inside.add(znak);
                    } else
                        inside.add(znak);
                } else {
                    if (znak.equals("{"))
                        insideZagrade++;
                    currentSection.c += znak + (znak.equals("{") ? "" : " ");
                }
            } else {
                currentSection.c += znak + " ";
                switch (znak) {
                    case "=":
                        currentSection.type = "pridruzivanje";
                        break;
                    case "(":
                        currentSection.type = "pozivFunkcije";
                        break;
                    case ";":
                        currentSection.outside.inside.add(currentSection);
                        currentSection = new Section(outside);
                        break;
                }
            }
        }

        return currentSection;
    }
}

