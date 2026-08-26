package cafeexpress.controller;

import cafeexpress.domain.Producto;
import cafeexpress.service.ProductoService;
import cafeexpress.service.ReglaNegocioException;
import cafeexpress.web.Http;
import cafeexpress.web.SimpleJson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class ProductoController implements HttpHandler {

    private static final String PREFIJO = "/api/productos";

    private final ProductoService servicio;

    public ProductoController(ProductoService servicio) {
        this.servicio = servicio;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String metodo = exchange.getRequestMethod();
            String ruta = exchange.getRequestURI().getPath();
            boolean conId = ruta.length() > PREFIJO.length();

            switch (metodo) {
                case "GET":
                    obtener(exchange, conId ? Http.idDeLaRuta(ruta, PREFIJO + "/") : null);
                    break;
                case "POST":
                    crear(exchange);
                    break;
                case "PUT":
                    actualizar(exchange, Http.idDeLaRuta(ruta, PREFIJO + "/"));
                    break;
                case "DELETE":
                    eliminar(exchange, Http.idDeLaRuta(ruta, PREFIJO + "/"));
                    break;
                default:
                    Http.error(exchange, 405, "Metodo no permitido");
                    break;
            }
        } catch (IllegalArgumentException e) {
            Http.error(exchange, 400, e.getMessage());
        } catch (ReglaNegocioException e) {
            Http.error(exchange, 422, e.getMessage());
        } catch (NoSuchElementException e) {
            Http.error(exchange, 404, e.getMessage());
        }
    }

    private void obtener(HttpExchange exchange, Integer id) throws IOException {
        if (id == null) {
            String query = exchange.getRequestURI().getQuery();
            boolean soloDisponibles = query != null && query.contains("disponibles=true");
            List<Producto> productos = soloDisponibles ? servicio.listarDisponibles() : servicio.listar();
            List<Map<String, Object>> datos = new ArrayList<>();
            for (Producto producto : productos) {
                datos.add(mapa(producto));
            }
            Http.ok(exchange, datos);
        } else {
            Http.ok(exchange, mapa(servicio.buscar(id)));
        }
    }

    private void crear(HttpExchange exchange) throws IOException {
        if (!Http.esAdmin(exchange)) {
            Http.error(exchange, 401, "Se requiere token de administrador");
            return;
        }
        Map<String, Object> cuerpo = SimpleJson.parse(Http.leerCuerpo(exchange));
        Producto producto = servicio.crear(texto(cuerpo.get("nombre")), texto(cuerpo.get("categoria")),
                numero(cuerpo.get("precio")));
        Http.creado(exchange, mapa(producto));
    }

    private void actualizar(HttpExchange exchange, int id) throws IOException {
        if (!Http.esAdmin(exchange)) {
            Http.error(exchange, 401, "Se requiere token de administrador");
            return;
        }
        Map<String, Object> cuerpo = SimpleJson.parse(Http.leerCuerpo(exchange));
        boolean disponible = cuerpo.get("disponible") == null || Boolean.parseBoolean(String.valueOf(cuerpo.get("disponible")));
        Producto producto = servicio.actualizar(id, texto(cuerpo.get("nombre")), texto(cuerpo.get("categoria")),
                numero(cuerpo.get("precio")), disponible);
        Http.ok(exchange, mapa(producto));
    }

    private void eliminar(HttpExchange exchange, int id) throws IOException {
        if (!Http.esAdmin(exchange)) {
            Http.error(exchange, 401, "Se requiere token de administrador");
            return;
        }
        if (servicio.eliminar(id)) {
            Http.ok(exchange, Collections.singletonMap("eliminado", id));
        } else {
            Http.error(exchange, 404, "El producto " + id + " no existe");
        }
    }

    private Map<String, Object> mapa(Producto producto) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", producto.getId());
        m.put("nombre", producto.getNombre());
        m.put("categoria", producto.getCategoria());
        m.put("precio", producto.getPrecio());
        m.put("disponible", producto.isDisponible());
        return m;
    }

    private String texto(Object valor) {
        return valor == null ? "" : String.valueOf(valor);
    }

    private double numero(Object valor) {
        if (valor instanceof Number) {
            return ((Number) valor).doubleValue();
        }
        return Double.parseDouble(String.valueOf(valor));
    }
}
