package cafeexpress.web;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SimpleJson {

    private SimpleJson() {
    }

    public static String de(Object valor) {
        StringBuilder sb = new StringBuilder();
        escribir(valor, sb);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parse(String texto) {
        return (Map<String, Object>) new Analizador(texto).valor();
    }

    private static void escribir(Object valor, StringBuilder sb) {
        if (valor == null) {
            sb.append("null");
        } else if (valor instanceof String s) {
            escribirTexto(s, sb);
        } else if (valor instanceof Boolean b) {
            sb.append(b);
        } else if (valor instanceof Double d) {
            if (d.isNaN() || d.isInfinite()) {
                sb.append("null");
            } else if (d == Math.floor(d)) {
                sb.append((long) d.doubleValue());
            } else {
                sb.append(d);
            }
        } else if (valor instanceof Number n) {
            sb.append(n);
        } else if (valor instanceof Enum<?> e) {
            escribirTexto(e.name(), sb);
        } else if (valor instanceof Map<?, ?> mapa) {
            escribirMapa(mapa, sb);
        } else if (valor instanceof List<?> lista) {
            escribirLista(lista, sb);
        } else {
            escribirTexto(valor.toString(), sb);
        }
    }

    private static void escribirMapa(Map<?, ?> mapa, StringBuilder sb) {
        sb.append('{');
        boolean primero = true;
        for (Map.Entry<?, ?> entrada : mapa.entrySet()) {
            if (!primero) sb.append(',');
            primero = false;
            escribirTexto(String.valueOf(entrada.getKey()), sb);
            sb.append(':');
            escribir(entrada.getValue(), sb);
        }
        sb.append('}');
    }

    private static void escribirLista(List<?> lista, StringBuilder sb) {
        sb.append('[');
        boolean primero = true;
        for (Object elemento : lista) {
            if (!primero) sb.append(',');
            primero = false;
            escribir(elemento, sb);
        }
        sb.append(']');
    }

    private static void escribirTexto(String texto, StringBuilder sb) {
        sb.append('"');
        for (char c : texto.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
    }

    private static final class Analizador {

        private final String texto;
        private int posicion;

        Analizador(String texto) {
            this.texto = texto == null ? "" : texto.trim();
            this.posicion = 0;
        }

        Object valor() {
            saltarEspacios();
            Object resultado = valorInterno();
            saltarEspacios();
            if (posicion != texto.length()) {
                throw new IllegalArgumentException("JSON invalido: contenido sobrante al final");
            }
            return resultado;
        }

        private Object valorInterno() {
            char c = actual();
            return switch (c) {
                case '{' -> objeto();
                case '[' -> arreglo();
                case '"' -> cadena();
                default -> literal();
            };
        }

        private Map<String, Object> objeto() {
            Map<String, Object> mapa = new LinkedHashMap<>();
            posicion++;
            saltarEspacios();
            if (actual() == '}') {
                posicion++;
                return mapa;
            }
            while (true) {
                saltarEspacios();
                String clave = cadena();
                saltarEspacios();
                esperar(':');
                posicion++;
                saltarEspacios();
                mapa.put(clave, valorInterno());
                saltarEspacios();
                char c = actual();
                if (c == ',') {
                    posicion++;
                } else if (c == '}') {
                    posicion++;
                    return mapa;
                } else {
                    throw new IllegalArgumentException("JSON invalido: se esperaba ',' o '}'");
                }
            }
        }

        private List<Object> arreglo() {
            List<Object> lista = new ArrayList<>();
            posicion++;
            saltarEspacios();
            if (actual() == ']') {
                posicion++;
                return lista;
            }
            while (true) {
                lista.add(valorInterno());
                saltarEspacios();
                char c = actual();
                if (c == ',') {
                    posicion++;
                } else if (c == ']') {
                    posicion++;
                    return lista;
                } else {
                    throw new IllegalArgumentException("JSON invalido: se esperaba ',' o ']'");
                }
            }
        }

        private String cadena() {
            esperar('"');
            posicion++;
            StringBuilder sb = new StringBuilder();
            while (true) {
                char c = texto.charAt(posicion);
                if (c == '"') {
                    posicion++;
                    return sb.toString();
                }
                if (c == '\\') {
                    posicion++;
                    char escape = texto.charAt(posicion);
                    switch (escape) {
                        case '"' -> sb.append('"');
                        case '\\' -> sb.append('\\');
                        case '/' -> sb.append('/');
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        case 'b' -> sb.append('\b');
                        case 'f' -> sb.append('\f');
                        case 'u' -> {
                            sb.append((char) Integer.parseInt(texto.substring(posicion + 1, posicion + 5), 16));
                            posicion += 4;
                        }
                        default -> throw new IllegalArgumentException("Escape JSON invalido");
                    }
                    posicion++;
                } else {
                    sb.append(c);
                    posicion++;
                }
            }
        }

        private Object literal() {
            int inicio = posicion;
            while (posicion < texto.length() && ",]} \t\r\n".indexOf(texto.charAt(posicion)) < 0) {
                posicion++;
            }
            String token = texto.substring(inicio, posicion);
            switch (token) {
                case "true": return Boolean.TRUE;
                case "false": return Boolean.FALSE;
                case "null": return null;
            }
            try {
                if (token.indexOf('.') >= 0 || token.indexOf('e') >= 0 || token.indexOf('E') >= 0) {
                    return Double.parseDouble(token);
                }
                long entero = Long.parseLong(token);
                if (entero >= Integer.MIN_VALUE && entero <= Integer.MAX_VALUE) {
                    return (int) entero;
                }
                return entero;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("JSON invalido: token inesperado '" + token + "'");
            }
        }

        private void saltarEspacios() {
            while (posicion < texto.length() && Character.isWhitespace(texto.charAt(posicion))) {
                posicion++;
            }
        }

        private char actual() {
            if (posicion >= texto.length()) {
                throw new IllegalArgumentException("JSON incompleto");
            }
            return texto.charAt(posicion);
        }

        private void esperar(char c) {
            if (actual() != c) {
                throw new IllegalArgumentException("JSON invalido: se esperaba '" + c + "'");
            }
        }
    }
}
