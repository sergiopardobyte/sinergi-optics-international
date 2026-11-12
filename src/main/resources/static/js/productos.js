/* ============================================================
   PROYECTO: SINERGI OPTICS INTERNATIONAL
   ARCHIVO: productos.js

   DESCRIPCIÓN:
   Este archivo permite administrar los productos de nuestra
   óptica mediante la conexión con el backend de Spring Boot.

   FUNCIONES:
   1. Consultar y mostrar productos.
   2. Buscar productos por nombre, descripción o categoría.
   3. Registrar nuevos productos.
   4. Editar productos existentes.
   5. Actualizar productos.
   6. Eliminar productos.
   7. Subir fotografías.
   8. Consultar el inventario.
   ============================================================ */


/* ============================================================
   1. CONEXIÓN CON LA API
   ============================================================ */

// Dirección de nuestra API REST creada con Spring Boot.
const API_PRODUCTOS = "/api/productos";


/* ============================================================
   2. ELEMENTOS DEL DOCUMENTO HTML
   ============================================================ */

// Contenedor donde aparecerán las tarjetas del catálogo.
const listaProductos = document.getElementById("lista-productos");

// Campo donde el usuario escribe el producto que desea buscar.
const buscadorProductos = document.getElementById("busqueda-productos");

// Botón que ejecuta la búsqueda.
const botonBuscarProductos = document.getElementById("btn-buscar-productos");

// Variable que almacena todos los productos recibidos del servidor.
// Se utiliza para realizar las búsquedas sin consultar nuevamente la API.
let productosCargados = [];

// Formulario para registrar y editar productos.
const formulario = document.getElementById("formulario-producto");

// Elemento donde mostramos mensajes de éxito o error.
const mensaje = document.getElementById("mensaje-producto");

// Elementos del panel de inventario.
const totalProductos = document.getElementById("total-productos");
const totalStockBajo = document.getElementById("total-stock-bajo");
const totalAgotados = document.getElementById("total-agotados");
const listaStockBajo = document.getElementById("lista-stock-bajo");
const listaAgotados = document.getElementById("lista-agotados");

// Campo para seleccionar una fotografía.
const inputImagen = document.getElementById("imagen");

// Contenedor para mostrar la vista previa de la fotografía.
const vistaPreviaImagen = document.getElementById("vista-previa-imagen");

// Almacena la dirección de la fotografía actual del producto.
let imagenActual = "";

// Almacena la dirección temporal de una vista previa.
let direccionVistaPrevia = null;

// Guarda el ID del producto que estamos editando.
// Si es null, significa que estamos registrando uno nuevo.
let productoEditando = null;

// Obtenemos el botón principal del formulario.
const botonGuardar = formulario
    ? formulario.querySelector('button[type="submit"]')
    : null;


/* ============================================================
   3. VISTA PREVIA DE FOTOGRAFÍAS
   ============================================================ */

// Esta función permite visualizar una fotografía antes de subirla.
function mostrarVistaPrevia(archivo) {

    // Comprobamos que exista el contenedor de vista previa.
    if (!vistaPreviaImagen) {
        return;
    }

    // Limpiamos el contenido anterior.
    vistaPreviaImagen.innerHTML = "";

    // Si no se seleccionó una fotografía, mostramos un mensaje.
    if (!archivo) {
        vistaPreviaImagen.innerHTML =
            "<p>Selecciona una fotografía para visualizarla.</p>";
        return;
    }

    // Formatos de imagen permitidos.
    const formatosPermitidos = [
        "image/jpeg",
        "image/png",
        "image/webp"
    ];

    // Comprobamos que el formato sea válido.
    if (!formatosPermitidos.includes(archivo.type)) {

        vistaPreviaImagen.innerHTML =
            "<p>Formato no permitido. Usa JPG, PNG o WEBP.</p>";

        inputImagen.value = "";
        return;
    }

    // Comprobamos que la imagen no supere los 5 MB.
    if (archivo.size > 5 * 1024 * 1024) {

        vistaPreviaImagen.innerHTML =
            "<p>La fotografía supera el límite de 5 MB.</p>";

        inputImagen.value = "";
        return;
    }

    // Liberamos la dirección temporal anterior.
    if (direccionVistaPrevia) {
        URL.revokeObjectURL(direccionVistaPrevia);
    }

    // Creamos una dirección temporal para visualizar la fotografía.
    direccionVistaPrevia = URL.createObjectURL(archivo);

    // Creamos el elemento de imagen.
    const imagen = document.createElement("img");

    imagen.src = direccionVistaPrevia;
    imagen.alt = "Vista previa del producto";

    // Establecemos el tamaño de la fotografía.
    imagen.style.width = "200px";
    imagen.style.height = "150px";
    imagen.style.objectFit = "contain";
    imagen.style.marginTop = "10px";
    imagen.style.borderRadius = "10px";

    // Agregamos la imagen al contenedor.
    vistaPreviaImagen.appendChild(imagen);
}


