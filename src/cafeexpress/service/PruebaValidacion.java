package cafeexpress.service;

import cafeexpress.domain.DetallePedido;
import cafeexpress.domain.Pedido;

import java.time.LocalDateTime;

public class PruebaValidacion {

    public static void main(String[] args) {

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

        try {

            servicio.validarPedido(pedido);

            System.out.println("Pedido valido");

        } catch (Exception e) {

            System.out.println("Error encontrado: " + e.getMessage());

        }
    }
}