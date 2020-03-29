package hr.fer.ppj.lab4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Section {
    String type;

    String c;
    String frisc;
    String dw;

    List<Section> inside;
    Section outside;

    public Section(Section outside) {
        this.outside = outside;
        type = "";
        c = "";
        frisc = "";
        dw = "";
        inside = new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        if (c.endsWith("{}"))
            string.append(c.substring(0, c.length() - 1));
        else
            string.append(c);

        for (Section i : inside)
            string.append("\n").append(String.join("\n", Arrays.asList(i.toString().split("\n")).stream().map(s -> s.isEmpty() ? "" : "  " + s).collect(Collectors.toList())));

        if (c.endsWith("{}"))
            string.append("}");

        return string.toString().replace("( ", "(").replace(" (", "(").replace(" )", ")").replace(" ;", ";").replace(" ,", ",");
    }

    void prevedi(String vars) {
        c = c.trim();

        if (!frisc.isEmpty()) {
            frisc += "\n";
            for (Section i : inside) {
                i.prevedi(vars);
                frisc += i.frisc;
            }
            return;
        }

        // int x = 5;

        // int f(int x, int y)
        if (c.startsWith("int ") && c.contains("(")) {
            frisc += "F_" + c.split(" ")[1].split("\\(")[0].trim() + "\n";
            frisc += "\tPUSH R0\n\tPUSH R1\n\tPUSH R2\n\tPUSH R3\n\tPUSH R4\n\tMOVE SR, R0\n\tPUSH R0\n\n";

            for (Section i : inside) {
                i.prevedi(vars);
                frisc += i.frisc;
            }

            frisc += "\tPOP R0\n\tMOVE R0, SR\n\tPOP R0\n\tPOP R1\n\tPOP R2\n\tPOP R3\n\tPOP R4\n\tRET\n\n";
        }

        // x = f(1, 2);
        if (!c.contains("int") && !c.startsWith("return") && c.contains("(") && c.contains(")") && c.contains(";")) {
            String functionName = c.split("\\(")[0].trim();
            String variables = "";
            if (c.contains(",")) {
                for (String v : vars.split("\n")) {
                    if (v.startsWith(functionName + ":")) {
                        variables = v.split(":")[1];
                        break;
                    }
                }
                for (int i = 0; i < c.split("\\(")[1].split("\\)")[0].trim().split(",").length; i++) {
                    String var = c.split("\\(")[1].split("\\)")[0].trim().split(",")[i].trim();
                    frisc += "\tMOVE %D " + var + ", R0\n";
                    frisc += "\tSTORE R0, (" + variables.split(",")[i].trim() + ")\n";
                }
            } else if (c.contains("()")) {

            } else if (!c.contains(",")) {
                for (String v : vars.split("\n")) {
                    if (v.startsWith(functionName + ":")) {
                        variables = v.split(":")[1];
                        break;
                    }
                }
                String var = c.split("\\(")[1].split("\\)")[0].trim();
                frisc += "\tMOVE %D " + var + ", R0\n";
                frisc += "\tSTORE R0, (" + variables.trim() + ")\n";
            }
            frisc += "\tCALL F_" + functionName + "\n";
            if (c.contains("=")) {
                c = c.split("=")[0].trim();
                frisc += "\tSTORE R5, (G_" + c + ")\n";
            }
            frisc += "\n";
        }

        // return 5;
        if (c.startsWith("return ") && c.endsWith(";") && !c.contains("(") && !c.contains("+") && !c.contains("-") && !c.contains("/") && !c.contains("*")) {
            try {
                frisc += "\tMOVE %D " + Integer.parseInt(c.split(" ")[1].trim()) + ", R5\n";
            } catch (Exception e) {
                frisc += "\tLOAD R5, (G_" + c.split(" ")[1].trim() + ")\n";
            }
            if (outside.c.contains("int main"))
                frisc += "\tMOVE R5, R6\n";
        }

        // return f(5);
        else if (c.startsWith("return ") && c.endsWith(";") && c.contains("(") && !c.contains("()") && !c.contains("+") && !c.contains("-") && !c.contains("/") && !c.contains("*")) {
            frisc += "\tCALL F_" + c.split(" ")[1].split("\\(")[0] + "\n";
            if (outside.c.contains("int main"))
                frisc += "\tMOVE R5, R6\n";
        }

        // return f();
        else if (c.startsWith("return ") && c.endsWith(";") && c.contains("()") && !c.contains("+") && !c.contains("-") && !c.contains("/") && !c.contains("*")) {
            frisc += "\tCALL F_" + c.split(" ")[1].split("\\(")[0] + "\n";
            frisc += "\tMOVE R5, R6\n";
        }

    }

    String izdvojiVarijable() {
        StringBuilder vars = new StringBuilder();
        String cc = c.trim();

        // int x = 5;
        if (cc.startsWith("int ") && cc.endsWith(";"))
            dw += "G_" + cc.split(" ")[1] + " DW %D " + (cc.contains("=") ? cc.split("=")[1].replace(";", "").trim() : "0") + "\n";

        // int f(int x, int y)
        if (cc.startsWith("int ") && cc.contains("(")) {
            vars.append(cc.split(" ")[1].split("\\(")[0].trim()).append(": ");
            cc = cc.split("\\(")[1].split("\\)")[0].trim();
            if (cc.contains(",")) {
                for (String v : cc.split(",")) {
                    dw += "G_" + v.trim().split(" ")[1] + " DW %D 0\n";
                    if (!vars.toString().trim().endsWith(":"))
                        vars.append(", ");
                    vars.append("G_").append(v.trim().split(" ")[1]);
                }
                vars.append("\n");
            } else if (!cc.equals("void"))
                dw += "G_" + (cc.split(" ").length > 1 ? cc.split(" ")[1] : cc) + " DW %D 0\n";
        }

        for (Section i : inside) {
            vars.append(i.izdvojiVarijable());
            dw += i.dw;
        }

        return vars.toString();
    }

}
