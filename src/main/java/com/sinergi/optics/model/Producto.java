
package com.sinergi.optics.model;

// Importaciones necesarias para trabajar con JPA
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// Indica que esta clase representa una tabla en la base de datos
@Entity
public class Producto {

    // Identificador único de cada producto
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre del producto
    private String nombre;

    // Descripción del producto
    private String descripcion;

    // Categoría: montura, lentes o accesorios
    private String categoria;

    // Precio del producto
    private Double precio;

    // Cantidad disponible en inventario
    private Integer stock;

    // Dirección de la imagen del producto
    private String imagen;

    // Constructor vacío necesario para JPA
    public Producto() {
    }

    // Obtener el identificador
    public Long getId() {
        return id;
    }

    // Obtener el nombre
    public String getNombre() {
        return nombre;
    }

    // Modificar el nombre
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // Obtener la descripción
    public String getDescripcion() {
        return descripcion;
    }

    // Modificar la descripción
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    // Obtener la categoría
    public String getCategoria() {
        return categoria;
    }

    // Modificar la categoría
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    // Obtener el precio
    public Double getPrecio() {
        return precio;
    }

    // Modificar el precio
    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    // Obtener el stock
    public Integer getStock() {
        return stock;
    }

    // Modificar el stock
    public void setStock(Integer stock) {
        this.stock = stock;
    }

    // Obtener la imagen
    public String getImagen() {
        return imagen;
    }

    // Modificar la imagen
    public void setImagen(String imagen) {
        this.imagen = imagen;
    }
}
