
package com.sinergi.optics.repository;

// Importaciones necesarias para trabajar con JPA
import com.sinergi.optics.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// Repositorio encargado de gestionar los usuarios en la base de datos
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Buscar un usuario por su nombre de usuario
    Optional<Usuario> findByUsername(String username);

    // Comprobar si ya existe un nombre de usuario registrado
    boolean existsByUsername(String username);
}
