package cafeexpress.repository;

import cafeexpress.domain.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository {

    Producto guardar(Producto producto);

    Optional<Producto> buscarPorId(int id);

    List<Producto> listar();

    boolean eliminar(int id);
}
