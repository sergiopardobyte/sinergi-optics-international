package com.sinergi.optics.repository;

// Importaciones para trabajar con JPA
import com.sinergi.optics.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Indica que esta interfaz trabaja con la base de datos
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Spring permite guardar, buscar, actualizar y eliminar productos
    // sin necesidad de escribir consultas SQL manualmente.

}