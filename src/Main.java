import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class AFDToken {
    private enum Estado {
        q0, q1, q2, q3, q4, qError
    }

    private final Map<String, String> tokens;
    private final char[] letras;
    private final char[] numeros;
    private final char[] simbolos;
    private final String[] simbolosCompuestos;

    public AFDToken() {
        tokens = new HashMap<>();
        inicializarTokens();
        letras = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
                'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
                'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '_'};
        numeros = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        simbolos = new char[]{'+', '-', '*', '/', '=', '>', '<', ':', ';', ',', '.', '(', ')', '{', '}', '[', ']',
                '#', '&', '|', '!', '"'};
        simbolosCompuestos = new String[]{"==", "!=", ">=", "<=", "&&", "||", "<<", ">>"};
    }

    private void inicializarTokens() {
        tokens.put("int", "1010");
        tokens.put("main", "1020");
        tokens.put("double", "1030");
        tokens.put("cout", "1040");
        tokens.put("cin", "1050");
        tokens.put("endl", "1060");
        tokens.put("system", "1070");
        tokens.put("return", "1080");
        tokens.put("using", "1090");
        tokens.put("namespace", "1100");
        tokens.put("switch", "1110");
        tokens.put("case", "1120");
        tokens.put("break", "1130");
        tokens.put("if", "1140");
        tokens.put("else", "1150");
        tokens.put("default", "1160");
        tokens.put("do", "1170");
        tokens.put("while", "1180");
        tokens.put("void", "1190");
        tokens.put("include", "1200");

        tokens.put("+", "2010");
        tokens.put("-", "2020");
        tokens.put("*", "2030");
        tokens.put("/", "2040");
        tokens.put("=", "2060");
        tokens.put(">", "2070");
        tokens.put("<", "2080");
        tokens.put(">=", "2090");
        tokens.put("<=", "2100");
        tokens.put("==", "2110");
        tokens.put("!=", "2120");
        tokens.put("&&", "2130");
        tokens.put("||", "2140");

        tokens.put(",", "3010");
        tokens.put(".", "3020");
        tokens.put(";", "3030");
        tokens.put(":", "3040");
        tokens.put("#", "3050");
        tokens.put("<<", "3060");
        tokens.put(">>", "3070");
        tokens.put("\"", "3080");
        tokens.put("{", "4010");
        tokens.put("}", "4020");
        tokens.put("(", "5010");
        tokens.put(")", "5020");
    }

    public String[] analizarLinea(String linea) {
        StringBuilder tokenActual = new StringBuilder();
        Estado estadoActual = Estado.q0;
        String[] resultado = new String[1024];
        int index = 0;

        for (int i = 0; i < linea.length(); i++) {
            char ch = linea.charAt(i);

            switch (estadoActual) {
                case q0 -> {
                    if (esLetra(ch)) {
                        tokenActual.append(ch);
                        estadoActual = Estado.q1;
                    } else if (esNumero(ch)) {
                        tokenActual.append(ch);
                        estadoActual = Estado.q2;
                    } else if (ch == '"') {
                        tokenActual.append(ch);
                        estadoActual = Estado.q4;
                    } else if (esSimbolo(ch)) {
                        tokenActual.append(ch);
                        estadoActual = Estado.q3;
                    } else if (Character.isWhitespace(ch)) {
                        // Ignorar espacios en blanco
                    } else {
                        // Caracter no válido, ir a qError
                        tokenActual.append(ch);
                        estadoActual = Estado.qError;
                    }
                }
                case q1 -> {
                    if (esLetra(ch) || esNumero(ch)) {
                        tokenActual.append(ch);
                    } else {
                        resultado[index++] = getToken(tokenActual.toString());
                        tokenActual.setLength(0);
                        estadoActual = Estado.q0;
                        i--;
                    }
                }
                case q2 -> {
                    if (esNumero(ch)) {
                        tokenActual.append(ch);
                    } else {
                        resultado[index++] = "7010(" + tokenActual + ")";
                        tokenActual.setLength(0);
                        estadoActual = Estado.q0;
                        i--;
                    }
                }
                case q3 -> {
                    String simboloCompuesto = tokenActual.toString() + ch;
                    if (esSimboloCompuesto(simboloCompuesto)) {
                        tokenActual.append(ch);
                        resultado[index++] = getToken(tokenActual.toString());
                        tokenActual.setLength(0);
                        estadoActual = Estado.q0;
                    } else {
                        resultado[index++] = getToken(tokenActual.toString());
                        tokenActual.setLength(0);
                        estadoActual = Estado.q0;
                        i--;
                    }
                }
                case q4 -> { // Estado de cadena
                    tokenActual.append(ch);
                    if (ch == '"') {
                        resultado[index++] = "3080(" + tokenActual + ")";
                        tokenActual.setLength(0);
                        estadoActual = Estado.q0;
                    }
                }
                case qError -> { // Estado de error
                    resultado[index++] = "ERROR(" + tokenActual + ")";
                    tokenActual.setLength(0);
                    estadoActual = Estado.q0;
                }
            }
        }

        if (!tokenActual.isEmpty()) {
            if (estadoActual == Estado.qError) {
                resultado[index++] = "ERROR(" + tokenActual + ")";
            } else {
                resultado[index++] = getToken(tokenActual.toString());
            }
        }

        String[] tokensFinal = new String[index];
        System.arraycopy(resultado, 0, tokensFinal, 0, index);
        return tokensFinal;
    }

    private boolean esLetra(char ch) {
        for (char letra : letras) {
            if (letra == ch) return true;
        }
        return false;
    }

    private boolean esNumero(char ch) {
        for (char numero : numeros) {
            if (numero == ch) return true;
        }
        return false;
    }

    private boolean esSimbolo(char ch) {
        for (char simbolo : simbolos) {
            if (simbolo == ch) return true;
        }
        return false;
    }

    private boolean esSimboloCompuesto(String str) {
        for (String simbolo : simbolosCompuestos) {
            if (simbolo.equals(str)) return true;
        }
        return false;
    }

    private String getToken(String str) {
        return tokens.getOrDefault(str, "6000") + "(" + str + ")";
    }
}

public class Main {
    public static void main(String[] args) {
        AFDToken afd = new AFDToken();

        try (BufferedReader lector = new BufferedReader(new FileReader("src/cpp/programa_cpp.txt"))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                String[] tokens = afd.analizarLinea(linea);
                for (String token : tokens) {
                    System.out.print(token + " ");
                }
                System.out.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al leer el archivo");
        }
    }
}