// Detectamos cuando el usuario selecciona una fotografía.
if (inputImagen) {

    inputImagen.addEventListener("change", function() {

        // Obtenemos el archivo seleccionado.
        const archivo = inputImagen.files[0];

        // Mostramos la vista previa.
        mostrarVistaPrevia(archivo);
    });

}


/* ============================================================
   4. SUBIR FOTOGRAFÍAS AL SERVIDOR
   ============================================================ */

// Esta función envía la fotografía al backend de Spring Boot.
async function subirImagenProducto(idProducto) {

    // Obtenemos la fotografía seleccionada.
    const archivo = inputImagen.files[0];

    // Si no existe una fotografía nueva, no hacemos nada.
    if (!archivo) {
        return null;
    }

    // Creamos un objeto FormData para enviar archivos.
    const datosImagen = new FormData();

    // El nombre "archivo" debe coincidir con el controlador Java.
    datosImagen.append("archivo", archivo);

    try {

        // Enviamos la fotografía al servidor.
        const respuesta = await fetch(
            `${API_PRODUCTOS}/${idProducto}/imagen`,
            {
                method: "POST",
                body: datosImagen
            }
        );

        // Comprobamos si la subida fue correcta.
        if (!respuesta.ok) {
            throw new Error("No se pudo subir la fotografía.");
        }

        // Devolvemos la información actualizada del producto.
        return await respuesta.json();

    } catch (error) {

        console.error("Error al subir la fotografía:", error);
        throw error;
    }
}


/* ============================================================
   5. BOTÓN PARA CANCELAR UNA EDICIÓN
   ============================================================ */

// Creamos el botón de cancelar solamente si existe el formulario.
let botonCancelar = null;

if (formulario) {

    botonCancelar = document.createElement("button");

    botonCancelar.type = "button";
    botonCancelar.textContent = "Cancelar edición";
    botonCancelar.style.display = "none";

    formulario.appendChild(botonCancelar);
}


/* ============================================================
   6. CARGAR EL INVENTARIO
   ============================================================ */

// Consulta los productos y actualiza el panel de inventario.
async function cargarInventario() {

    // Comprobamos que los elementos del inventario existan.
    if (
        !totalProductos ||
        !totalStockBajo ||
        !totalAgotados ||
        !listaStockBajo ||
        !listaAgotados
    ) {
        return;
    }

    try {

        // Consultamos todos los productos.
        const respuestaProductos = await fetch(API_PRODUCTOS);

        if (!respuestaProductos.ok) {
            throw new Error("No se pudo consultar el inventario.");
        }

        const productos = await respuestaProductos.json();

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
            throw new Error("No se pudo consultar los productos agotados.");
        }

        const productosAgotados = await respuestaAgotados.json();

        // Actualizamos los contadores del inventario.
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

        console.error("Error al cargar el inventario:", error);

        listaStockBajo.textContent =
            "No se pudo cargar el inventario.";

        listaAgotados.textContent =
            "No se pudo cargar el inventario.";
    }
}


