
package com.sinergi.optics.controller;

/*
 * ============================================================
 * PROYECTO: SINERGI OPTICS INTERNATIONAL
 * ARCHIVO: ProductoController.java
 * ============================================================
 *
 * DESCRIPCIÓN:
 *
 * Esta clase es el controlador encargado de recibir las
 * solicitudes que llegan desde nuestra página web.
 *
 * Se comunica con ProductoService para realizar operaciones
 * sobre los productos almacenados en nuestra base de datos.
 *
 * FUNCIONES PRINCIPALES:
 *
 * 1. Consultar todos los productos.
 * 2. Buscar un producto por su identificador.
 * 3. Registrar nuevos productos.
 * 4. Actualizar productos existentes.
 * 5. Eliminar productos.
 * 6. Recibir y almacenar fotografías desde la computadora.
 *
 * ============================================================
 */


/* ============================================================
   1. IMPORTACIONES DEL PROYECTO
   ============================================================ */

// Importamos la clase Producto.
// Representa la información de cada producto de nuestra óptica.
import com.sinergi.optics.model.Producto;

// Importamos ProductoService.
// Esta clase contiene las operaciones para administrar productos.
import com.sinergi.optics.service.ProductoService;


/* ============================================================
   2. IMPORTACIONES DE SPRING BOOT
   ============================================================ */

// Importamos las anotaciones de Spring para crear rutas,
// recibir parámetros y controlar las solicitudes HTTP.
import org.springframework.web.bind.annotation.*;

// MultipartFile permite recibir archivos enviados desde un formulario.
// En nuestro proyecto lo utilizaremos para recibir fotografías.
import org.springframework.web.multipart.MultipartFile;


/* ============================================================
   3. IMPORTACIONES PARA MANEJAR ARCHIVOS
   ============================================================ */

// IOException permite manejar posibles errores al leer o guardar archivos.
import java.io.IOException;

// Files permite crear carpetas, copiar archivos y realizar
// operaciones sobre el sistema de archivos.
import java.nio.file.Files;

// Path permite representar la ubicación de una carpeta o archivo.
import java.nio.file.Path;

// StandardCopyOption permite establecer cómo se copiará un archivo.
import java.nio.file.StandardCopyOption;

// UUID genera identificadores únicos.
// Lo utilizaremos para que cada fotografía tenga un nombre diferente.
import java.util.UUID;

// List permite trabajar con listas de productos.
import java.util.List;


/* ============================================================
   4. IMPORTACIONES PARA MANEJAR ERRORES HTTP
   ============================================================ */

// HttpStatus contiene los códigos de respuesta HTTP.
// Por ejemplo, BAD_REQUEST representa una solicitud incorrecta.
import org.springframework.http.HttpStatus;

// ResponseStatusException permite devolver errores HTTP
// cuando ocurre algún problema durante una operación.
import org.springframework.web.server.ResponseStatusException;


/* ============================================================
   5. CONFIGURACIÓN DEL CONTROLADOR
   ============================================================ */

// @RestController indica que esta clase recibe solicitudes
// HTTP y devuelve información, generalmente en formato JSON.
@RestController

// @RequestMapping establece la dirección principal de las rutas.
// Todas las funciones de esta clase comienzan con /api/productos.
@RequestMapping("/api/productos")

// @CrossOrigin permite solicitudes desde otros orígenes.
// En nuestro proyecto trabajamos con el frontend y el backend.
@CrossOrigin(origins = "*")

public class ProductoController {


    /* ========================================================
       6. CONEXIÓN CON EL SERVICIO DE PRODUCTOS
       ======================================================== */

    // Declaramos el servicio que contiene las operaciones
    // para consultar, registrar, actualizar y eliminar productos.
    private final ProductoService productoService;
    // ============================================================
// FUNCIÓN PARA ELIMINAR UNA FOTOGRAFÍA DEL SERVIDOR
// ============================================================

private void eliminarArchivoImagen(String direccionImagen) {

    // Comprobamos que exista una dirección de imagen.
    if (direccionImagen == null || direccionImagen.isBlank()) {
        return;
    }

    try {

        // Convertimos la dirección web en una ruta de archivo.
        String nombreArchivo = Path.of(direccionImagen)
                .getFileName()
                .toString();

        // Obtenemos la carpeta donde guardamos las fotografías.
        Path carpeta = Path.of("uploads").toAbsolutePath().normalize();

        // Construimos la ruta de la fotografía.
        Path rutaImagen = carpeta.resolve(nombreArchivo).normalize();

        // Comprobamos que el archivo pertenezca a uploads.
        if (!rutaImagen.startsWith(carpeta)) {
            throw new IllegalArgumentException(
                    "La ruta de la fotografía no es válida."
            );
        }

        // Si la fotografía existe, la eliminamos.
        Files.deleteIfExists(rutaImagen);

    } catch (IOException error) {

        // Informamos el problema en la consola del servidor.
        System.err.println(
                "No se pudo eliminar la fotografía: "
                + error.getMessage()
        );

    }
}

