package cafeexpress.web;

import com.sun.net.httpserver.HttpExchange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public final class Http {

    public static final String TOKEN_ADMIN = "admin123";

    private Http() {
    }

    public static void json(HttpExchange exchange, int codigo, String cuerpo) throws IOException {
        byte[] bytes = cuerpo.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(codigo, bytes.length);
        try (OutputStream salida = exchange.getResponseBody()) {
            salida.write(bytes);
        }
    }

    public static void ok(HttpExchange exchange, Object datos) throws IOException {
        json(exchange, 200, SimpleJson.de(datos));
    }

    public static void error(HttpExchange exchange, int codigo, String mensaje) throws IOException {
        json(exchange, codigo, SimpleJson.de(Collections.singletonMap("error", mensaje)));
    }

    public static void creado(HttpExchange exchange, Object datos) throws IOException {
        json(exchange, 201, SimpleJson.de(datos));
    }

    public static String leerCuerpo(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int nRead;
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toString("UTF-8");
    }

    public static boolean esAdmin(HttpExchange exchange) {
        return TOKEN_ADMIN.equals(exchange.getRequestHeaders().getFirst("X-Admin-Token"));
    }

    public static int idDeLaRuta(String ruta, String prefijo) {
        try {
            return Integer.parseInt(ruta.substring(prefijo.length()).replace("/", ""));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Identificador invalido");
        }
    }
}
