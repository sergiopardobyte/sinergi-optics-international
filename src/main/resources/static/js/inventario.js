
/* ============================================================
   PROYECTO: SINERGI OPTICS INTERNATIONAL
   ARCHIVO: inventario.js

   DESCRIPCIÓN:
   Este archivo permite consultar, mostrar y administrar
   los productos registrados en el inventario de la óptica.
   ============================================================ */

// Dirección principal de nuestra API de productos.
const API_PRODUCTOS = "/api/productos";


// ============================================================
// 1. ELEMENTOS HTML DEL PANEL DE INVENTARIO
// ============================================================

// Contador de todos los productos registrados.
const totalProductos = document.getElementById("total-productos");

// Contador de productos con pocas unidades.
const totalStockBajo = document.getElementById("total-stock-bajo");

// Contador de productos agotados.
const totalAgotados = document.getElementById("total-agotados");

// Contenedor de productos con poco stock.
const listaStockBajo = document.getElementById("lista-stock-bajo");

// Contenedor de productos agotados.
const listaAgotados = document.getElementById("lista-agotados");

// Contenedor de todos los productos registrados.
const listaTodosProductos = document.getElementById("lista-todos-productos");


// ============================================================
// 2. FUNCIÓN PRINCIPAL PARA CARGAR EL INVENTARIO
// ============================================================

async function cargarInventario() {

    try {

        // Consultamos todos los productos registrados.
        const respuestaProductos = await fetch(API_PRODUCTOS);

        if (!respuestaProductos.ok) {
            throw new Error("No se pudieron consultar los productos.");
        }

        const productos = await respuestaProductos.json();

        // Mostramos todos los productos registrados.
        mostrarListaInventario(
            productos,
            listaTodosProductos,
            "Todavía no hay productos registrados."
        );


        // Consultamos los productos con poco stock.
        const respuestaStockBajo = await fetch(
            `${API_PRODUCTOS}/poco-stock`
        );

        if (!respuestaStockBajo.ok) {
            throw new Error("No se pudo consultar el stock bajo.");
        }

        const productosStockBajo = await respuestaStockBajo.json();


        // Consultamos los productos agotados.
        const respuestaAgotados = await fetch(
            `${API_PRODUCTOS}/agotados`
        );

        if (!respuestaAgotados.ok) {
            throw new Error("No se pudieron consultar los productos agotados.");
        }

        const productosAgotados = await respuestaAgotados.json();


        // Actualizamos los contadores del panel.
        totalProductos.textContent = productos.length;
        totalStockBajo.textContent = productosStockBajo.length;
        totalAgotados.textContent = productosAgotados.length;


        // Mostramos los productos con poco stock.
        mostrarListaInventario(
            productosStockBajo,
            listaStockBajo,
            "No hay productos con poco stock."
        );

        // Mostramos los productos agotados.
        mostrarListaInventario(
            productosAgotados,
            listaAgotados,
            "No hay productos agotados."
        );

    } catch (error) {

        // Mostramos el error en la consola del navegador.
        console.error("Error al cargar el inventario:", error);

        // Informamos si ocurrió algún problema.
        listaTodosProductos.textContent = "No se pudo cargar el inventario.";
        listaStockBajo.textContent = "No se pudo cargar el inventario.";
        listaAgotados.textContent = "No se pudo cargar el inventario.";
    }
}


// ============================================================
// 3. FUNCIÓN PARA MOSTRAR LOS PRODUCTOS EN UNA TABLA
// ============================================================

