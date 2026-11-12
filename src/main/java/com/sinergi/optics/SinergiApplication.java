
package com.sinergi.optics;

// Importamos las herramientas de Spring Boot
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Identificamos esta clase como la principal del sistema
@SpringBootApplication
public class SinergiApplication {

    // Punto de inicio de nuestra aplicación
    public static void main(String[] args) {

        // Iniciamos el servidor web
        SpringApplication.run(SinergiApplication.class, args);
    }
}
