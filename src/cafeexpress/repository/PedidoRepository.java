package cafeexpress.repository;

import cafeexpress.domain.Pedido;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository {

    Pedido guardar(Pedido pedido);

    Optional<Pedido> buscarPorId(int id);

    List<Pedido> listar();
}
