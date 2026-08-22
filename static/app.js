const API = {
    productos: "/api/productos",
    pedidos: "/api/pedidos"
};

const ESTADOS = ["RECIBIDO", "EN_PREPARACION", "LISTO", "ENTREGADO"];

let carrito = {};
let tokenAdmin = "";

const $ = id => document.getElementById(id);

function dinero(valor) {
    return "$" + Number(valor || 0).toLocaleString("es-CO");
}

function cabecerasAdmin() {
    return { "Content-Type": "application/json", "X-Admin-Token": tokenAdmin };
}

async function pedirJson(url, opciones = {}) {
    const respuesta = await fetch(url, opciones);
    const cuerpo = await respuesta.json().catch(() => ({}));
    if (!respuesta.ok) {
        throw new Error(cuerpo.error || "Error " + respuesta.status);
    }
    return cuerpo;
}

function mostrarVista(nombre) {
    document.querySelectorAll(".vista").forEach(v => v.classList.remove("visible"));
    $("vista-" + nombre).classList.add("visible");
    document.querySelectorAll(".pestana").forEach(p => p.classList.toggle("activa", p.dataset.vista === nombre));
}

document.querySelectorAll(".pestana").forEach(boton => {
    boton.addEventListener("click", () => mostrarVista(boton.dataset.vista));
});

async function cargarProductos() {
    try {
        const productos = await pedirJson(API.productos + "?disponibles=true");
        const contenedor = $("lista-productos");
        contenedor.innerHTML = "";
        productos.forEach(producto => {
            const tarjeta = document.createElement("article");
            tarjeta.className = "tarjeta";
            tarjeta.innerHTML = `
                <span class="categoria">${producto.categoria}</span>
                <h3>${producto.nombre}</h3>
                <span class="precio">${dinero(producto.precio)}</span>
                <button class="primario" type="button">Agregar</button>`;
            tarjeta.querySelector("button").addEventListener("click", () => agregarAlCarrito(producto));
            contenedor.appendChild(tarjeta);
        });
    } catch (e) {
        $("lista-productos").innerHTML = `<p class="error">No fue posible cargar el menú: ${e.message}</p>`;
    }
}

function agregarAlCarrito(producto) {
    const actual = carrito[producto.id];
    carrito[producto.id] = {
        productoId: producto.id,
        nombre: producto.nombre,
        precio: producto.precio,
        cantidad: (actual ? actual.cantidad : 0) + 1
    };
    pintarCarrito();
}

function quitarDelCarrito(id) {
    delete carrito[id];
    pintarCarrito();
}

function pintarCarrito() {
    const items = Object.values(carrito);
    const lista = $("carrito-lista");
    lista.innerHTML = "";
    if (!items.length) {
        lista.innerHTML = '<li class="vacio">Aún no agregas productos.</li>';
    }
    let subtotal = 0;
    items.forEach(item => {
        subtotal += item.precio * item.cantidad;
        const li = document.createElement("li");
        li.innerHTML = `<span>${item.nombre} x${item.cantidad}</span><span>${dinero(item.precio * item.cantidad)} <button title="Quitar">&#10005;</button></span>`;
        li.querySelector("button").addEventListener("click", () => quitarDelCarrito(item.productoId));
        lista.appendChild(li);
    });
    const descuento = subtotal >= 200000 ? subtotal * 0.10 : 0;
    $("carrito-subtotal").textContent = dinero(subtotal);
    $("carrito-descuento").textContent = "-" + dinero(descuento);
    $("carrito-total").textContent = dinero(subtotal - descuento);
}

$("btn-realizar").addEventListener("click", async () => {
    $("mensaje-pedido").classList.add("oculto");
    $("error-pedido").textContent = "";
    const items = Object.values(carrito).map(i => ({ productoId: i.productoId, cantidad: i.cantidad }));
    if (!items.length) {
        $("error-pedido").textContent = "Agrega al menos un producto al pedido.";
        return;
    }
    try {
        const pedido = await pedirJson(API.pedidos, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                cliente: $("cliente").value,
                formaPago: $("forma-pago").value,
                items
            })
        });
        $("mensaje-pedido").textContent = `Pedido #${pedido.id} registrado. Total: ${dinero(pedido.total)}. Consulta su estado en la pestaña "Mi pedido".`;
        $("mensaje-pedido").classList.remove("oculto");
        carrito = {};
        pintarCarrito();
    } catch (e) {
        $("error-pedido").textContent = e.message;
    }
});

