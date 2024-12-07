// Importaciones necesarias para trabajar con lectura de archivos y manejo de excepciones
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// Clase principal que implementa un Analizador Léxico Determinista (AFD)
class AFDToken {
    // Enum para representar los posibles estados del autómata
    private enum Estado {
        q0, q1, q2, q3, q4, qError
    }

    // Diccionario para almacenar palabras reservadas y tokens especiales
    private final Map<String, String> tokens;
    // Arrays que contienen los caracteres válidos para letras, números y símbolos
    private final char[] letras;
    private final char[] numeros;
    private final char[] simbolos;
    private final String[] simbolosCompuestos;

    // Constructor para inicializar los atributos del AFD
    public AFDToken() {
        tokens = new HashMap<>(); // Inicializa el mapa para almacenar los tokens
        inicializarTokens(); // Carga los tokens predefinidos en el mapa
        letras = new char[]{ // Letras válidas para identificadores (incluye '_')
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
                'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
                'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '_'
        };
        numeros = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'}; // Dígitos válidos
        simbolos = new char[]{ // Símbolos simples válidos
                '+', '-', '*', '/', '=', '>', '<', ':', ';', ',', '.', '(', ')', '{', '}', '[',
                ']', '"', '#', '&', '|', '!'
        };
        simbolosCompuestos = new String[]{ // Símbolos compuestos válidos
                "==", "!=", ">=", "<=", "&&", "||", "<<", ">>"
        };
    }

    // Método para inicializar los tokens en el mapa (diccionario)
    private void inicializarTokens() {
        // Palabras reservadas con sus respectivos códigos
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

        // Operadores y símbolos especiales con códigos únicos
        tokens.put("+", "2010");
        tokens.put("-", "2020");
        tokens.put("*", "2030");
        tokens.put("/", "2040");
        tokens.put("%", "2050");
        tokens.put("=", "2060");
        tokens.put(">", "2070");
        tokens.put("<", "2080");
        tokens.put(">=", "2090");
        tokens.put("<=", "2100");
        tokens.put("==", "2110");
        tokens.put("!=", "2120");
        tokens.put("&&", "2130");
        tokens.put("||", "2140");
        tokens.put("!", "2150");
        tokens.put("&", "2160");
        tokens.put("|", "2170");

        // Otros símbolos, como puntuación y operadores bitwise
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
        tokens.put("[", "5030");
        tokens.put("]", "5040");
    }

