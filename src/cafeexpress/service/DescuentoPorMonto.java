package cafeexpress.service;

public class DescuentoPorMonto implements PoliticaDescuento {

    public static final double MONTO_MINIMO = 200000;
    public static final double PORCENTAJE = 0.10;

    @Override
    public double calcular(double subtotal) {
        if (subtotal >= MONTO_MINIMO) {
            return Math.round(subtotal * PORCENTAJE * 100.0) / 100.0;
        }
        return 0;
    }
}
