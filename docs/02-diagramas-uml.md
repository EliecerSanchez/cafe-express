# 2. Diseño de la arquitectura mediante UML

> Los diagramas están en sintaxis **Mermaid**: se renderizan automáticamente en GitHub, VS Code
> (extensión Mermaid) o https://mermaid.live. Cada diagrama incluye su descripción y justificación.

## 2.1 Diagrama de casos de uso

```mermaid
flowchart LR
    cliente([Actor: Cliente])
    admin([Actor: Administrador])

    subgraph sistema["Sistema Café Express"]
        uc1(["Consultar productos"])
        uc2(["Realizar pedido"])
        uc3(["Consultar estado del pedido"])
        uc4(["Consultar pedidos"])
        uc5(["Actualizar estado del pedido"])
        uc6(["Gestionar productos"])
        uc7(["Validar reglas de negocio «include»"])
    end

    cliente --> uc1
    cliente --> uc2
    cliente --> uc3
    admin --> uc4
    admin --> uc5
    admin --> uc6
    uc2 -. include .-> uc7
    uc6 -. include .-> uc7
```

**Descripción:** el *Cliente* consulta el catálogo, realiza pedidos y consulta su estado; el
*Administrador* consulta y actualiza pedidos y gestiona el catálogo de productos.

**Relevancia arquitectónica:** define la frontera del sistema y los actores antes de diseñar.
De él se derivan directamente los módulos de la capa de presentación (una pantalla por caso de uso)
y los endpoints REST, garantizando trazabilidad entre requisitos y componentes.

## 2.2 Diagrama de clases

```mermaid
classDiagram
    class Cliente {
        +String nombre
    }
    class Producto {
        +int id
        +String nombre
        +String categoria
        +double precio
        +boolean disponible
    }
    class Pedido {
        +int id
        +String cliente
        +LocalDateTime fecha
        +EstadoPedido estado
        +FormaPago formaPago
        +agregarDetalle(d)
        +aplicarDescuento(v)
        +getSubtotal() double
        +getTotal() double
    }
    class DetallePedido {
        +int productoId
        +String nombreProducto
        +double precioUnitario
        +int cantidad
        +getSubtotal() double
    }
    class EstadoPedido {
        <<enumeration>>
        RECIBIDO
        EN_PREPARACION
        LISTO
        ENTREGADO
        CANCELADO
    }
    class FormaPago {
        <<enumeration>>
        EFECTIVO
        TARJETA
    }
    class PedidoService {
        -PoliticaDescuento politicaDescuento
        +crear() Pedido
        +cambiarEstado() Pedido
        +obtener() Pedido
    }
    class ProductoService {
        +listarDisponibles() List
        +crear() Producto
    }
    class PoliticaDescuento {
        <<interface>>
        +calcular(subtotal) double
    }
    class DescuentoPorMonto {
        +MONTO_MINIMO double
        +PORCENTAJE double
    }
    class PedidoRepository {
        <<interface>>
        +guardar(pedido) Pedido
        +buscarPorId(id) Optional
        +listar() List
    }
    class EnMemoriaPedidoRepository
    class ProductoRepository {
        <<interface>>
        +guardar(p) Producto
        +buscarPorId(id) Optional
        +eliminar(id) boolean
    }
    class EnMemoriaProductoRepository

    Cliente "1" --> "*" Pedido : realiza
    Pedido "1" *-- "1..*" DetallePedido : contiene
    DetallePedido --> Producto : referencia
    Pedido --> EstadoPedido
    Pedido --> FormaPago
    PedidoService --> PedidoRepository : usa (patrón Repositorio)
    ProductoService --> ProductoRepository : usa (patrón Repositorio)
    PedidoService --> PoliticaDescuento : usa (patrón Strategy)
    DescuentoPorMonto ..|> PoliticaDescuento
    EnMemoriaPedidoRepository ..|> PedidoRepository
    EnMemoriaProductoRepository ..|> ProductoRepository
```