    // Método principal para analizar una línea de código
    public String[] analizarLinea(String linea) {
        StringBuilder tokenActual = new StringBuilder(); // Almacena el token en construcción
        Estado estadoActual = Estado.q0; // Estado inicial del autómata
        String[] resultado = new String[1024]; // Arreglo para almacenar los tokens generados
        int index = 0; // Índice del siguiente espacio disponible en el arreglo resultado

        for (int i = 0; i < linea.length(); i++) { // Recorre cada carácter de la línea
            char ch = linea.charAt(i); // Carácter actual

            switch (estadoActual) {
                case q0 -> { // Estado inicial
                    if (esLetra(ch)) { // Si es letra, puede ser identificador o palabra reservada
                        tokenActual.append(ch);
                        estadoActual = Estado.q1; // Cambia al estado de identificador
                    } else if (esNumero(ch)) { // Si es número, inicia un token numérico
                        tokenActual.append(ch);
                        estadoActual = Estado.q2; // Cambia al estado de número
                    } else if (ch == '"') { // Si es una comilla, inicia una cadena
                        tokenActual.append(ch);
                        estadoActual = Estado.q4; // Cambia al estado de cadena
                    } else if (esSimbolo(ch)) { // Si es un símbolo, procesa el símbolo
                        tokenActual.append(ch);
                        estadoActual = Estado.q3; // Cambia al estado de símbolo
                    } else if (Character.isWhitespace(ch)) {
                        // Ignora espacios en blanco
                    } else { // Carácter desconocido o inválido
                        tokenActual.append(ch);
                        estadoActual = Estado.qError; // Cambia al estado de error
                    }
                }
                case q1 -> { // Estado de identificador o palabra reservada
                    if (esLetra(ch) || esNumero(ch)) {
                        tokenActual.append(ch); // Sigue construyendo el token
                    } else { // Token finalizado
                        resultado[index++] = getToken(tokenActual.toString()); // Emite el token
                        tokenActual.setLength(0); // Limpia el buffer del token actual
                        estadoActual = Estado.q0; // Regresa al estado inicial
                        i--; // Reanaliza el carácter actual
                    }
                }
                case q2 -> { // Estado de número
                    if (esNumero(ch)) {
                        tokenActual.append(ch); // Sigue construyendo el token numérico
                    } else { // Token numérico finalizado
                        resultado[index++] = "7010(" + tokenActual + ")"; // Genera token numérico
                        tokenActual.setLength(0); // Limpia el buffer del token actual
                        estadoActual = Estado.q0; // Regresa al estado inicial
                        i--; // Reanaliza el carácter actual
                    }
                }
                case q3 -> { // Estado de símbolo
                    String simboloCompuesto = tokenActual.toString() + ch; // Construye símbolo compuesto
                    if (esSimboloCompuesto(simboloCompuesto)) { // Si forma un símbolo compuesto
                        tokenActual.append(ch);
                        resultado[index++] = getToken(tokenActual.toString()); // Genera el token
                        tokenActual.setLength(0); // Limpia el buffer del token actual
                        estadoActual = Estado.q0; // Regresa al estado inicial
                    } else { // Es un símbolo simple
                        resultado[index++] = getToken(tokenActual.toString()); // Genera el token
                        tokenActual.setLength(0); // Limpia el buffer del token actual
                        estadoActual = Estado.q0; // Regresa al estado inicial
                        i--; // Reanaliza el carácter actual
                    }
                }
                case q4 -> { // Estado de cadena
                    tokenActual.append(ch);
                    if (ch == '"') { // Cierra la cadena
                        resultado[index++] = "3080(" + tokenActual + ")"; // Genera token de cadena
                        tokenActual.setLength(0); // Limpia el buffer del token actual
                        estadoActual = Estado.q0; // Regresa al estado inicial
                    }
                }
                case qError -> { // Estado de error
                    resultado[index++] = "ERROR(" + tokenActual + ")"; // Genera un token de error
                    tokenActual.setLength(0); // Limpia el buffer del token actual
                    estadoActual = Estado.q0; // Regresa al estado inicial
                }
            }
        }

        if (!tokenActual.isEmpty()) { // Si hay un token pendiente al final de la línea
            resultado[index++] = getToken(tokenActual.toString()); // Emite el token
        }

        // Copia los tokens válidos a un nuevo arreglo del tamaño adecuado
        String[] tokensFinal = new String[index];
        System.arraycopy(resultado, 0, tokensFinal, 0, index); // Copia desde resultado al nuevo arreglo
        return tokensFinal; // Retorna el arreglo final de tokens
    }

    // Métodos auxiliares para verificar tipos de caracteres
    private boolean esLetra(char ch) {
        for (char letra : letras) {
            if (letra == ch) {
                return true;
            }
        }
        return false;
    }

    private boolean esNumero(char ch) {
        for (char numero : numeros) {
            if (numero == ch) {
                return true;
            }
        }
        return false;
    }

    private boolean esSimbolo(char ch) {
        for (char simbolo : simbolos) {
            if (simbolo == ch) {
                return true;
            }
        }
        return false;
    }

    private boolean esSimboloCompuesto(String str) {
        for (String simbolo : simbolosCompuestos) {
            if (simbolo.equals(str)) {
                return true;
            }
        }
        return false;
    }

    // Método para obtener el token asociado a una cadena
    private String getToken(String str) {
        return tokens.getOrDefault(str, "6000") + "(" + str + ")";
    }
}

// Clase principal que ejecuta el programa
public class Main {
    public static void main(String[] args) {
        AFDToken afd = new AFDToken(); // Crea una instancia del analizador léxico

        try (BufferedReader lector = new BufferedReader(new FileReader("src/cpp/programa_cpp.txt"))) {
            // Abre el archivo fuente para leer línea por línea
            String linea;
            while ((linea = lector.readLine()) != null) { // Mientras haya líneas por leer
                String[] tokens = afd.analizarLinea(linea); // Analiza la línea para obtener tokens
                for (String token : tokens) { // Imprime cada token generado
                    System.out.print(token + " ");
                }
                System.out.println(); // Salto de línea después de procesar cada línea
            }
        } catch (IOException e) { // Manejo de excepciones si el archivo no se encuentra o no se puede leer
            e.printStackTrace(); // Imprime el stacktrace del error
            System.err.println("Archivo no encontrado en la ruta especificada o el archivo está dañado");
        }
    }
}