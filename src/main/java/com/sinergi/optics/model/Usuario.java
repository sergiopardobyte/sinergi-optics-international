
package com.sinergi.optics.model;

// Importaciones necesarias para trabajar con JPA
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;

// Indica que esta clase representa una tabla en la base de datos
@Entity
public class Usuario {

    // Identificador único de cada usuario
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre de usuario, no puede repetirse ni estar vacío
    @Column(unique = true, nullable = false)
    private String username;

    // Nombre completo de la persona
    @Column(nullable = false)
    private String nombre;

    // Contraseña del usuario (se guardará codificada)
    @Column(nullable = false)
    private String password;

    // Rol del usuario: ADMIN, INVENTARIO o USER
    @Column(nullable = false)
    private String rol;

    // Constructor vacío necesario para JPA
    public Usuario() {
    }

    // Obtener el identificador
    public Long getId() {
        return id;
    }

    // Obtener el nombre de usuario
    public String getUsername() {
        return username;
    }

    // Modificar el nombre de usuario
    public void setUsername(String username) {
        this.username = username;
    }

    // Obtener el nombre completo
    public String getNombre() {
        return nombre;
    }

    // Modificar el nombre completo
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // Obtener la contraseña
    public String getPassword() {
        return password;
    }

    // Modificar la contraseña
    public void setPassword(String password) {
        this.password = password;
    }

    // Obtener el rol del usuario
    public String getRol() {
        return rol;
    }

    // Modificar el rol del usuario
    public void setRol(String rol) {
        this.rol = rol;
    }
}
