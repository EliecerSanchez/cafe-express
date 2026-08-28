package cafeexpress.service;

import cafeexpress.domain.DetallePedido;
import cafeexpress.domain.Pedido;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidacionPedidoServiceTest {


    @Test
    void debeRechazarPedidoSinCliente() {

        Pedido pedido = new Pedido(
                1,
                "",
                LocalDateTime.now()
        );

        pedido.agregarDetalle(
                new DetallePedido(
                        1,
                        "Cafe",
                        5000,
                        1
                )
        );


        ValidacionPedidoService servicio =
                new ValidacionPedidoService();


        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.validarPedido(pedido)
        );
    }
}