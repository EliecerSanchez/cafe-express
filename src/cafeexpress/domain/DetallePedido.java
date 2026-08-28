package cafeexpress.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PedidoTest {

    @Test
    void debeCalcularSubtotalDelPedido() {

        Pedido pedido = new Pedido(
                1,
                "Juan Perez",
                LocalDateTime.now()
        );

        DetallePedido detalle = new DetallePedido(
                1,
                "Cafe Americano",
                5000,
                2
        );

        pedido.agregarDetalle(detalle);

        assertEquals(10000, pedido.getSubtotal());
    }

    @Test
    void debeAplicarDescuentoAlPedido() {

        Pedido pedido = new Pedido(
                2,
                "Maria Lopez",
                LocalDateTime.now()
        );

        DetallePedido detalle = new DetallePedido(
                2,
                "Capuchino",
                8000,
                2
        );

        pedido.agregarDetalle(detalle);

        pedido.aplicarDescuento(2000);

        assertEquals(14000, pedido.getTotal());
    }
}