**Descripción:** `Pedido` agrega uno o más `DetallePedido` que referencian un `Producto`. La lógica
vive en los servicios (`PedidoService`, `ProductoService`); el acceso a datos queda detrás de las
interfaces `PedidoRepository` / `ProductoRepository`; la regla de descuento se encapsula en la
estrategia `PoliticaDescuento`.

**Relevancia arquitectónica:** muestra **quién es responsable de qué** (separación de responsabilidades)
y hace visible el desacoplamiento: cambiar la base de datos solo implica otra implementación de
repositorio, sin tocar servicios ni controladores.

## 2.3 Diagrama de secuencia — Realizar pedido

```mermaid
sequenceDiagram
    actor Cliente
    participant V as Interfaz Web (Vista)
    participant PC as PedidoController (Controlador)
    participant PS as PedidoService (Servicio)
    participant PR as ProductoRepository
    participant PD as Datos en memoria (BD)
    participant PRp as PedidoRepository

    Cliente->>V: Selecciona productos y confirma
    V->>PC: POST /api/pedidos {cliente, formaPago, items}
    PC->>PS: crear(cliente, formaPago, items)
    PS->>PR: buscarPorId(cada item)
    PR->>PD: consultar producto
    PD-->>PR: Producto
    PR-->>PS: Producto(s)
    PS->>PS: Validar disponibilidad y cantidades
    PS->>PS: Regla: total < $5.000 no permite tarjeta
    PS->>PS: Regla: total >= $200.000 aplica 10% descuento
    PS->>PRp: guardar(pedido)
    PRp->>PD: persistir pedido (id asignado)
    PD-->>PRp: OK
    PRp-->>PS: Pedido registrado
    PS-->>PC: Pedido con totales
    PC-->>V: 201 Created (JSON)
    V-->>Cliente: Notificación: "Pedido #102 registrado"
```

**Descripción:** recorre el flujo completo desde la acción del usuario hasta la persistencia y la
notificación de confirmación, evidenciando las capas Vista → Controlador → Servicio → Repositorio → Datos.

**Relevancia arquitectónica:** demuestra que cada mensaje cruza una sola frontera por capa; si algo
falla (regla incumplida), el servicio responde y el controlador traduce a HTTP 422 — la interfaz nunca
habla directo con los datos.

## 2.4 Diagrama de despliegue

```mermaid
flowchart LR
    subgraph internet["Internet"]
        u((Usuario))
    end
    subgraph navegador["Nodo: Navegador del cliente"]
        ui["Interfaz web<br/>index.html + app.js"]
    end
    subgraph nube["Nube (Azure Container Apps / App Service)"]
        subgraph nodo1["Nodo: Servidor web - réplica 1 (pod)"]
            app1["CaféExpress.jar<br/>Controladores · Servicios · Repositorios<br/>puerto 8080"]
            datos1["Almacén de datos<br/>(memoria / BD)"]
        end
        subgraph nodo2["Nodo: Servidor web - réplica 2 (pod)"]
            app2["CaféExpress.jar<br/>réplica idéntica"]
            datos2["Almacén de datos<br/>(memoria / BD)"]
        end
        bal[("Balanceador<br/>de carga")]
    end

    u -->|HTTPS| navegador
    navegador -->|HTTPS REST JSON| bal
    bal --> app1
    bal --> app2
    app1 --- datos1
    app2 --- datos2
```

**Descripción:** el navegador ejecuta la vista; las peticiones HTTPS llegan al balanceador de carga de
la plataforma en la nube, que distribuye entre réplicas idénticas (pods) de la aplicación; cada réplica
accede a su almacén de datos (en memoria para el prototipo, base de datos administrada en producción).

**Relevancia arquitectónica:** documenta **dónde se ejecuta cada componente** y cómo crece el sistema:
escalar horizontalmente = agregar réplicas/pods sin reconstruir nada; escalar verticalmente = aumentar
RAM/CPU del nodo. Como el JAR no tiene dependencias nativas, la misma imagen corre igual en local o en la nube.
