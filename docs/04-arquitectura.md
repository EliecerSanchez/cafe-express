# 4. Arquitectura propuesta — Café Express

## 4.1 Visión general

Arquitectura **en capas (layered)** organizada bajo el esquema **MVC**, donde cada capa tiene una única
responsabilidad y solo se comunica con la capa adyacente:

```
┌──────────────────────────────────────────────────────────┐
│ VISTA (presentación)                                     │
│ static/index.html · styles.css · app.js                  │
│ Lo que ve y usa el usuario                               │
├──────────────────────────────────────────────────────────┤
│ CONTROLADOR (API REST HTTP)                              │
│ controller/ProductoController · PedidoController         │
│ Recibe solicitudes, valida acceso, traduce a JSON        │
├──────────────────────────────────────────────────────────┤
│ SERVICIO (reglas de negocio)                             │
│ service/PedidoService · ProductoService                  │
│ + PoliticaDescuento (Strategy) · ReglaNegocioException   │
├──────────────────────────────────────────────────────────┤
│ REPOSITORIO (patrón Repository)                          │
│ repository/ProductoRepository · PedidoRepository (interf.)│
│ repository/memory/EnMemoria...(implementación)           │
├──────────────────────────────────────────────────────────┤
│ DATOS                                                    │
│ Almacén en memoria sembrado (prototipo) / BD en prod.    │
└──────────────────────────────────────────────────────────┘
```

## 4.2 Responsabilidad por paquete

| Paquete | Responsabilidad | No debe hacer |
|---------|-----------------|---------------|
| `domain` | Entidades del negocio y sus invariantes (Pedido, DetallePedido, Producto, enums). | No conoce HTTP ni almacenamiento. |
| `repository` | Contratos de acceso a datos + implementación en memoria. | No contiene reglas de negocio. |
| `service` | Casos de uso y reglas (mínimo $5.000 tarjeta, 10 % ≥ $200.000, transiciones de estado). | No habla JSON ni HTTP. |
| `controller` | Traducir HTTP ↔ llamadas de servicio; códigos 400/401/404/422. | No decide reglas de negocio. |
| `web` | Infraestructura compartida: utilidades HTTP y JSON mínimas, archivos estáticos. | — |
| `static` | Interfaz del usuario (SPA ligera vanilla JS). | No calcula totales definitivos: solo refleja lo que responde el servicio. |

## 4.3 Flujo de una petición (ejemplo)

`POST /api/pedidos` → `PedidoController.crear()` → `PedidoService.crear()`
→ valida cliente/items/forma de pago → consulta productos vía `ProductoRepository`
→ aplica `PoliticaDescuento` → guarda con `PedidoRepository` → responde `201` con el pedido en JSON.

Errores: regla incumplida → `422` con mensaje claro; sin token admin → `401`;
recurso inexistente → `404`; JSON malformado → `400`.

## 4.4 Escalabilidad (vertical vs horizontal)

| Tipo | Mecanismo | En este proyecto |
|------|-----------|------------------|
| Vertical | Potenciar un único servidor (más RAM/CPU/procesador). | El JAR aprovecha hilos (`Executors.newFixedThreadPool(16)`); basta subir el tamaño del nodo. |
| Horizontal | Duplicar instancias (réplicas/pods) detrás de un balanceador. | Imagen Docker idéntica y sin estado fuera de la capa de datos: la plataforma crea pods nuevos cuando sube la carga (Azure Container Apps). |

Analogía de clase: llenar tanques — cuando uno se llena, se agrega otro tanque idéntico, en lugar de
agrandar infinitamente el primero.

Límite actual honesto: con datos **en memoria** cada réplica tiene su propio conjunto de datos; para
escalar horizontalmente en producción el almacén debe externalizarse (SQL administrada). La arquitectura
ya está preparada: solo cambia la implementación del repositorio.

## 4.5 Seguridad

1. Operaciones administrativas protegidas por token (`X-Admin-Token`).
2. Validación de todas las entradas en servicio (nunca confiar en el cliente).
3. Ruta de evolución documentada: sustituir el token por Identity Server (OAuth2/JWT) sin tocar la lógica,
   porque la verificación ocurre en un único punto de la capa controlador.

## 4.6 Cómo agregar una funcionalidad nueva (evidencia RNF-04)

Ejemplo: *"notificar por correo cuando el pedido esté LISTO"*.
1. Nueva interfaz `CanalNotificacion` y su implementación `CorreoNotificacion`.
2. Inyección en `PedidoService.cambiarEstado(...)`.
3. Ninguna capa existente se reescribe: solo se compone en `App`.

## 4.7 Ejecución del prototipo

```powershell
javac -encoding UTF-8 -d bin (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp bin cafeexpress.App          # http://localhost:8080
# Administración: pestaña "Administración", token demostrativo: admin123
```

Con Docker:

```bash
docker build -t cafe-express .
docker run -p 8080:8080 cafe-express
```

## 4.8 Endpoints del API

| Método | Ruta | Actor | Descripción |
|--------|------|-------|-------------|
| GET | `/api/productos` | Cliente/Admin | Catálogo completo (admin) |
| GET | `/api/productos?disponibles=true` | Cliente | Solo productos disponibles |
| GET | `/api/productos/{id}` | Ambos | Detalle de producto |
| POST | `/api/productos` | Admin | Crear producto |
| PUT | `/api/productos/{id}` | Admin | Actualizar producto |
| DELETE | `/api/productos/{id}` | Admin | Eliminar producto |
| POST | `/api/pedidos` | Cliente | Realizar pedido |
| GET | `/api/pedidos/{id}` | Cliente | Consultar estado/detalle |
| GET | `/api/pedidos` | Admin | Listar todos los pedidos |
| PUT | `/api/pedidos/{id}/estado` | Admin | Actualizar estado |