// Muestra una lista de productos dentro del inventario.
function mostrarListaInventario(productos, contenedor, mensajeVacio) {

    // Limpiamos el contenido anterior.
    contenedor.innerHTML = "";

    // Si no hay productos, mostramos un mensaje.
    if (productos.length === 0) {
        contenedor.textContent = mensajeVacio;
        return;
    }

    // Recorremos los productos.
    productos.forEach(producto => {

        const elemento = document.createElement("p");

        elemento.textContent =
            producto.nombre +
            " - Stock disponible: " +
            producto.stock;

        contenedor.appendChild(elemento);
    });
}


/* ============================================================
   7. MOSTRAR LOS PRODUCTOS EN EL CATÁLOGO
   ============================================================ */

// Esta función crea las tarjetas de los productos.
function mostrarProductos(productos) {

    // Comprobamos que exista el catálogo en el HTML.
    if (!listaProductos) {
        return;
    }

    // Limpiamos las tarjetas anteriores.
    listaProductos.innerHTML = "";

    // Si no hay resultados, mostramos un mensaje.
    if (productos.length === 0) {

        listaProductos.textContent =
            "No se encontraron productos que coincidan con tu búsqueda.";

        return;
    }

    // Recorremos los productos que debemos mostrar.
    productos.forEach(producto => {

        // Creamos la tarjeta.
        const tarjeta = document.createElement("article");
        tarjeta.className = "card";

        // Creamos la imagen.
        const imagen = document.createElement("img");

        if (producto.imagen && producto.imagen.trim() !== "") {
            imagen.src = producto.imagen;
        } else {
            imagen.src =
                "https://images.unsplash.com/photo-1574258495973-f010dfbb5371?auto=format&fit=crop&w=800&q=80";
        }

        imagen.alt = "Imagen de " + (producto.nombre || "producto");

        // Si la imagen no carga, la ocultamos.
        imagen.onerror = function() {
            this.style.display = "none";
        };

        // Creamos el nombre.
        const nombre = document.createElement("h3");
        nombre.textContent = producto.nombre || "Producto sin nombre";

        // Creamos la descripción.
        const descripcion = document.createElement("p");
        descripcion.textContent =
            producto.descripcion ||
            "Producto disponible en nuestra óptica.";

        // Creamos la categoría.
        const categoria = document.createElement("p");
        categoria.textContent =
            "Categoría: " + (producto.categoria || "General");

        // Creamos el precio.
        const precio = document.createElement("p");
        precio.textContent =
            "Precio: $" + Number(producto.precio ?? 0).toFixed(2);
        precio.className = "precio-producto";

        // Creamos el stock.
        const stock = document.createElement("p");
        stock.textContent =
            "Disponibles: " + (producto.stock ?? 0);

        // Creamos el botón para editar.
        const botonEditar = document.createElement("button");
        botonEditar.textContent = "Editar";
        botonEditar.type = "button";
        botonEditar.className = "btn-editar";

        botonEditar.addEventListener("click", function() {
            editarProducto(producto);
        });

        // Creamos el botón para eliminar.
        const botonEliminar = document.createElement("button");
        botonEliminar.textContent = "Eliminar";
        botonEliminar.type = "button";
        botonEliminar.className = "btn-eliminar";

        botonEliminar.addEventListener("click", function() {
            eliminarProducto(producto.id);
        });

        // Agregamos todos los elementos a la tarjeta.
        tarjeta.append(
            imagen,
            nombre,
            descripcion,
            categoria,
            precio,
            stock,
            botonEditar,
            botonEliminar
        );

        // Agregamos la tarjeta al catálogo.
        listaProductos.appendChild(tarjeta);
    });
}


/* ============================================================
   8. CARGAR LOS PRODUCTOS DESDE EL SERVIDOR
   ============================================================ */

// Consulta los productos registrados en la base de datos.
async function cargarProductos() {

    // Si no existe el catálogo, no ejecutamos la función.
    if (!listaProductos) {
        return;
    }

    try {

        // Solicitamos los productos al backend.
        const respuesta = await fetch(API_PRODUCTOS);

        if (!respuesta.ok) {
            throw new Error("No se pudieron cargar los productos.");
        }

        // Convertimos la respuesta a JSON.
        const productos = await respuesta.json();

        // Guardamos todos los productos para las búsquedas.
        productosCargados = productos;

        // Mostramos todos los productos inicialmente.
        mostrarProductos(productosCargados);

    } catch (error) {

        console.error("Error al cargar productos:", error);

        listaProductos.textContent =
            "No se pudieron cargar los productos.";
    }
}


