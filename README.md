# ☕ Café Express — Sistema web de gestión de pedidos

Proyecto de la **Unidad 1 – Actividad 2** de *Arquitectura de Software*: diseño y prototipo funcional
de un sistema web pequeño para gestionar los pedidos de una cafetería, con arquitectura en capas (MVC),
patrón Repositorio, Strategy para reglas de negocio e inyección de dependencias.

## Estructura del repositorio

```
├── README.md                     Este archivo
├── cafe-express.jar              JAR ejecutable (java -jar)
├── Dockerfile                    Empaquetado para despliegue en la nube
├── docs/
│   ├── 01-analisis-requisitos.md RF / RNF + técnica de análisis + trazabilidad
│   ├── 02-diagramas-uml.md       Casos de uso, clases, secuencia y despliegue (Mermaid)
│   ├── 03-patrones-diseno.md     Patrones investigados, seleccionados y justificados
│   ├── 04-arquitectura.md        Capas, escalabilidad, seguridad y API
│   └── 05-presentacion.md        Guion de presentación (15 min)
├── informe/
│   ├── informe-cafe-express.docx   Informe técnico en Word (formato APA 7, 9 páginas)
│   ├── informe-cafe-express.html   Versión web del informe (Ctrl+P → PDF)
│   ├── informe-cafe-express.rtf    Versión RTF equivalente
│   └── img/                        Diagramas UML renderizados (SVG/PNG)
├── src/cafeexpress/
│   ├── App.java                  Composición del sistema (inyección de dependencias)
│   ├── controller/               Capa controladora HTTP (MVC)
│   ├── service/                  Reglas de negocio + Strategy (PoliticaDescuento)
│   ├── repository/               Interfaces del patrón Repositorio
│   │   └── memory/               Implementación en memoria
│   ├── domain/                   Entidades: Producto, Pedido, DetallePedido, enums
│   └── web/                      Infraestructura HTTP/JSON mínima (sin librerías)
└── static/                       Vista: interfaz web SPA (HTML/CSS/JS vanilla)
```

## Ejecución local

**Opción rápida — JAR ejecutable (solo necesita JDK instalado):**

```powershell
java -jar cafe-express.jar
```

**Opción completa — compilar desde el código fuente:**

```powershell
javac -encoding UTF-8 -d bin (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp bin cafeexpress.App
```

**Recompilar el JAR desde el código fuente:**

```powershell
javac -encoding UTF-8 -d bin (Get-ChildItem -Recurse src -Filter *.java).FullName
New-Item -ItemType Directory -Force META-INF | Out-Null
Set-Content -Path META-INF\MANIFEST.MF -Value "Manifest-Version: 1.0`nMain-Class: cafeexpress.App`n" -NoNewline
jar cfm cafe-express.jar META-INF\MANIFEST.MF -C bin .
Remove-Item -Recurse -Force bin, META-INF
```

Abrir http://localhost:8080 — token de administración demostrativo: `admin123`.

## Ejecución con Docker (nube)

```bash
docker build -t cafe-express .
docker run -p 8080:8080 cafe-express
```

Compatible con Azure Container Apps / App Service: cada réplica es un pod idéntico detrás del
balanceador de carga (escalabilidad horizontal).

## Reglas de negocio implementadas

1. Descuento automático del **10 %** en pedidos ≥ **$200.000**.
2. Pagos con **tarjeta solo desde $5.000**.
3. Ciclo de vida del pedido con transiciones válidas:
   `RECIBIDO → EN_PREPARACION → LISTO → ENTREGADO` (o `CANCELADO`).

## Documentación completa

Ver `docs/` (fuentes Markdown con diagramas Mermaid) y `informe/informe-cafe-express.docx`
(informe técnico en formato APA 7.ª ed.: portada, resumen, introducción, requisitos, UML,
patrones, prototipo, conclusiones y referencias).