$("btn-consultar").addEventListener("click", async () => {
    $("error-consulta").textContent = "";
    const id = $("numero-pedido").value;
    if (!id) {
        $("error-consulta").textContent = "Escribe el número del pedido.";
        return;
    }
    try {
        const pedido = await pedirJson(`${API.pedidos}/${id}`);
        pintarResultado(pedido);
    } catch (e) {
        $("resultado-pedido").classList.add("oculto");
        $("error-consulta").textContent = e.message;
    }
});

function pintarResultado(pedido) {
    $("resultado-pedido").classList.remove("oculto");
    const linea = $("linea-tiempo");
    linea.innerHTML = "";
    ESTADOS.forEach(estado => {
        const paso = document.createElement("div");
        paso.className = "paso" + (estado === pedido.estado ? " actual" : "");
        paso.textContent = estado.replace("_", " ");
        linea.appendChild(paso);
        if (estado === pedido.estado) return;
    });
    if (pedido.estado === "ENTREGADO") {
        [...linea.children].forEach(p => p.classList.add("hecho"));
    }
    if (pedido.estado === "CANCELADO") {
        linea.innerHTML = '<div class="paso actual">PEDIDO CANCELADO</div>';
    }

    const filas = pedido.detalles.map(d =>
        `<tr><td>${d.nombreProducto}</td><td>x${d.cantidad}</td><td>${dinero(d.precioUnitario)}</td><td>${dinero(d.subtotal)}</td></tr>`
    ).join("");
    $("resumen-pedido").innerHTML = `
        <h2>Pedido #${pedido.id} &middot; ${pedido.cliente}</h2>
        <table>
            <thead><tr><th>Producto</th><th>Cant.</th><th>Precio</th><th>Subtotal</th></tr></thead>
            <tbody>${filas}</tbody>
        </table>
        <div class="totales-linea">
            <span>Subtotal ${dinero(pedido.subtotal)}</span>
            <span>Descuento -${dinero(pedido.descuento)}</span>
            <strong class="final">${pedido.formaPago} &middot; Total: ${dinero(pedido.total)}</strong>
        </div>`;
}

$("btn-conectar").addEventListener("click", () => {
    tokenAdmin = $("token-admin").value.trim();
    $("panel-admin").classList.toggle("oculto", !tokenAdmin);
    if (tokenAdmin) {
        cargarTablaProductos();
        cargarTablaPedidos();
    } else {
        $("error-admin").textContent = "";
    }
});

$("btn-refrescar-productos").addEventListener("click", cargarTablaProductos);
$("btn-refrescar-pedidos").addEventListener("click", cargarTablaPedidos);

async function cargarTablaProductos() {
    try {
        const productos = await pedirJson(API.productos, { headers: cabecerasAdmin() });
        const tabla = $("tabla-productos");
        tabla.innerHTML = `
            <thead><tr><th>ID</th><th>Nombre</th><th>Categoría</th><th>Precio</th><th>Disponible</th><th></th></tr></thead>
            <tbody></tbody>`;
        const cuerpo = tabla.querySelector("tbody");
        productos.forEach(p => {
            const fila = document.createElement("tr");
            fila.innerHTML = `
                <td>${p.id}</td><td>${p.nombre}</td><td>${p.categoria}</td><td>${dinero(p.precio)}</td>
                <td>${p.disponible ? "Sí" : "No"}</td>
                <td style="white-space:nowrap">
                    <button class="mini">Editar</button>
                    <button class="mini">${p.disponible ? "Deshabilitar" : "Habilitar"}</button>
                    <button class="mini peligro">Eliminar</button>
                </td>`;
            const [btnEditar, btnDisponible, btnEliminar] = fila.querySelectorAll("button");
            btnEditar.addEventListener("click", () => editarProducto(p));
            btnDisponible.addEventListener("click", () =>
                guardarProducto({ id: p.id, nombre: p.nombre, categoria: p.categoria, precio: p.precio, disponible: !p.disponible }));
            btnEliminar.addEventListener("click", () => eliminarProducto(p.id));
            cuerpo.appendChild(fila);
        });
    } catch (e) {
        $("error-admin").textContent = e.message;
    }
}

