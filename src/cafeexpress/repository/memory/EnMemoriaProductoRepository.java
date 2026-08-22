package cafeexpress.repository.memory;

import cafeexpress.domain.Producto;
import cafeexpress.repository.ProductoRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class EnMemoriaProductoRepository implements ProductoRepository {

    private final Map<Integer, Producto> datos = new ConcurrentHashMap<>();
    private final AtomicInteger secuencia = new AtomicInteger(0);

    public EnMemoriaProductoRepository() {
        sembrarCatalogo();
    }

    @Override
    public Producto guardar(Producto producto) {
        if (producto.getId() <= 0) {
            producto.setId(secuencia.incrementAndGet());
        }
        datos.put(producto.getId(), producto);
        return producto;
    }

    @Override
    public Optional<Producto> buscarPorId(int id) {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public List<Producto> listar() {
        List<Producto> lista = new ArrayList<>(datos.values());
        lista.sort(Comparator.comparing(Producto::getId));
        return lista;
    }

    @Override
    public boolean eliminar(int id) {
        return datos.remove(id) != null;
    }

    private void sembrarCatalogo() {
        guardar(new Producto(0, "Cafe Americano", "Bebidas calientes", 4500, true));
        guardar(new Producto(0, "Capuchino", "Bebidas calientes", 7500, true));
        guardar(new Producto(0, "Latte Vainilla", "Bebidas calientes", 8500, true));
        guardar(new Producto(0, "Espresso Doble", "Bebidas calientes", 6000, true));
        guardar(new Producto(0, "Moka Frío", "Bebidas frias", 9500, true));
        guardar(new Producto(0, "Malteada de Arequipe", "Bebidas frias", 11000, true));
        guardar(new Producto(0, "Croissant de Queso", "Pasteleria", 6500, true));
        guardar(new Producto(0, "Torta de Zanahoria", "Pasteleria", 7000, true));
        guardar(new Producto(0, "Sandwich de Pavo", "Comidas", 12500, true));
        guardar(new Producto(0, "Combo Desayuno", "Combos", 18000, true));
        guardar(new Producto(0, "Combo Almuerzo Express", "Combos", 22000, true));
    }
}
