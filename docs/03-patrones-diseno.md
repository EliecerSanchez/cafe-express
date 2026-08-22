# 3. Selección de patrones de diseño

## 3.1 Patrones investigados

Se estudiaron los catálogos clásicos (Gamma et al., 1994; Fowler, 2002) y los patrones de arquitecturas
para la nube (Microsoft Azure, 2024), evaluando su pertinencia para un sistema de pedidos pequeño pero
con vocación de crecer:

| Patrón | Categoría | ¿Por qué se consideró? |
|--------|-----------|------------------------|
| MVC (Model-View-Controller) | Arquitectural | Separa interfaz, lógica y datos; el estándar para sistemas web. |
| Repository | Acceso a datos | Aísla la persistencia detrás de una interfaz; permite cambiar el motor de datos sin tocar la lógica. |
| Strategy (GoF) | Comportamiento GoF | Las reglas comerciales cambian (descuentos, promociones); conviene encapsularlas intercambiables. |
| Dependency Injection / IoC | Estructural | Reduce acoplamiento: los servicios reciben sus colaboradores en lugar de crearlos. |
| Singleton | Creacional GoF | Garantiza una única instancia de un recurso compartido. Descartado como patrón explícito: introduce estado global y dificulta pruebas; se prefiere inyectar un único repositorio desde `App`. |
| API Gateway / Backend for Frontend | Nube | Útil con muchos microservicios. Sobreingeniería para un módulo único; se documenta como evolución futura. |
| Circuit Breaker | Nube (resiliencia) | Aplica cuando hay servicios externos que pueden fallar; aún no existen dependencias externas. |
| CQRS | Nube / datos | Separar lecturas/escrituras ayuda a escalar mucho, pero duplica esfuerzo a esta escala; queda como evolución. |

## 3.2 Patrones seleccionados y justificación

### 3.2.1 MVC — separación de responsabilidades
La **Vista** (`static/index.html`, `app.js`) solo presenta información; el **Controlador**
(`controller/*Controller`) recibe las solicitudes HTTP, valida credenciales y delega; el **Modelo/Servicio**
(`service/*Service`, `domain/*`) concentra las reglas de negocio.
*Justificación:* responde directamente a RNF-03 (mantenibilidad) — el ejemplo del profesor de "3000 líneas
de código mezcladas" es exactamente lo que este patrón evita.

### 3.2.2 Repositorio (Repository)
`ProductoRepository` y `PedidoRepository` son interfaces; `EnMemoria...Repository` es una implementación.
*Justificación:* la capa de servicio no sabe dónde están los datos. Cuando el prototipo migre a SQL o a una
base administrada en la nube, solo se escribe otra implementación — cero cambios en controladores ni
servicios (Fowler, 2002). Además facilita pruebas unitarias con dobles de prueba.

### 3.2.3 Strategy — políticas de negocio intercambiables
`PoliticaDescuento` (interfaz) con la implementación `DescuentoPorMonto` (10 % sobre $200.000).
*Justificación:* las promociones de una cafetería cambian con frecuencia; agregar una nueva política es
crear una clase nueva e inyectarla, sin modificar `PedidoService` (abierto/cerrado).

### 3.2.4 Inyección de dependencias por constructor (IoC)
`App` construye repositorios → servicios → controladores y los conecta por constructor.
*Justificación:* bajo acoplamiento y composición explícita del sistema en un único punto; es la versión
ligera de un contenedor IoC (Spring), suficiente a esta escala.

### 3.2.5 Consideración de nube
El prototipo se empaqueta como contenedor **Docker** (JRE puro, sin dependencias nativas), lo que lo hace
ideal para plataformas de réplicas automáticas: **Azure Container Apps / App Service** crean *pods*
adicionales ante carga alta (escalabilidad horizontal descrita en clase) o más CPU/RAM al nodo
(escalabilidad vertical). Los patrones descartados (API Gateway, Circuit Breaker, CQRS) quedan
documentados como ruta de evolución cuando el sistema pase de un módulo a varios servicios.

## 3.3 Referencias utilizadas (APA)

- Gamma, E., Helm, R., Johnson, R., & Vlissides, J. (1994). *Design patterns: Elements of reusable object-oriented software*. Addison-Wesley.
- Fowler, M. (2002). *Patterns of enterprise application architecture*. Addison-Wesley.
- Bass, L., Clements, P., & Kazman, R. (2012). *Software architecture in practice* (3rd ed.). Addison-Wesley.
- Microsoft. (2024). *Cloud design patterns*. https://learn.microsoft.com/azure/architecture/patterns/
