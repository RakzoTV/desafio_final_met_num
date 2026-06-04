package com.desfinmetnum.desafio_final.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Respuesta de la integración numérica del costo acumulado (Escenario D).
 * Contiene el gasto total calculado, la pérdida adquisitiva y el desglose por intervalos.
 */
@Data
@NoArgsConstructor
public class IntegracionResponse {

    // Nombre del método usado (Trapecio, Simpson 1/3, Simpson 3/8)
    private String metodo;

    // Nombre del producto analizado
    private String producto;

    // Costo total acumulado durante el mes (área bajo la curva de precios)
    private double costoAcumulado;

    // Costo que hubiera tenido si el precio se hubiera mantenido constante (sin inflación)
    private double costoSinInflacion;

    // Pérdida del poder adquisitivo = costoAcumulado - costoSinInflacion
    private double perdidaAdquisitiva;

    // Pérdida expresada como porcentaje del costo sin inflación
    private double porcentajePerdida;

    // Tabla de pasos de integración (aporte de cada intervalo)
    private List<PasoIntegracion> tablaPasos;

    // Interpretación en lenguaje natural del resultado económico
    private String interpretacion;

    /**
     * Aporte de UN intervalo al total de la integral.
     */
    @Data
    @NoArgsConstructor
    public static class PasoIntegracion {
        // Intervalo de días aplicado (ej: "[1, 5]")
        private String intervalo;

        // Área (costo) calculada en este intervalo
        private double aporte;

        public PasoIntegracion(String intervalo, double aporte) {
            this.intervalo = intervalo;
            this.aporte = aporte;
        }
    }
}
