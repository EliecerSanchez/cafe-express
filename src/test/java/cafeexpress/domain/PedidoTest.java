package cafeexpress.domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PedidoTest {

    @Test
    void debeCalcularTotalPedido() {

        Pedido pedido = new Pedido(
                1,
                "Juan Perez",
                LocalDateTime.now()
        );

        Producto producto = new Producto(
                1,
                "Cafe Americano",
                "Bebidas",
                5000,
                true
        );

        DetallePedido detalle = new DetallePedido(
                producto,
                2
        );

        pedido.agregarDetalle(detalle);

        assertEquals(10000, pedido.getTotal());
    }
}