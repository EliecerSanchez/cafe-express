package cafeexpress.service;

import cafeexpress.domain.DetallePedido;
import cafeexpress.domain.EstadoPedido;
import cafeexpress.domain.FormaPago;
import cafeexpress.domain.Pedido;
import cafeexpress.domain.Producto;
import cafeexpress.repository.PedidoRepository;
import cafeexpress.repository.ProductoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class PedidoService {

    public static final double MONTO_MINIMO_TARJETA = 5000;

    private static final Map<EstadoPedido, Set<EstadoPedido>> TRANSICIONES = Map.of(
            EstadoPedido.RECIBIDO, Set.of(EstadoPedido.EN_PREPARACION, EstadoPedido.CANCELADO),
            EstadoPedido.EN_PREPARACION, Set.of(EstadoPedido.LISTO, EstadoPedido.CANCELADO),
            EstadoPedido.LISTO, Set.of(EstadoPedido.ENTREGADO),
            EstadoPedido.ENTREGADO, Set.of(),
            EstadoPedido.CANCELADO, Set.of()
    );

    private final PedidoRepository repositorioPedidos;
    private final ProductoRepository repositorioProductos;
    private final PoliticaDescuento politicaDescuento;

    public PedidoService(PedidoRepository repositorioPedidos, ProductoRepository repositorioProductos) {
        this(repositorioPedidos, repositorioProductos, new DescuentoPorMonto());
    }

    public PedidoService(PedidoRepository repositorioPedidos, ProductoRepository repositorioProductos,
                         PoliticaDescuento politicaDescuento) {
        this.repositorioPedidos = repositorioPedidos;
        this.repositorioProductos = repositorioProductos;
        this.politicaDescuento = politicaDescuento;
    }

    public Pedido crear(String cliente, String formaPagoTexto, List<ItemSolicitado> items) {
        if (cliente == null || cliente.isBlank()) {
            throw new ReglaNegocioException("El nombre del cliente es obligatorio");
        }
        if (items == null || items.isEmpty()) {
            throw new ReglaNegocioException("El pedido debe incluir al menos un producto");
        }
        FormaPago formaPago = leerFormaPago(formaPagoTexto);

        Pedido pedido = new Pedido(0, cliente.trim(), LocalDateTime.now());
        pedido.setFormaPago(formaPago);

        for (ItemSolicitado item : items) {
            if (item.cantidad() <= 0) {
                throw new ReglaNegocioException("La cantidad del producto " + item.productoId() + " debe ser mayor a cero");
            }
            Producto producto = repositorioProductos.buscarPorId(item.productoId())
                    .orElseThrow(() -> new ReglaNegocioException("El producto " + item.productoId() + " no existe"));
            if (!producto.isDisponible()) {
                throw new ReglaNegocioException("El producto '" + producto.getNombre() + "' no esta disponible");
            }
            pedido.agregarDetalle(new DetallePedido(producto.getId(), producto.getNombre(),
                    producto.getPrecio(), item.cantidad()));
        }

        if (pedido.getTotal() < MONTO_MINIMO_TARJETA && formaPago == FormaPago.TARJETA) {
            throw new ReglaNegocioException("Los pagos con tarjeta solo se aceptan por montos iguales o superiores a $"
                    + MONTO_MINIMO_TARJETA);
        }

        pedido.aplicarDescuento(politicaDescuento.calcular(pedido.getSubtotal()));
        return repositorioPedidos.guardar(pedido);
    }

    public List<Pedido> listar() {
        return repositorioPedidos.listar();
    }

    public Pedido obtener(int id) {
        return repositorioPedidos.buscarPorId(id)
                .orElseThrow(() -> new NoSuchElementException("El pedido " + id + " no existe"));
    }

    public Pedido cambiarEstado(int id, EstadoPedido nuevoEstado) {
        Pedido pedido = obtener(id);
        Set<EstadoPedido> permitidos = TRANSICIONES.get(pedido.getEstado());
        if (!permitidos.contains(nuevoEstado)) {
            throw new ReglaNegocioException("Transicion invalida: no se puede pasar de "
                    + pedido.getEstado() + " a " + nuevoEstado);
        }
        pedido.setEstado(nuevoEstado);
        return repositorioPedidos.guardar(pedido);
    }

    private FormaPago leerFormaPago(String texto) {
        if (texto == null || texto.isBlank()) {
            return FormaPago.EFECTIVO;
        }
        try {
            return FormaPago.valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ReglaNegocioException("Forma de pago no soportada: " + texto);
        }
    }
}
