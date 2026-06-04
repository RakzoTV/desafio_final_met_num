package com.desfinmetnum.desafio_final.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Respuesta del análisis de condicionamiento y rumores (Escenario F).
 * Contiene el número de condición κ, la clasificación del sistema
 * y los resultados de diferentes escenarios de rumor.
 */
@Data
@NoArgsConstructor
public class CondicionamientoResponse {

    // Nombre del método o análisis realizado
    private String metodo;

    // Nivel de rumor analizado (BAJO, MEDIO, ALTO, PANICO, PERSONALIZADO)
    private String nivelRumor;

    // Porcentaje de perturbación aplicado a la demanda
    private Double porcentajePerturbacion;

    // Solución del sistema original (sin rumor)
    private List<Double> solucion;

    // Solución original en análisis de perturbación
    private List<Double> solucionOriginal;

    // Solución con la demanda perturbada por el rumor
    private List<Double> solucionPerturbada;

    // Vector b original (demanda real)
    private List<Double> terminosOriginales;

    // Vector b perturbado (demanda artificial por rumor)
    private List<Double> terminosPerturbados;

    // Número de condición κ(A) = ||A|| · ||A⁻¹||
    // κ ≈ 1       → sistema muy estable
    // κ < 10      → estable
    // κ ∈ [10,100] → moderado
    // κ > 100     → mal condicionado
    // κ >> 1000   → casi singular
    private double numeroDCondicion;

    // Clasificación textual: ESTABLE, MODERADO, MAL CONDICIONADO, SINGULAR O CASI SINGULAR
    private String clasificacion;

    // Cambio relativo en los términos b (%)
    private Double cambioRelativoTerminos;

    // Cambio relativo en la solución x (%)
    private Double cambioRelativoSolucion;

    // true si el sistema se considera estable (κ < 100)
    private boolean estable;

    // Interpretación del análisis en lenguaje natural
    private String interpretacion;

    // Lista de escenarios de rumor comparados (solo para comparacionRumores)
    private List<EscenarioRumor> escenarios;

    /**
     * Resultado de UN escenario de rumor en la comparación múltiple.
     */
    @Data
    @NoArgsConstructor
    public static class EscenarioRumor {
        // Nivel del rumor: BAJO, MEDIO, ALTO, PANICO
        private String nivel;

        // Porcentaje de aumento en la demanda percibida
        private double porcentaje;

        // Solución del sistema con esta perturbación
        private List<Double> solucionPerturbada;

        // Cambio relativo en la solución (%) respecto a la solución original
        private double cambioRelativo;

        public EscenarioRumor(String nivel, double porcentaje,
                              List<Double> solucionPerturbada, double cambioRelativo) {
            this.nivel = nivel;
            this.porcentaje = porcentaje;
            this.solucionPerturbada = solucionPerturbada;
            this.cambioRelativo = cambioRelativo;
        }
    }
}
