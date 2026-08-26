package cafeexpress.service;

public class ItemSolicitado {

    private final int productoId;
    private final int cantidad;

    public ItemSolicitado(int productoId, int cantidad) {
        this.productoId = productoId;
        this.cantidad = cantidad;
    }

    public int getProductoId() {
        return productoId;
    }

    public int getCantidad() {
        return cantidad;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemSolicitado that = (ItemSolicitado) o;
        return productoId == that.productoId && cantidad == that.cantidad;
    }

    @Override
    public int hashCode() {
        int result = productoId;
        result = 31 * result + cantidad;
        return result;
    }

    @Override
    public String toString() {
        return "ItemSolicitado{productoId=" + productoId + ", cantidad=" + cantidad + "}";
    }
}
