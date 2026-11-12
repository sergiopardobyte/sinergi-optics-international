
package com.sinergi.optics.config;

/*
 * ============================================================
 * PROYECTO: SINERGI OPTICS INTERNATIONAL
 * ARCHIVO: WebConfig.java
 * ============================================================
 *
 * DESCRIPCIÓN:
 *
 * Esta clase configura cómo Spring Boot permite acceder
 * a los archivos almacenados en nuestra aplicación.
 *
 * En nuestro caso, necesitamos mostrar las fotografías
 * de los productos que se guardan en la carpeta uploads.
 *
 * ============================================================
 */

// Importamos la interfaz que permite configurar recursos web.
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
 * @Configuration indica que esta clase contiene
 * configuraciones que Spring Boot debe aplicar.
 */
@Configuration

/*
 * WebConfig implementa WebMvcConfigurer.
 *
 * Esta interfaz permite personalizar el funcionamiento
 * de las rutas y los recursos estáticos de nuestra página.
 */
public class WebConfig implements WebMvcConfigurer {

    /*
     * ========================================================
     * CONFIGURACIÓN PARA MOSTRAR LAS FOTOGRAFÍAS
     * ========================================================
     *
     * addResourceHandlers permite establecer rutas públicas
     * para acceder a archivos almacenados en el servidor.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        /*
         * addResourceHandler("/uploads/**")
         *
         * Indica que cuando el navegador solicite una dirección
         * que comience con /uploads/, Spring Boot debe buscar
         * el archivo correspondiente.
         *
         * addResourceLocations("file:uploads/")
         *
         * Indica que los archivos se encuentran en la carpeta
         * uploads del directorio desde donde se ejecuta la aplicación.
         */
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");

    }

}