function mostrarListaInventario(productos, contenedor, mensajeVacio) {

    // Limpiamos el contenido anterior.
    contenedor.innerHTML = "";

    // Si no existen productos, mostramos un mensaje.
    if (productos.length === 0) {

        contenedor.innerHTML = `
            <div class="inventario-vacio">
                ${mensajeVacio}
            </div>
        `;

        return;
    }

    // Creamos la tabla del inventario.
    const tabla = document.createElement("table");

    tabla.className = "tabla-inventario";

    // Creamos el encabezado de la tabla.
    tabla.innerHTML = `
        <thead>
            <tr>
                <th>Producto</th>
                <th>Categoría</th>
                <th>Precio</th>
                <th>Existencias</th>
                <th>Estado</th>
                <th>Acciones</th>
            </tr>
        </thead>
        <tbody></tbody>
    `;

    // Seleccionamos el cuerpo de la tabla.
    const cuerpoTabla = tabla.querySelector("tbody");

    // Recorremos todos los productos.
    productos.forEach(producto => {

        // Creamos una fila para cada producto.
        const fila = document.createElement("tr");

        // Determinamos el estado de las existencias.
        let estado = "Disponible";
        let claseEstado = "estado-disponible";

        if (producto.stock === 0) {

            estado = "Agotado";
            claseEstado = "estado-agotado";

        } else if (producto.stock <= 5) {

            estado = "Stock bajo";
            claseEstado = "estado-bajo";
        }

        // Agregamos los datos y botones de cada producto.
        fila.innerHTML = `
            <td>${producto.nombre ?? ""}</td>

            <td>${producto.categoria ?? ""}</td>

            <td>$${Number(producto.precio ?? 0).toFixed(2)}</td>

            <td>${producto.stock ?? 0} unidades</td>

            <td>
                <span class="${claseEstado}">
                    ${estado}
                </span>
            </td>

            <!-- Botones para administrar el producto -->
            <td>
                <button
                    class="btn-modificar-stock"
                    onclick="modificarStock(${producto.id})">
                    Modificar stock
                </button>

                <a
                    class="btn-editar-producto"
                    href="/?editar=${producto.id}">
                    Editar producto
                </a>
            </td>
        `;

        // Incorporamos la fila a la tabla.
        cuerpoTabla.appendChild(fila);
    });

    // Mostramos la tabla en la página.
    contenedor.appendChild(tabla);
}


// ============================================================
// 4. FUNCIÓN PARA MODIFICAR LAS EXISTENCIAS DE UN PRODUCTO
// ============================================================

async function modificarStock(id) {

    try {

        // Consultamos los datos actuales del producto.
        const respuesta = await fetch(`${API_PRODUCTOS}/${id}`);

        if (!respuesta.ok) {
            throw new Error("No se pudo consultar el producto.");
        }

        const producto = await respuesta.json();

        // Solicitamos la nueva cantidad de existencias.
        const nuevoStock = prompt(
            `Producto: ${producto.nombre}\n` +
            `Stock actual: ${producto.stock}\n\n` +
            "Ingrese las nuevas existencias:",
            producto.stock
        );

        // Si el usuario cancela, no realizamos cambios.
        if (nuevoStock === null) {
            return;
        }

        // Convertimos el valor ingresado a número.
        const cantidad = Number(nuevoStock);

        // Validamos que sea un número entero no negativo.
        if (
            nuevoStock.trim() === "" ||
            !Number.isInteger(cantidad) ||
            cantidad < 0
        ) {
            alert("Ingrese una cantidad válida, igual o mayor que cero.");
            return;
        }

        // Enviamos los datos actualizados al servidor.
        const respuestaActualizacion = await fetch(
            `${API_PRODUCTOS}/${id}`,
            {
                method: "PUT",

                headers: {
                    "Content-Type": "application/json"
                },

                // Conservamos los demás datos del producto.
                body: JSON.stringify({
                    nombre: producto.nombre,
                    descripcion: producto.descripcion,
                    categoria: producto.categoria,
                    precio: producto.precio,
                    stock: cantidad
                })
            }
        );

        // Comprobamos si el servidor aceptó la actualización.
        if (
            !respuestaActualizacion.ok ||
            respuestaActualizacion.redirected
        ) {
            throw new Error(
                "No se pudo actualizar el stock. Verifique que haya iniciado sesión como administrador."
            );
        }

        // Informamos que la operación fue exitosa.
        alert("Las existencias se actualizaron correctamente.");

        // Recargamos las tablas y los contadores.
        await cargarInventario();

    } catch (error) {

        // Mostramos el error en la consola.
        console.error("Error al modificar el stock:", error);

        // Informamos al usuario.
        alert(error.message);
    }
}


// ============================================================
// 5. INICIAR EL PANEL DE INVENTARIO
// ============================================================

// Ejecutamos la consulta cuando se abre esta página.
cargarInventario();
