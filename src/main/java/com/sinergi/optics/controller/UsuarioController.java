
package com.sinergi.optics.controller;

// Importaciones necesarias para gestionar las solicitudes HTTP
import com.sinergi.optics.model.Usuario;
import com.sinergi.optics.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.web.csrf.CsrfToken;

// Permite recibir solicitudes desde la página web
@RestController
@RequestMapping("/api/auth")
public class UsuarioController {

    // Servicio encargado de gestionar los usuarios
    @Autowired
    private UsuarioService usuarioService;

    // Clase para recibir los datos del formulario de registro
    public static class RegistroRequest {

        private String username;
        private String nombre;
        private String password;

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
    }

    // Endpoint para registrar nuevos usuarios
    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(
            @RequestBody RegistroRequest datos) {

        try {

            // Validar que los campos no estén vacíos
            if (datos.getUsername() == null ||
                datos.getNombre() == null ||
                datos.getPassword() == null ||
                datos.getUsername().isBlank() ||
                datos.getNombre().isBlank() ||
                datos.getPassword().isBlank()) {

                return ResponseEntity.badRequest()
                        .body("Todos los campos son obligatorios");
            }

            // Validar la longitud mínima de la contraseña
            if (datos.getPassword().length() < 8) {
                return ResponseEntity.badRequest()
                        .body("La contraseña debe tener al menos 8 caracteres");
            }

            // Registrar el usuario mediante el servicio
            Usuario usuario = usuarioService.registrarUsuario(
                    datos.getUsername(),
                    datos.getNombre(),
                    datos.getPassword()
            );

            // Confirmar que el registro fue exitoso
            return ResponseEntity.ok("Cuenta creada correctamente");

        } catch (IllegalArgumentException e) {

            // Informar si el nombre de usuario ya existe
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    // Consultar los datos del usuario que inició sesión
    @GetMapping("/me")
    public ResponseEntity<?> obtenerUsuarioActual(Authentication authentication) {

        // Obtener el nombre y los roles del usuario autenticado
        Map<String, Object> datos = Map.of(
                "username", authentication.getName(),
                "roles", authentication.getAuthorities()
                        .stream()
                        .map(autoridad -> autoridad.getAuthority())
                        .collect(Collectors.toList())
        );

        // Devolver los datos de la sesión
        return ResponseEntity.ok(datos);
    }

        // Obtener el token de seguridad CSRF para los formularios
    @GetMapping("/csrf")
    public CsrfToken obtenerTokenCsrf(CsrfToken csrfToken) {

        // Devolver el token que utiliza Spring Security
        return csrfToken;
    }

}
