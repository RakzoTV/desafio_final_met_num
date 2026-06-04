package com.desfinmetnum.desafio_final.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Respuesta de la simulación de dinámica social (Escenario G).
 * Contiene las series temporales N(t), M(t), D(t) calculadas
 * con los métodos de Heun y RK4.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EscenarioGResponse {

    // Resultado de la simulación usando el método de Heun
    private ResultadoMetodoSocial heun;

    // Resultado de la simulación usando el método de RK4
    private ResultadoMetodoSocial rk4;

    // Población total inicial N(0) + M(0) + D(0)
    // Si el modelo es cerrado, esta suma debería conservarse aproximadamente
    private double poblacionTotal;

    /**
     * Resultado de UN método numérico para el sistema de 3 EDOs.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResultadoMetodoSocial {

        // Nombre del método (Heun o RK4)
        private String metodo;

        // Serie temporal con las tres poblaciones en cada paso de tiempo
        private List<PuntoSocial> puntos;

        // Valor máximo de manifestantes M(t) alcanzado durante la simulación
        private double picoManifestantes;

        // Día en que se alcanza el pico de manifestantes
        private double diaPicoManifestantes;

        // true si el conflicto tiende a estabilizarse al final del período
        private boolean tieneEstabilidad;

        // Valores finales de cada población al terminar la simulación
        private double neutralesFinal;
        private double manifestantesFinal;
        private double mediadoresFinal;
    }

    /**
     * Un punto en la serie temporal con las tres poblaciones simultáneas.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PuntoSocial {
        private double t;              // Tiempo (días)
        private double neutrales;      // N(t) — ciudadanos neutrales
        private double manifestantes;  // M(t) — manifestantes activos
        private double mediadores;     // D(t) — mediadores / diálogo
    }
}
