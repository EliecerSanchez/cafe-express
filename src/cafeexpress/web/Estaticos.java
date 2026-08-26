package cafeexpress.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Estaticos implements HttpHandler {

    private final Path raiz = Paths.get("static").toAbsolutePath().normalize();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String ruta = exchange.getRequestURI().getPath();
        if (ruta.equals("/") || ruta.trim().isEmpty()) ruta = "/index.html";
        Path archivo = raiz.resolve(ruta.substring(1)).normalize();

        if (!archivo.startsWith(raiz) || !Files.isRegularFile(archivo)) {
            noEncontrado(exchange);
            return;
        }

        byte[] bytes = Files.readAllBytes(archivo);
        exchange.getResponseHeaders().set("Content-Type", tipoDeContenido(archivo.getFileName().toString()));
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream salida = exchange.getResponseBody()) {
            salida.write(bytes);
        }
    }

    private void noEncontrado(HttpExchange exchange) throws IOException {
        byte[] bytes = "Recurso no encontrado".getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(404, bytes.length);
        try (OutputStream salida = exchange.getResponseBody()) {
            salida.write(bytes);
        }
    }

    private String tipoDeContenido(String nombre) {
        if (nombre.endsWith(".css")) return "text/css; charset=utf-8";
        if (nombre.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (nombre.endsWith(".svg")) return "image/svg+xml";
        if (nombre.endsWith(".png")) return "image/png";
        return "text/html; charset=utf-8";
    }
}
