package cafeexpress.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Pedido {

    private int id;
    private final String cliente;
    private final LocalDateTime fecha;
    private EstadoPedido estado;
    private FormaPago formaPago;
    private final List<DetallePedido> detalles = new ArrayList<>();
    private double subtotal;
    private double descuento;
    private double total;

    public Pedido(int id, String cliente, LocalDateTime fecha) {
        this.id = id;
        this.cliente = cliente;
        this.fecha = fecha;
        this.estado = EstadoPedido.RECIBIDO;
        this.formaPago = FormaPago.EFECTIVO;
    }

    public void agregarDetalle(DetallePedido detalle) {
        detalles.add(detalle);
        subtotal = Math.round((subtotal + detalle.getSubtotal()) * 100.0) / 100.0;
    }

    public void aplicarDescuento(double valorDescuento) {
        this.descuento = Math.round(valorDescuento * 100.0) / 100.0;
        this.total = Math.round((subtotal - descuento) * 100.0) / 100.0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCliente() {
        return cliente;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public FormaPago getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(FormaPago formaPago) {
        this.formaPago = formaPago;
    }

    public List<DetallePedido> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getDescuento() {
        return descuento;
    }

    public double getTotal() {
        return total == 0 ? subtotal : total;
    }
}
