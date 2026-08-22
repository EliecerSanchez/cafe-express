package cafeexpress.domain;

public class DetallePedido {

    private final int productoId;
    private final String nombreProducto;
    private final double precioUnitario;
    private final int cantidad;

    public DetallePedido(int productoId, String nombreProducto, double precioUnitario, int cantidad) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
    }

    public int getProductoId() {
        return productoId;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getSubtotal() {
        return Math.round(precioUnitario * cantidad * 100.0) / 100.0;
    }
}
