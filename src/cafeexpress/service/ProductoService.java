package cafeexpress.service;

import cafeexpress.domain.Producto;
import cafeexpress.repository.ProductoRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class ProductoService {

    private final ProductoRepository repositorio;

    public ProductoService(ProductoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<Producto> listar() {
        return repositorio.listar();
    }

    public List<Producto> listarDisponibles() {
        return repositorio.listar().stream().filter(Producto::isDisponible).toList();
    }

    public Producto buscar(int id) {
        return repositorio.buscarPorId(id)
                .orElseThrow(() -> new NoSuchElementException("El producto " + id + " no existe"));
    }

    public Producto crear(String nombre, String categoria, double precio) {
        validarNombre(nombre);
        validarPrecio(precio);
        return repositorio.guardar(new Producto(0, nombre.trim(), categoria.trim(), precio, true));
    }

    public Producto actualizar(int id, String nombre, String categoria, double precio, boolean disponible) {
        Producto producto = buscar(id);
        validarNombre(nombre);
        validarPrecio(precio);
        producto.setNombre(nombre.trim());
        producto.setCategoria(categoria == null ? "" : categoria.trim());
        producto.setPrecio(precio);
        producto.setDisponible(disponible);
        return repositorio.guardar(producto);
    }

    public boolean eliminar(int id) {
        buscar(id);
        return repositorio.eliminar(id);
    }

    private void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException("El nombre del producto es obligatorio");
        }
    }

    private void validarPrecio(double precio) {
        if (precio <= 0) {
            throw new ReglaNegocioException("El precio debe ser mayor a cero");
        }
    }

    public Optional<Producto> buscarOpcional(int id) {
        return repositorio.buscarPorId(id);
    }
}
