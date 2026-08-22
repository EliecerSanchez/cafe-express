# 1. Análisis de requisitos — Café Express

## 1.1 Contexto del negocio

**Café Express** es una cafetería que necesita un sistema web pequeño para gestionar sus pedidos.
Antes de escribir código se modeló el negocio respondiendo las preguntas básicas del análisis:

| Pregunta | Respuesta |
|---|---|
| ¿Quién utiliza el sistema? | **Cliente** (pide y consulta) y **Administrador** (gestiona productos y pedidos). A futuro: proveedores. |
| ¿Qué información se maneja? | Productos, pedidos, detalle de cada pedido, estados, formas de pago, totales y descuentos. |
| ¿Qué procesos existen? | Tomar pedido → preparar → notificar listo → entregar; administrar catálogo; consultar ventas. |
| ¿Qué reglas aplica el negocio? | Descuento del 10 % en compras ≥ $200.000; tarjeta solo desde $5.000; ciclo de vida controlado del pedido. |

## 1.2 Técnica de análisis empleada

Se aplicaron dos técnicas recomendadas en clase:

1. **Entrevista / observación del proceso actual**: la cafetería toma pedidos en papel, lo que genera
   errores de transcripción, pérdida de tickets y desconocimiento del estado de un pedido por parte del cliente.
2. **Listado de eventos y actores**: a partir de los actores (cliente, administrador) se listan los
   eventos del negocio que disparan ("quiero un capuchino", "llegó la carta nueva"), y de cada evento
   se deriva un requisito funcional.

## 1.3 Requisitos funcionales (RF)

| ID | Requisito | Actor | Descripción |
|----|-----------|-------|-------------|
| RF-01 | Consultar los productos | Cliente | El sistema muestra el catálogo con nombre, categoría, precio y disponibilidad. |
| RF-02 | Realizar los pedidos | Cliente | El cliente selecciona productos y cantidades, indica su nombre y forma de pago; el sistema valida reglas y registra el pedido. |
| RF-03 | Consultar el estado del pedido | Cliente | Con el número de pedido el cliente consulta su estado actual y detalle. |
| RF-04 | Consultar los pedidos | Administrador | Lista todos los pedidos registrados con cliente, estado y totales. |
| RF-05 | Actualizar el estado del pedido | Administrador | Cambia el estado siguiendo transiciones válidas: RECIBIDO → EN_PREPARACION → LISTO → ENTREGADO (o CANCELADO). |
| RF-06 | Gestionar productos | Administrador | Crear, editar precio, habilitar/deshabilitar y eliminar productos del catálogo. |

## 1.4 Requisitos no funcionales (RNF)

| ID | Requisito no funcional | Cómo lo satisface la arquitectura |
|----|------------------------|-----------------------------------|
| RNF-01 | Fácil de utilizar | Interfaz web sencilla de una sola página, sin capacitación previa; API REST coherente. |
| RNF-02 | Proteger la información | Separación de capas + control de acceso: operaciones administrativas exigen token (`X-Admin-Token`); en producción se sustituiría por Identity Server (OAuth2/JWT). |
| RNF-03 | Fácil de mantener | Arquitectura en capas con separación de responsabilidades (interfaz, controlador, servicio, repositorio, datos): cada módulo tiene una única responsabilidad. |
| RNF-04 | Permitir agregar nuevas funcionalidades | Bajo acoplamiento mediante interfaces (`ProductoRepository`, `PoliticaDescuento`) e inyección de dependencias: se agregan módulos sin reescribir los existentes. |
| RNF-05 | Escalabilidad | Sin dependencias externas y con estado aislado en la capa de datos: puede escalar verticalmente (más recursos al nodo) u horizontalmente (réplicas/pods detrás de un balanceador). |

## 1.5 Trazabilidad requisito → decisión arquitectónica

| Requisito | Decisión |
|-----------|----------|
| RF-01..RF-06 | Casos de uso y endpoints REST específicos por caso (ver diagrama de casos de uso). |
| RNF-01 | Vista SPA con HTML/CSS/JS vanilla servida por el mismo servidor. |
| RNF-02 | Capa de controladores valida credenciales antes de invocar servicios. |
| RNF-03, RNF-04 | Patrón MVC + patrón Repositorio + inyección de dependencias por constructor. |
| RNF-05 | Contenedor Docker único JRE; despliegue como réplicas (pods) en la nube. |