/* ============================================================
   9. EDITAR PRODUCTOS
   ============================================================ */

// Coloca los datos del producto seleccionado en el formulario.
function editarProducto(producto) {

    // Comprobamos que el formulario exista.
    if (!formulario) {
        return;
    }

    // Guardamos el ID del producto que vamos a editar.
    productoEditando = producto.id;

    // Conservamos la fotografía actual.
    imagenActual = producto.imagen || "";

    // Colocamos los datos en los campos del formulario.
    document.getElementById("nombre").value =
        producto.nombre || "";

    document.getElementById("descripcion").value =
        producto.descripcion || "";

    document.getElementById("categoria").value =
        producto.categoria || "";

    document.getElementById("precio").value =
        producto.precio ?? "";

    document.getElementById("stock").value =
        producto.stock ?? "";

    // Limpiamos el selector de fotografías.
    if (inputImagen) {
        inputImagen.value = "";
    }

    // Liberamos una vista previa temporal anterior.
    if (direccionVistaPrevia) {
        URL.revokeObjectURL(direccionVistaPrevia);
        direccionVistaPrevia = null;
    }

    // Limpiamos el espacio de vista previa.
    if (vistaPreviaImagen) {

        vistaPreviaImagen.innerHTML = "";

        // Si el producto tiene fotografía, la mostramos.
        if (imagenActual) {

            const imagen = document.createElement("img");

            imagen.src = imagenActual;
            imagen.alt = "Fotografía actual de " + producto.nombre;

            imagen.style.width = "200px";
            imagen.style.height = "150px";
            imagen.style.objectFit = "contain";
            imagen.style.marginTop = "10px";
            imagen.style.borderRadius = "10px";

            imagen.onerror = function() {
                vistaPreviaImagen.innerHTML =
                    "<p>No se pudo cargar la fotografía actual.</p>";
            };

            vistaPreviaImagen.appendChild(imagen);

        } else {

            vistaPreviaImagen.innerHTML =
                "<p>Este producto todavía no tiene fotografía.</p>";
        }
    }

    // Cambiamos el texto del botón principal.
    if (botonGuardar) {
        botonGuardar.textContent = "Actualizar producto";
    }

    // Mostramos el botón para cancelar.
    if (botonCancelar) {
        botonCancelar.style.display = "inline-block";
    }

    // Mostramos un mensaje informativo.
    if (mensaje) {
        mensaje.textContent = "Editando: " + producto.nombre;
        mensaje.style.color = "#1b5e9a";
    }

    // Desplazamos la página hasta el formulario.
    formulario.scrollIntoView({
        behavior: "smooth"
    });
}


/* ============================================================
   10. CANCELAR LA EDICIÓN
   ============================================================ */

// Detectamos cuando se presiona el botón de cancelar.
if (botonCancelar) {

    botonCancelar.addEventListener("click", function() {

        // Limpiamos el formulario.
        formulario.reset();

        // Terminamos el modo de edición.
        productoEditando = null;

        // Borramos la fotografía anterior.
        imagenActual = "";

        // Liberamos la dirección temporal de la vista previa.
        if (direccionVistaPrevia) {
            URL.revokeObjectURL(direccionVistaPrevia);
            direccionVistaPrevia = null;
        }

        // Limpiamos la vista previa.
        if (vistaPreviaImagen) {
            vistaPreviaImagen.innerHTML =
                "<p>Selecciona una fotografía para visualizarla.</p>";
        }

        // Restauramos el texto del botón principal.
        if (botonGuardar) {
            botonGuardar.textContent = "Guardar producto";
        }

        // Ocultamos el botón de cancelar.
        botonCancelar.style.display = "none";

        // Mostramos un mensaje.
        if (mensaje) {
            mensaje.textContent = "Edición cancelada.";
            mensaje.style.color = "gray";
        }
    });
}