function editarProducto(p) {
    $("prod-id").value = p.id;
    $("prod-nombre").value = p.nombre;
    $("prod-categoria").value = p.categoria;
    $("prod-precio").value = p.precio;
    $("btn-cancelar-edicion").classList.remove("oculto");
}

$("btn-cancelar-edicion").addEventListener("click", () => {
    $("form-producto").reset();
    $("prod-id").value = "";
    $("btn-cancelar-edicion").classList.add("oculto");
});

$("form-producto").addEventListener("submit", evento => {
    evento.preventDefault();
    const datos = {
        nombre: $("prod-nombre").value,
        categoria: $("prod-categoria").value || "General",
        precio: Number($("prod-precio").value),
        disponible: true
    };
    const id = $("prod-id").value;
    if (id) {
        datos.id = Number(id);
        datos.disponible = true;
    }
    guardarProducto(datos, !!id);
});

async function guardarProducto(datos, esEdicion) {
    try {
        const url = esEdicion ? `${API.productos}/${datos.id}` : API.productos;
        await pedirJson(url, {
            method: esEdicion ? "PUT" : "POST",
            headers: cabecerasAdmin(),
            body: JSON.stringify(datos)
        });
        $("form-producto").reset();
        $("prod-id").value = "";
        $("btn-cancelar-edicion").classList.add("oculto");
        cargarTablaProductos();
        cargarProductos();
    } catch (e) {
        $("error-admin").textContent = e.message;
    }
}

async function eliminarProducto(id) {
    try {
        await pedirJson(`${API.productos}/${id}`, { method: "DELETE", headers: cabecerasAdmin() });
        cargarTablaProductos();
        cargarProductos();
    } catch (e) {
        $("error-admin").textContent = e.message;
    }
}

async function cargarTablaPedidos() {
    try {
        const pedidos = await pedirJson(API.pedidos, { headers: cabecerasAdmin() });
        const tabla = $("tabla-pedidos");
        tabla.innerHTML = `
            <thead><tr><th>#</th><th>Cliente</th><th>Estado</th><th>Total</th><th>Cambiar estado</th></tr></thead>
            <tbody></tbody>`;
        const cuerpo = tabla.querySelector("tbody");
        pedidos.forEach(p => {
            const opcionesTransicion = transicionesPermitidas(p.estado)
                .map(e => `<option value="${e}">${e.replace("_", " ")}</option>`).join("");
            const fila = document.createElement("tr");
            fila.innerHTML = `
                <td>${p.id}</td><td>${p.cliente}</td><td>${p.estado.replace("_", " ")}</td><td>${dinero(p.total)}</td>
                <td><select ${opcionesTransicion ? "" : "disabled"}><option value="">--</option>${opcionesTransicion}</select></td>`;
            const selector = fila.querySelector("select");
            selector.addEventListener("change", () => cambiarEstado(p.id, selector.value));
            cuerpo.appendChild(fila);
        });
    } catch (e) {
        $("error-admin").textContent = e.message;
    }
}

function transicionesPermitidas(estado) {
    switch (estado) {
        case "RECIBIDO": return ["EN_PREPARACION", "CANCELADO"];
        case "EN_PREPARACION": return ["LISTO", "CANCELADO"];
        case "LISTO": return ["ENTREGADO"];
        default: return [];
    }
}

async function cambiarEstado(id, estado) {
    if (!estado) return;
    try {
        await pedirJson(`${API.pedidos}/${id}/estado`, {
            method: "PUT",
            headers: cabecerasAdmin(),
            body: JSON.stringify({ estado })
        });
        cargarTablaPedidos();
    } catch (e) {
        $("error-admin").textContent = e.message;
        cargarTablaPedidos();
    }
}

cargarProductos();
pintarCarrito();