    // Constructor de la clase.
    //
    // Spring Boot utiliza este constructor para proporcionar
    // automáticamente el objeto ProductoService.
    //
    // De esta manera, el controlador puede utilizar sus funciones
    // sin tener que crear manualmente una instancia.
    public ProductoController(ProductoService productoService) {

        // Guardamos el servicio recibido en nuestra variable.
        this.productoService = productoService;
    }


    /* ========================================================
       7. CONSULTAR TODOS LOS PRODUCTOS
       ======================================================== */

    // @GetMapping indica que esta función responde a solicitudes GET.
    //
    // Dirección:
    // http://localhost:8080/api/productos
    //
    // Su objetivo es devolver todos los productos registrados.
    @GetMapping
    public List<Producto> listar() {

        // Llamamos al servicio para consultar los productos.
        //
        // El resultado es una lista de objetos Producto.
        // Spring Boot convierte automáticamente la lista a JSON.
        return productoService.listarProductos();
    }

    // ============================================================
// 7.1. CONSULTAR PRODUCTOS CON POCO STOCK
// ============================================================

@GetMapping("/poco-stock")
public List<Producto> listarProductosConPocoStock() {

    // Solicitamos al servicio los productos con pocas unidades.
    return productoService.listarProductosConPocoStock();
}


// ============================================================
// 7.2. CONSULTAR PRODUCTOS AGOTADOS
// ============================================================

@GetMapping("/agotados")
public List<Producto> listarProductosAgotados() {

    // Solicitamos al servicio los productos sin existencias.
    return productoService.listarProductosAgotados();
}


    /* ========================================================
       8. BUSCAR UN PRODUCTO POR SU IDENTIFICADOR
       ======================================================== */

    // @GetMapping("/{id}") permite buscar un producto específico.
    //
    // Ejemplo:
    // http://localhost:8080/api/productos/1
    //
    // @PathVariable obtiene el número que aparece en la dirección.
    @GetMapping("/{id}")
    public Producto buscar(@PathVariable Long id) {

        // Buscamos el producto utilizando su identificador.
        //
        // orElseThrow permite generar un error si no existe.
        return productoService.buscarProducto(id)
            .orElseThrow(() ->
                new RuntimeException("Producto no encontrado")
            );
    }


    /* ========================================================
       9. REGISTRAR UN PRODUCTO NUEVO
       ======================================================== */

    // @PostMapping indica que esta función recibe solicitudes POST.
    //
    // Se utiliza cuando queremos registrar un producto nuevo.
    //
    // @RequestBody recibe los datos enviados desde JavaScript
    // y los convierte en un objeto Producto.
    @PostMapping
    public Producto guardar(@RequestBody Producto producto) {

        // Enviamos el producto al servicio para guardarlo
        // en nuestra base de datos.
        return productoService.guardarProducto(producto);
    }


    /* ========================================================
       10. SUBIR UNA FOTOGRAFÍA DESDE LA COMPUTADORA
       ======================================================== */

    /*
     * Esta función permite seleccionar una fotografía desde
     * nuestra computadora y enviarla al servidor.
     *
     * La fotografía se almacenará dentro de la carpeta uploads.
     *
     * También guardaremos su dirección en el producto
     * correspondiente para que pueda aparecer en el catálogo.
     *
     * Ejemplo de dirección:
     *
     * POST /api/productos/1/imagen
     *
     * El número 1 representa el identificador del producto.
     */

    // @PostMapping establece la dirección que recibirá el archivo.
    // ============================================================
// MÉTODO PARA SUBIR O REEMPLAZAR LA FOTOGRAFÍA DE UN PRODUCTO
// ============================================================

// ============================================================
// MÉTODO PARA ACTUALIZAR UN PRODUCTO
// ============================================================

@PutMapping("/{id}")
public Producto actualizar(
        @PathVariable Long id,
        @RequestBody Producto producto
) {

    // Comprobamos que el producto exista en la base de datos.
    Producto productoExistente = productoService.buscarProducto(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Producto no encontrado"
            ));

