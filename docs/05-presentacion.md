# 5. Guion de presentación (15 minutos)

| # | Diapositiva | Contenido | Tiempo |
|---|-------------|-----------|--------|
| 1 | Portada | Café Express — arquitectura de un sistema web de pedidos. Integrantes, curso. | 0:30 |
| 2 | El problema | Cafetería con pedidos en papel: errores, tickets perdidos, cliente sin información del estado. | 1:00 |
| 3 | Modelo del negocio | Actores (cliente, administrador), información (productos, pedidos, estados), procesos. | 1:30 |
| 4 | Requisitos funcionales | RF-01 a RF-06: consultar productos, realizar pedido, consultar estado, consultar/actualizar pedidos, gestionar productos. | 1:00 |
| 5 | Requisitos no funcionales | Fácil de usar, proteger información, mantenible, extensible, escalable → cada uno se resuelve **con arquitectura**, no con suerte. | 1:00 |
| 6 | Arquitectura en capas | Vista → Controlador → Servicio → Repositorio → Datos. Separación de responsabilidades; contraste con "todo mezclado en un archivo". | 2:00 |
| 7 | Casos de uso (UML) | Dos actores, seis casos, include de validación de reglas. Trazabilidad requisitos ↔ casos. | 1:00 |
| 8 | Clases (UML) | Pedido *-- DetallePedido → Producto; servicios usan interfaces de repositorio. | 1:00 |
| 9 | Secuencia (UML) | Recorrido completo de "realizar pedido" con las dos reglas de negocio ($200.000 descuento / $5.000 tarjeta). | 1:30 |
| 10 | Despliegue (UML) | Navegador → balanceador → pods idénticos en la nube; escalabilidad vertical vs horizontal (analogía de los tanques). | 1:30 |
| 11 | Patrones seleccionados | Repository, MVC, Strategy (descuento intercambiable), inyección de dependencias; descartes justificados (Singleton, API Gateway, CQRS). | 2:00 |
| 12 | Demo del prototipo | Menú → carrito → pedido con descuento; consulta de estado; admin: cambiar estado y gestionar productos; regla de tarjeta rechazada en vivo. | 2:00 |
| 13 | Conclusiones | La arquitectura cumple los 4 RNF; ruta de crecimiento (BD administrada, Identity Server, más pods). | 0:30 |

**Consejos para la demo:** dejar el servidor corriendo antes (`java -cp bin cafeexpress.App`),
tener preparado un pedido grande (10 × Combo Almuerzo = $220.000 → muestra el 10 % de descuento)
y uno pequeño en efectivo/tarjeta para mostrar la regla de $5.000.
