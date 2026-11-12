package com.sinergi.optics.service;

// Importaciones del proyecto
import com.sinergi.optics.model.Producto;
import com.sinergi.optics.repository.ProductoRepository;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

// Indica que esta clase contiene la lógica de los productos
@Service
public class ProductoService {

    // Conexión con el repositorio de productos
    private final ProductoRepository productoRepository;

    // Constructor para recibir el repositorio
    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    // Listar todos los productos
    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }
    // ============================================================
// CONTROL DE INVENTARIO Y EXISTENCIAS
// ============================================================

// Consultar productos con pocas unidades disponibles.
public List<Producto> listarProductosConPocoStock() {

    // Obtenemos todos los productos registrados.
    List<Producto> productos = productoRepository.findAll();

    // Filtramos los productos que tienen entre 1 y 5 unidades.
    return productos.stream()
            .filter(producto ->
                    producto.getStock() != null &&
                    producto.getStock() > 0 &&
                    producto.getStock() <= 5
            )
            .toList();
}

// Consultar productos que están agotados.
public List<Producto> listarProductosAgotados() {

    // Obtenemos todos los productos registrados.
    List<Producto> productos = productoRepository.findAll();

    // Seleccionamos los productos cuyo stock es cero.
    return productos.stream()
            .filter(producto ->
                    producto.getStock() != null &&
                    producto.getStock() == 0
            )
            .toList();
}

    // Buscar un producto por su identificador
    public Optional<Producto> buscarProducto(Long id) {
        return productoRepository.findById(id);
    }

    // Guardar un producto nuevo
    public Producto guardarProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    // Actualizar un producto existente
    public Producto actualizarProducto(Long id, Producto datos) {
        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        producto.setNombre(datos.getNombre());
        producto.setDescripcion(datos.getDescripcion());
        producto.setCategoria(datos.getCategoria());
        producto.setPrecio(datos.getPrecio());
        producto.setStock(datos.getStock());
        producto.setImagen(datos.getImagen());

        return productoRepository.save(producto);
    }

    // Eliminar un producto
    public void eliminarProducto(Long id) {
        productoRepository.deleteById(id);
    }
}