    // Actualizamos el nombre del producto.
    productoExistente.setNombre(producto.getNombre());

    // Actualizamos la descripción.
    productoExistente.setDescripcion(producto.getDescripcion());

    // Actualizamos la categoría.
    productoExistente.setCategoria(producto.getCategoria());

    // Actualizamos el precio.
    productoExistente.setPrecio(producto.getPrecio());

    // Actualizamos la cantidad disponible.
    productoExistente.setStock(producto.getStock());

    // Conservamos la fotografía que ya tenía el producto.
    // La fotografía nueva se subirá mediante el método subirImagen().

    // Guardamos los cambios en la base de datos.
    return productoService.guardarProducto(productoExistente);
}

@PostMapping("/{id}/imagen")
public Producto subirImagen(

        // Recibimos el identificador del producto.
        @PathVariable Long id,

        // Recibimos la fotografía enviada desde el formulario.
        @RequestParam("archivo") MultipartFile archivo

) throws IOException {

    // Comprobamos que se haya seleccionado una fotografía.
    if (archivo.isEmpty()) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "No se seleccionó ninguna imagen"
        );
    }

    // Obtenemos el tipo de archivo seleccionado.
    String tipo = archivo.getContentType();

    // Permitimos únicamente imágenes JPEG, PNG y WEBP.
    if (tipo == null ||
        !(tipo.equals("image/jpeg") ||
          tipo.equals("image/png") ||
          tipo.equals("image/webp"))) {

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Formato de imagen no permitido"
        );
    }

    // Buscamos el producto en la base de datos.
    Producto producto = productoService.buscarProducto(id)
            .orElseThrow(() -> new RuntimeException(
                    "Producto no encontrado"
            ));

    // Guardamos la dirección de la fotografía anterior.
    String imagenAnterior = producto.getImagen();

    // Definimos la carpeta donde se guardarán las fotografías.
    Path carpeta = Path.of("uploads");

    // Creamos la carpeta si todavía no existe.
    Files.createDirectories(carpeta);

    // Determinamos la extensión según el formato de la imagen.
    String extension;

    if (tipo.equals("image/jpeg")) {
        extension = ".jpg";
    } else if (tipo.equals("image/png")) {
        extension = ".png";
    } else {
        extension = ".webp";
    }

    // Generamos un nombre único para la nueva fotografía.
    String nombreArchivo = UUID.randomUUID() + extension;

    // Construimos la ruta donde se guardará la imagen.
    Path ruta = carpeta.resolve(nombreArchivo);

    // Copiamos la fotografía a la carpeta uploads.
    Files.copy(
            archivo.getInputStream(),
            ruta,
            StandardCopyOption.REPLACE_EXISTING
    );

    // Actualizamos la dirección de la fotografía del producto.
    producto.setImagen("/uploads/" + nombreArchivo);

    // Guardamos los cambios en la base de datos.
    Producto productoGuardado =
            productoService.guardarProducto(producto);

    // Eliminamos la fotografía anterior si existía.
    if (imagenAnterior != null &&
        !imagenAnterior.equals(productoGuardado.getImagen())) {

        eliminarArchivoImagen(imagenAnterior);
    }

    // Devolvemos el producto actualizado.
    return productoGuardado;
}

    /* ========================================================
       12. ELIMINAR UN PRODUCTO
       ======================================================== */

    // @DeleteMapping indica que esta función recibe solicitudes DELETE.
    //
    // Ejemplo:
    // DELETE /api/productos/1
    //
    // Esta operación elimina el producto de la base de datos.
  // ============================================================
// MÉTODO PARA ELIMINAR UN PRODUCTO Y SU FOTOGRAFÍA
// ============================================================

@DeleteMapping("/{id}")
public String eliminar(@PathVariable Long id) {

    // Buscamos el producto antes de eliminarlo.
    Producto producto = productoService.buscarProducto(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Producto no encontrado"
            ));

    // Guardamos la dirección de su fotografía.
    String imagen = producto.getImagen();

    // Eliminamos el producto de la base de datos.
    productoService.eliminarProducto(id);

    // Eliminamos la fotografía asociada, si existe.
    eliminarArchivoImagen(imagen);

    // Devolvemos un mensaje de confirmación.
    return "Producto eliminado correctamente";
}

    /* ========================================================
       FIN DEL CONTROLADOR
       SINERGI OPTICS INTERNATIONAL
       ======================================================== */

}
