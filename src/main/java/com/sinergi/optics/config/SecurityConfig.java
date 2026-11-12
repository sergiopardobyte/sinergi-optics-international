
package com.sinergi.optics.config;

// Importaciones de Spring Security
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// Indica que esta clase configura la seguridad
@Configuration
public class SecurityConfig {

    // Configuramos los usuarios del sistema
    @Bean
    public UserDetailsService usuarios() {

        // Usuario administrador
        UserDetails administrador = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("Admin12345"))
                .roles("ADMIN")
                .build();

        // Usuario normal
        UserDetails usuario = User.builder()
                .username("usuario")
                .password(passwordEncoder().encode("Usuario12345"))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(administrador, usuario);
    }

    // Codificamos las contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Definimos qué puede hacer cada usuario
    @Bean
    public SecurityFilterChain seguridad(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth

                // Todos pueden consultar los productos
                .requestMatchers(HttpMethod.GET, "/api/productos", "/api/productos/**").permitAll()

                // Solo el administrador puede modificar productos
                .requestMatchers("/api/productos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/productos").hasRole("ADMIN")

                // Todos pueden ver la página y sus recursos
                // Permitimos que cualquier visitante pueda ver la página y sus fotografías.
                .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/img/**", "/uploads/**").permitAll()

                // Las demás rutas requieren iniciar sesión
                .anyRequest().authenticated()
            )

            // Activamos el inicio de sesión
            // Activamos el inicio de sesión con la página predeterminada
            .formLogin(form -> form
            .defaultSuccessUrl("/", true)
            .permitAll()
)

            // Configuramos el cierre de sesión
            .logout(Customizer.withDefaults());

        return http.build();
    }
}
