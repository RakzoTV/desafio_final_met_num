package com.desfinmetnum.desafio_final.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración global de CORS.
 * Permite que el frontend (desde cualquier origen) llame a nuestra API REST.
 * Sin esto, el navegador bloquearía las peticiones del frontend al backend.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Permitir peticiones desde cualquier origen a todos los endpoints /api/**
        registry.addMapping("/api/**")
                .allowedOrigins("*")               // En producción, restringir a tu dominio
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