/* ============================================================
   11. REGISTRAR Y ACTUALIZAR PRODUCTOS
   ============================================================ */

// Escuchamos el envío del formulario.
if (formulario) {

    formulario.addEventListener("submit", async function(evento) {

        // Evitamos que el navegador recargue la página.
        evento.preventDefault();

        // Recopilamos los datos del formulario.
        const producto = {

            nombre: document.getElementById("nombre").value,

            descripcion:
                document.getElementById("descripcion").value,

            categoria:
                document.getElementById("categoria").value,

            precio:
                Number(document.getElementById("precio").value),

            stock:
                Number(document.getElementById("stock").value),

            imagen: imagenActual
        };

        try {

            // Comprobamos si estamos editando un producto.
            const esEdicion = productoEditando !== null;

            // Enviamos los datos al servidor.
            const respuesta = await fetch(
                esEdicion
                    ? `${API_PRODUCTOS}/${productoEditando}`
                    : API_PRODUCTOS,
                {
                    method: esEdicion ? "PUT" : "POST",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify(producto)
                }
            );

            // Comprobamos que el servidor haya guardado los datos.
            if (!respuesta.ok) {
                throw new Error("No se pudo guardar el producto.");
            }

            // Obtenemos el producto guardado.
            const productoGuardado = await respuesta.json();

            // Si se seleccionó una fotografía nueva, la subimos.
            if (inputImagen && inputImagen.files.length > 0) {

                await subirImagenProducto(productoGuardado.id);
            }

            // Mostramos el mensaje correspondiente.
            if (mensaje) {

                mensaje.textContent = esEdicion
                    ? "¡Producto actualizado correctamente!"
                    : "¡Producto guardado correctamente!";

                mensaje.style.color = "green";
            }

            // Limpiamos el formulario.
            formulario.reset();

            // Borramos la fotografía anterior.
            imagenActual = "";

            // Liberamos la vista previa temporal.
            if (direccionVistaPrevia) {
                URL.revokeObjectURL(direccionVistaPrevia);
                direccionVistaPrevia = null;
            }

            // Limpiamos el contenedor de vista previa.
            if (vistaPreviaImagen) {
                vistaPreviaImagen.innerHTML =
                    "<p>Selecciona una fotografía para visualizarla.</p>";
            }

            // Terminamos el modo de edición.
            productoEditando = null;

            // Restauramos el botón principal.
            if (botonGuardar) {
                botonGuardar.textContent = "Guardar producto";
            }

            // Ocultamos el botón cancelar.
            if (botonCancelar) {
                botonCancelar.style.display = "none";
            }

            // Actualizamos el catálogo.
            await cargarProductos();

            // Actualizamos el inventario.
            await cargarInventario();

        } catch (error) {

            console.error("Error al guardar:", error);

            if (mensaje) {
                mensaje.textContent =
                    "Error al guardar o actualizar el producto.";

                mensaje.style.color = "red";
            }
        }
    });
}


/* ============================================================
   12. ELIMINAR PRODUCTOS
   ============================================================ */

// Elimina un producto utilizando su identificador.
async function eliminarProducto(id) {

    // Solicitamos confirmación antes de eliminar.
    const confirmar = confirm(
        "¿Estás seguro de que deseas eliminar este producto?"
    );

    // Si el usuario cancela, detenemos la función.
    if (!confirmar) {
        return;
    }

    try {

        // Enviamos la solicitud DELETE al servidor.
        const respuesta = await fetch(
            `${API_PRODUCTOS}/${id}`,
            {
                method: "DELETE"
            }
        );

        // Comprobamos si se eliminó correctamente.
        if (!respuesta.ok) {
            throw new Error("No se pudo eliminar el producto.");
        }

        // Mostramos un mensaje de éxito.
        if (mensaje) {
            mensaje.textContent =
                "¡Producto eliminado correctamente!";

            mensaje.style.color = "green";
        }

        // Actualizamos el catálogo.
        await cargarProductos();

        // Actualizamos el inventario.
        await cargarInventario();

    } catch (error) {

        console.error("Error al eliminar:", error);

        if (mensaje) {
            mensaje.textContent =
                "Error al eliminar el producto.";

            mensaje.style.color = "red";
        }
    }
}


