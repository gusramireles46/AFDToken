import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class AFDToken {
    private enum Estado {
        q0, q1, q2, q3, q4, q5, q6, q7, q8, q9, q10, q11, q12, q13, qError
    }

    private final Map<String, String> tokens;
    private final Map<String, Integer> identificadores;
    private int contadorIdentificadores;
    private final char[] letras;
    private final char[] numeros;
    private final char[] simbolos;
    private final String[] simbolosCompuestos;

    public AFDToken() {
        tokens = new HashMap<>();
        identificadores = new HashMap<>();
        contadorIdentificadores = 6000;
        inicializarTokens();
        letras = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
                'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
                'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '_', '$'};
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
        String[] resultado = new String[1000000];
        int index = 0;

        for (int i = 0; i < linea.length(); i++) {
            char ch = linea.charAt(i);

            switch (estadoActual) {
                case q0 -> {
                    if (esCaracterValido(ch)) {
                        resultado[index++] = "ERROR(" + ch + ")";
                    } else if (esNumero(ch)) {
                        tokenActual.append(ch);
                        estadoActual = Estado.q2; // Maneja números
                    } else if (esLetra(ch)) {
                        tokenActual.append(ch);
                        estadoActual = Estado.q1; // Maneja identificadores
                    } else if (ch == '"') {
                        tokenActual.append(ch);
                        estadoActual = Estado.q4; // Maneja cadenas
                    } else if (esSimbolo(ch)) {
                        tokenActual.append(ch);
                        estadoActual = Estado.q3; // Maneja símbolos
                    } else if (Character.isWhitespace(ch)) {
                        // Ignorar espacios
                    } else {
                        tokenActual.append(ch);
                        estadoActual = Estado.qError;
                    }
                }

                case q1 -> { // Maneja identificadores y palabras reservadas
                    if (esLetra(ch) || esNumero(ch) || ch == '_' || ch == '$') {
                        tokenActual.append(ch);
                    } else if (esCaracterValido(ch)) {
                        tokenActual.append(ch);
                        estadoActual = Estado.qError;
                    } else {
                        String palabra = tokenActual.toString();
                        if (tokens.containsKey(palabra)) {
                            // Es una palabra reservada
                            resultado[index++] = tokens.get(palabra) + "(" + palabra + ")";
                        } else if (esIdentificadorValido(palabra)) {
                            // Es un identificador válido
                            if (!identificadores.containsKey(palabra)) {
                                identificadores.put(palabra, contadorIdentificadores+=5);
                            }
                            resultado[index++] = identificadores.get(palabra) + "(" + palabra + ")";
                        } else {
                            resultado[index++] = "ERROR(" + palabra + ")";
                        }
                        tokenActual.setLength(0);
                        estadoActual = Estado.q0;
                        i--;
                    }
                }

                case q2 -> { // Maneja números enteros
                    if (esNumero(ch)) {
                        tokenActual.append(ch);
                    } else if (ch == '.') {
                        tokenActual.append(ch);
                        estadoActual = Estado.q5;
                    } else if (esCaracterValido(ch) || esLetra(ch)) {
                        tokenActual.append(ch);
                        estadoActual = Estado.qError;
                    } else {
                        String numero = tokenActual.toString();
                        if (esNumeroValido(numero)) {
                            resultado[index++] = "7000(" + numero + ")";
                        } else {
                            resultado[index++] = "ERROR(" + numero + ")";
                        }
                        tokenActual.setLength(0);
                        estadoActual = Estado.q0;
                        i--;
                    }
                }

                case q5 -> { // Maneja la parte decimal de los flotantes
                    if (esNumero(ch)) {
                        tokenActual.append(ch);
                    } else if (ch == '.' || esCaracterValido(ch) || esLetra(ch)) {
                        // Detecta un segundo punto o un carácter inválido en un flotante
                        tokenActual.append(ch);
                        estadoActual = Estado.qError; // Enviar al estado de error
                    } else {
                        String numero = tokenActual.toString();
                        if (esNumeroValido(numero)) {
                            resultado[index++] = "8000(" + numero + ")";
                        } else {
                            resultado[index++] = "ERROR(" + numero + ")";
                        }
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
                case q4 -> { // Maneja cadenas
                    tokenActual.append(ch);
                    if (ch == '"') {
                        // Fin de la cadena
                        resultado[index++] = "3080(" + tokenActual + ")";
                        tokenActual.setLength(0);
                        estadoActual = Estado.q0;
                    }
                }

                case qError -> {
                    if (!Character.isWhitespace(ch) && ch != ';' && ch != ',') {
                        tokenActual.append(ch);
                    } else {
                        resultado[index++] = "ERROR(" + tokenActual + ")";
                        tokenActual.setLength(0);
                        estadoActual = Estado.q0;
                        i--;
                    }
                }

            }
        }

        if (!tokenActual.isEmpty()) {
            String palabra = tokenActual.toString();
            if (estadoActual == Estado.q1) {
                if (tokens.containsKey(palabra)) {
                    resultado[index++] = tokens.get(palabra) + "(" + palabra + ")";
                } else if (esIdentificadorValido(palabra)) {
                    if (!identificadores.containsKey(palabra)) {
                        identificadores.put(palabra, contadorIdentificadores);
                    }
                    resultado[index++] = identificadores.get(palabra) + "(" + palabra + ")";
                } else {
                    resultado[index++] = "ERROR(" + palabra + ")";
                }
            } else if (estadoActual == Estado.q2) {
                resultado[index++] = "7000(" + tokenActual + ")";
            } else if (estadoActual == Estado.q5) {
                resultado[index++] = "8000(" + tokenActual + ")";
            } else {
                resultado[index++] = getToken(tokenActual.toString());
            }
        }



        String[] tokensFinal = new String[index];
        System.arraycopy(resultado, 0, tokensFinal, 0, index);
        return tokensFinal;
    }

    private boolean esCaracterValido(char ch) {
        return !esLetra(ch) && !esNumero(ch) && !esSimbolo(ch) && !Character.isWhitespace(ch);
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

    private boolean esIdentificadorValido(String identificador) {
        Estado estadoActual = Estado.q11;

        for (int i = 0; i < identificador.length(); i++) {
            char ch = identificador.charAt(i);
            switch (estadoActual) {
                case q11 -> {
                    if (esLetra(ch) || ch == '_' || ch == '$') {
                        estadoActual = Estado.q12; // envia al primer carácter válido
                    } else {
                        estadoActual = Estado.qError;
                    }
                }
                case q12 -> { // reconoce el primer carácter
                    if (esLetra(ch) || esNumero(ch) || ch == '_') {
                        estadoActual = Estado.q13; // envia a reconocer caracteres adicionales
                    } else {
                        estadoActual = Estado.qError;
                    }
                }
                case q13 -> { // reconoce caracteres adicionales
                    if (!(esLetra(ch) || esNumero(ch) || ch == '_')) {
                        estadoActual = Estado.qError; // envia al estado de error si hay un carácter no válido
                    }
                }
                case qError -> {
                    return false;
                }
            }
        }
        return estadoActual == Estado.q12 || estadoActual == Estado.q13;
    }

    private boolean esNumeroValido(String numero) {
        Estado estadoActual = Estado.q7;

        for (int i = 0; i < numero.length(); i++) {
            char ch = numero.charAt(i);
            switch (estadoActual) {
                case q7 -> { // Estado inicial
                    if (esNumero(ch)) {
                        estadoActual = Estado.q8; // envia a reconocer dígitos enteros
                    } else {
                        estadoActual = Estado.qError;
                    }
                }
                case q8 -> { // Reconociendo la parte entera
                    if (esNumero(ch)) {
                        continue;
                        // Sigue reconociendo enteros
                    } else if (ch == '.') {
                        estadoActual = Estado.q9; // envia al punto decimal
                    } else {
                        estadoActual = Estado.qError;
                    }
                }
                case q9 -> { // Reconociendo el punto decimal
                    if (esNumero(ch)) {
                        estadoActual = Estado.q10; // envia a reconocer la parte decimal
                    } else {
                        estadoActual = Estado.qError;
                    }
                }
                case q10 -> { // Reconociendo la parte decimal
                    if (esNumero(ch)) {
                        continue;
                        // Sigue reconociendo dígitos decimales
                    } else {
                        estadoActual = Estado.qError;
                    }
                }
                case qError -> {
                    return false; // Si llegamos a qError, el número no es válido
                }
            }
        }

        // Estados finales válidos: q8 (entero) y q10 (flotante)
        return estadoActual == Estado.q8 || estadoActual == Estado.q10;
    }

    private String getToken(String str) {
        return tokens.getOrDefault(str, "ERROR") + "(" + str + ")";
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
            System.err.println("Error al leer el archivo");
        }
    }
}
