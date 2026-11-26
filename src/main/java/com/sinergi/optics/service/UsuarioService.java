
package com.sinergi.optics.service;

// Importaciones necesarias para gestionar usuarios y seguridad
import com.sinergi.optics.model.Usuario;
import com.sinergi.optics.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Indica que esta clase contiene la lógica de los usuarios
@Service
public class UsuarioService implements UserDetailsService {

    // Repositorio para consultar y guardar usuarios
    @Autowired
    private UsuarioRepository usuarioRepository;

    // Herramienta para codificar las contraseñas
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Registrar un nuevo usuario en la base de datos
    @Transactional
    public Usuario registrarUsuario(String username, String nombre, String password) {

        // Comprobar que el nombre de usuario no esté registrado
        if (usuarioRepository.existsByUsername(username.trim())) {
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }

        // Crear un nuevo usuario
        Usuario usuario = new Usuario();

        // Guardar los datos personales
        usuario.setUsername(username.trim());
        usuario.setNombre(nombre.trim());

        // Codificar la contraseña antes de guardarla
        usuario.setPassword(passwordEncoder.encode(password));

        // Asignar siempre el rol USER al registrarse públicamente
        usuario.setRol("USER");

        // Guardar el usuario en la base de datos
        return usuarioRepository.save(usuario);
    }

    // Buscar un usuario para comprobar sus credenciales al iniciar sesión
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        // Buscar el usuario por su nombre de usuario
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado"
                ));

        // Convertir el usuario de la base de datos al formato de Spring Security
        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .roles(usuario.getRol())
                .build();
    }
}