/* ============================================================
   13. ABRIR UN PRODUCTO PARA EDITAR DESDE EL INVENTARIO
   ============================================================ */

// Permite abrir automáticamente un producto seleccionado.
async function cargarProductoParaEditar() {

    // Obtenemos los parámetros de la dirección de la página.
    const parametros = new URLSearchParams(window.location.search);

    // Recuperamos el ID enviado desde el inventario.
    const idProducto = parametros.get("editar");

    // Si no hay ID, no hacemos nada.
    if (!idProducto) {
        return;
    }

    try {

        // Consultamos el producto seleccionado.
        const respuesta = await fetch(
            `${API_PRODUCTOS}/${idProducto}`
        );

        if (!respuesta.ok) {
            throw new Error("No se pudo encontrar el producto.");
        }

        // Convertimos la respuesta a JSON.
        const producto = await respuesta.json();

        // Abrimos el formulario con sus datos.
        editarProducto(producto);

    } catch (error) {

        console.error(
            "Error al cargar el producto para editar:",
            error
        );

        alert("No se pudo abrir el producto seleccionado.");
    }
}


/* ============================================================
   14. BUSCADOR DE PRODUCTOS
   ============================================================ */

/*
   Esta función busca productos por:
   - Nombre.
   - Descripción.
   - Categoría.

   También permite buscar sin importar:
   - Mayúsculas.
   - Minúsculas.
   - Tildes.
*/

// Función para eliminar tildes y convertir texto a minúsculas.
function normalizarTexto(texto) {

    return String(texto || "")
        .toLowerCase()
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "");
}


// Función principal de búsqueda.
function buscarProductos() {

    // Obtenemos el texto escrito en el buscador.
    const textoBusqueda = normalizarTexto(
        buscadorProductos.value.trim()
    );

    // Si el buscador está vacío, mostramos todos los productos.
    if (textoBusqueda === "") {

        mostrarProductos(productosCargados);

        return;
    }

    // Filtramos los productos que coinciden con la búsqueda.
    const resultados = productosCargados.filter(producto => {

        // Normalizamos el nombre.
        const nombre = normalizarTexto(producto.nombre);

        // Normalizamos la descripción.
        const descripcion = normalizarTexto(producto.descripcion);

        // Normalizamos la categoría.
        const categoria = normalizarTexto(producto.categoria);

        // Comprobamos si existe una coincidencia.
        return (
            nombre.includes(textoBusqueda) ||
            descripcion.includes(textoBusqueda) ||
            categoria.includes(textoBusqueda)
        );
    });

    // Mostramos únicamente los productos encontrados.
    mostrarProductos(resultados);
}


// Comprobamos que los elementos del buscador existan.
if (buscadorProductos && botonBuscarProductos) {

    // Ejecutamos la búsqueda al presionar el botón.
    botonBuscarProductos.addEventListener(
        "click",
        buscarProductos
    );

    // También permitimos buscar presionando Enter.
    buscadorProductos.addEventListener(
        "keydown",
        function(evento) {

            if (evento.key === "Enter") {

                evento.preventDefault();

                buscarProductos();
            }
        }
    );
}

// Detectamos cuando el usuario escribe o borra texto del buscador.
if (buscadorProductos) {

    buscadorProductos.addEventListener("input", function() {

        // Si el buscador queda vacío, mostramos nuevamente todos los productos.
        if (buscadorProductos.value.trim() === "") {

            mostrarProductos(productosCargados);

        }

    });

}

/* ============================================================
   15. CARGAR LAS FUNCIONES AL ABRIR LA PÁGINA
   ============================================================ */

// Cargamos los productos al abrir la página.
cargarProductos();

// Cargamos el panel de inventario.
cargarInventario();

// Revisamos si se seleccionó un producto para editar.
cargarProductoParaEditar();


/* ============================================================
   FIN DEL ARCHIVO PRODUCTOS.JS
   SINERGI OPTICS INTERNATIONAL
   ============================================================ */