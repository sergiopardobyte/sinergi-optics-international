package com.sinergi.optics.config;

import com.sinergi.optics.model.Usuario;
import com.sinergi.optics.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class UsuarioDataInitializer {

    @Bean
    public CommandLineRunner inicializarUsuarios(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // Buscar o crear administrador
            Usuario administrador = usuarioRepository
                    .findByUsername("admin")
                    .orElseGet(Usuario::new);

            administrador.setUsername("admin");
            administrador.setNombre("Administrador");
            administrador.setPassword(
                    passwordEncoder.encode("Admin12345")
            );
            administrador.setRol("ADMIN");

            usuarioRepository.save(administrador);

            // Buscar o crear usuario de inventario
            Usuario inventario = usuarioRepository
                    .findByUsername("inventario")
                    .orElseGet(Usuario::new);

            inventario.setUsername("inventario");
            inventario.setNombre("Personal de Inventario");
            inventario.setPassword(
                    passwordEncoder.encode("Inventario12345")
            );
            inventario.setRol("INVENTARIO");

            usuarioRepository.save(inventario);
        };
    }
}