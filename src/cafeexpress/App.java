package cafeexpress;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import cafeexpress.controller.PedidoController;
import cafeexpress.controller.ProductoController;
import cafeexpress.repository.memory.EnMemoriaPedidoRepository;
import cafeexpress.repository.memory.EnMemoriaProductoRepository;
import cafeexpress.service.PedidoService;
import cafeexpress.service.ProductoService;
import cafeexpress.web.Estaticos;
import com.sun.net.httpserver.HttpServer;

public class App {

    public static void main(String[] args) throws Exception {
        int puerto = args.length > 0 ? Integer.parseInt(args[0]) : 8080;

        EnMemoriaProductoRepository productoRepo = new EnMemoriaProductoRepository();
        EnMemoriaPedidoRepository pedidoRepo = new EnMemoriaPedidoRepository();

        ProductoService productoService = new ProductoService(productoRepo);
        PedidoService pedidoService = new PedidoService(pedidoRepo, productoRepo);

        ProductoController productoController = new ProductoController(productoService);
        PedidoController pedidoController = new PedidoController(pedidoService);

        HttpServer servidor = HttpServer.create(new InetSocketAddress(puerto), 0);
        servidor.setExecutor(Executors.newFixedThreadPool(16));
        servidor.createContext("/api/productos", productoController);
        servidor.createContext("/api/pedidos", pedidoController);
        servidor.createContext("/", new Estaticos());
        servidor.start();

        System.out.println("Cafe Express escuchando en http://localhost:" + puerto);
    }
}
