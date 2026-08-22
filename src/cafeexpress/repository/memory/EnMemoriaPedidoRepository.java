package cafeexpress.repository.memory;

import cafeexpress.domain.Pedido;
import cafeexpress.repository.PedidoRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class EnMemoriaPedidoRepository implements PedidoRepository {

    private final Map<Integer, Pedido> datos = new ConcurrentHashMap<>();
    private final AtomicInteger secuencia = new AtomicInteger(100);

    @Override
    public Pedido guardar(Pedido pedido) {
        if (pedido.getId() <= 0) {
            pedido.setId(secuencia.incrementAndGet());
        }
        datos.put(pedido.getId(), pedido);
        return pedido;
    }

    @Override
    public Optional<Pedido> buscarPorId(int id) {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public List<Pedido> listar() {
        List<Pedido> lista = new ArrayList<>(datos.values());
        lista.sort(Comparator.comparing(Pedido::getId).reversed());
        return lista;
    }
}
