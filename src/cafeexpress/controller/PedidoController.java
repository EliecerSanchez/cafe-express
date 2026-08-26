package cafeexpress.controller;

import cafeexpress.domain.DetallePedido;
import cafeexpress.domain.EstadoPedido;
import cafeexpress.domain.Pedido;
import cafeexpress.service.ItemSolicitado;
import cafeexpress.service.PedidoService;
import cafeexpress.service.ReglaNegocioException;
import cafeexpress.web.Http;
import cafeexpress.web.SimpleJson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class PedidoController implements HttpHandler {

    private static final String PREFIJO = "/api/pedidos";

    private final PedidoService servicio;

    public PedidoController(PedidoService servicio) {
        this.servicio = servicio;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String metodo = exchange.getRequestMethod();
            String ruta = exchange.getRequestURI().getPath();

            switch (metodo) {
                case "GET":
                    consultar(exchange, ruta);
                    break;
                case "POST":
                    crear(exchange);
                    break;
                case "PUT":
                    actualizarEstado(exchange, ruta);
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

    @SuppressWarnings("unchecked")
    private void consultar(HttpExchange exchange, String ruta) throws IOException {
        if (ruta.length() > PREFIJO.length()) {
            int id = Http.idDeLaRuta(ruta, PREFIJO + "/");
            Http.ok(exchange, mapa(servicio.obtener(id)));
            return;
        }
        if (!Http.esAdmin(exchange)) {
            Http.error(exchange, 401, "Se requiere token de administrador");
            return;
        }
        List<Map<String, Object>> datos = new ArrayList<>();
        for (Pedido pedido : servicio.listar()) {
            datos.add(mapa(pedido));
        }
        Http.ok(exchange, datos);
    }

    @SuppressWarnings("unchecked")
    private void crear(HttpExchange exchange) throws IOException {
        Map<String, Object> cuerpo = SimpleJson.parse(Http.leerCuerpo(exchange));
        List<ItemSolicitado> items = new ArrayList<>();
        Object crudo = cuerpo.get("items");
        if (crudo instanceof List) {
            List<?> lista = (List<?>) crudo;
            for (Object elemento : lista) {
                Map<String, Object> item = (Map<String, Object>) elemento;
                items.add(new ItemSolicitado(
                        (int) doble(item.get("productoId")),
                        (int) doble(item.get("cantidad"))));
            }
        }
        Pedido pedido = servicio.crear(texto(cuerpo.get("cliente")), texto(cuerpo.get("formaPago")), items);
        Http.creado(exchange, mapa(pedido));
    }

    private void actualizarEstado(HttpExchange exchange, String ruta) throws IOException {
        if (!Http.esAdmin(exchange)) {
            Http.error(exchange, 401, "Se requiere token de administrador");
            return;
        }
        String sinPrefijo = ruta.substring(PREFIJO.length() + 1);
        String[] partes = sinPrefijo.split("/");
        int id = Integer.parseInt(partes[0]);
        Map<String, Object> cuerpo = SimpleJson.parse(Http.leerCuerpo(exchange));
        EstadoPedido nuevoEstado = EstadoPedido.valueOf(String.valueOf(cuerpo.get("estado")).trim().toUpperCase());
        Http.ok(exchange, mapa(servicio.cambiarEstado(id, nuevoEstado)));
    }

    private Map<String, Object> mapa(Pedido pedido) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", pedido.getId());
        m.put("cliente", pedido.getCliente());
        m.put("fecha", pedido.getFecha().toString());
        m.put("estado", pedido.getEstado().name());
        m.put("formaPago", pedido.getFormaPago().name());
        m.put("subtotal", pedido.getSubtotal());
        m.put("descuento", pedido.getDescuento());
        m.put("total", pedido.getTotal());

        List<Map<String, Object>> detalles = new ArrayList<>();
        for (DetallePedido detalle : pedido.getDetalles()) {
            Map<String, Object> d = new LinkedHashMap<>();
            d.put("productoId", detalle.getProductoId());
            d.put("nombreProducto", detalle.getNombreProducto());
            d.put("precioUnitario", detalle.getPrecioUnitario());
            d.put("cantidad", detalle.getCantidad());
            d.put("subtotal", detalle.getSubtotal());
            detalles.add(d);
        }
        m.put("detalles", detalles);
        return m;
    }

    private double doble(Object valor) {
        if (valor instanceof Number) {
            return ((Number) valor).doubleValue();
        }
        return Double.parseDouble(String.valueOf(valor));
    }

    private String texto(Object valor) {
        return valor == null ? "" : String.valueOf(valor);
    }
}
