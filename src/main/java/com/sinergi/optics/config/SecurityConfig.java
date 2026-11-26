package com.sinergi.optics.config;

// Importaciones necesarias para configurar Spring Security
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

// Indica que esta clase configura la seguridad del sistema
@Configuration
public class SecurityConfig {

    // Codificar las contraseñas mediante BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Definir los permisos de acceso del sistema
    @Bean
    public SecurityFilterChain seguridad(HttpSecurity http) throws Exception {

        // Configurar el manejo del token CSRF
        CookieCsrfTokenRepository repositorioCsrf =
                CookieCsrfTokenRepository.withHttpOnlyFalse();

        CsrfTokenRequestAttributeHandler controladorCsrf =
                new CsrfTokenRequestAttributeHandler();

        controladorCsrf.setCsrfRequestAttributeName("_csrf");

        http

            // Mantener la protección CSRF
            .csrf(csrf -> csrf
                .csrfTokenRepository(repositorioCsrf)
                .csrfTokenRequestHandler(controladorCsrf)
            )

            // Configurar los permisos de cada ruta
            .authorizeHttpRequests(auth -> auth

                // Permitir consultar el catálogo público
                .requestMatchers(HttpMethod.GET,
                        "/api/productos").permitAll()

                // Permitir registrar nuevos usuarios
                .requestMatchers(HttpMethod.POST,
                        "/api/auth/registro").permitAll()

                // Permitir obtener el token CSRF
                .requestMatchers(HttpMethod.GET,
                        "/api/auth/csrf").permitAll()

                // Consultar información del usuario autenticado
                .requestMatchers(HttpMethod.GET,
                        "/api/auth/me").authenticated()

                // Proteger estadísticas del inventario
                .requestMatchers(HttpMethod.GET,
                        "/api/productos/poco-stock",
                        "/api/productos/agotados")
                        .hasAnyRole("ADMIN", "INVENTARIO")

                // Permitir consultar productos individuales
                .requestMatchers(HttpMethod.GET,
                        "/api/productos/*").permitAll()

                // Solo ADMIN puede crear productos
                .requestMatchers(HttpMethod.POST,
                        "/api/productos").hasRole("ADMIN")

                // ADMIN e INVENTARIO pueden modificar el stock
                .requestMatchers(HttpMethod.PATCH,
                        "/api/productos/*/stock")
                        .hasAnyRole("ADMIN", "INVENTARIO")

                // Solo ADMIN puede modificar productos completos
                .requestMatchers(HttpMethod.PUT,
                        "/api/productos/**").hasRole("ADMIN")

                // Solo ADMIN puede subir imágenes
                .requestMatchers(HttpMethod.POST,
                        "/api/productos/*/imagen").hasRole("ADMIN")

                // Solo ADMIN puede eliminar productos
                .requestMatchers(HttpMethod.DELETE,
                        "/api/productos/**").hasRole("ADMIN")

                // Permitir archivos públicos
                .requestMatchers(
                        "/",
                        "/index.html",
                        "/login.html",
                        "/registro.html",
                        "/css/**",
                        "/js/**",
                        "/img/**",
                        "/uploads/**"
                ).permitAll()

                // Proteger la página de inventario
                .requestMatchers("/inventario.html")
                        .hasAnyRole("ADMIN", "INVENTARIO")

                // Las demás rutas requieren autenticación
                .anyRequest().authenticated()
            )

            // Configurar el inicio de sesión
            .formLogin(form -> form

                // Página personalizada de inicio de sesión
                .loginPage("/login.html")

                // Dirección que recibe el formulario
                .loginProcessingUrl("/login")

                // Nombres de los campos del formulario
                .usernameParameter("username")
                .passwordParameter("password")

                // Dirección después de iniciar sesión correctamente
                .defaultSuccessUrl("/", true)

                // Dirección cuando las credenciales son incorrectas
                .failureUrl("/login.html?error=true")

                // Permitir acceder al formulario
                .permitAll()
            )

            // Configurar el cierre de sesión
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }
}