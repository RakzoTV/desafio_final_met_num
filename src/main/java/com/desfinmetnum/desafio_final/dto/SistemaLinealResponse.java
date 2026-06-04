package com.desfinmetnum.desafio_final.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Respuesta del sistema de ecuaciones lineales (Escenario A).
 * Contiene la solución x, historial de iteraciones y diagnóstico del sistema.
 */
@Data
@NoArgsConstructor
public class
SistemaLinealResponse {

    // Nombre del método usado (Jacobi, Gauss-Seidel, SOR, LU, Gradiente Conjugado)
    private String metodo;

    // Solución x del sistema Ax = b
    // Cada elemento xᵢ = cantidad enviada a la zona i
    private List<Double> solucion;

    // Solución original antes de perturbación (solo para Escenario F)
    private List<Double> solucionOriginal;

    // Solución perturbada (solo para Escenario F — análisis de rumores)
    private List<Double> solucionPerturbada;

    // Número de iteraciones realizadas (null para LU, que es directo)
    private Integer iteraciones;

    // true si el método convergió dentro de la tolerancia
    private boolean convergencia;

    // true si la matriz es diagonalmente dominante (garantía de convergencia)
    private boolean esDiagonalmenteDominante;

    // Advertencia si la matriz no es diagonalmente dominante
    private String advertencia;

    // Historial de iteraciones paso a paso (para mostrar en tabla)
    private List<PasoIteracion> historial;

    // Interpretación en lenguaje natural del resultado
    private String interpretacion;

    // Valor de omega usado (solo para SOR)
    private Double omega;

    // Matriz L de la descomposición LU (solo para método LU)
    private List<List<Double>> matrizL;

    // Matriz U de la descomposición LU (solo para método LU)
    private List<List<Double>> matrizU;

    /**
     * Una fila del historial de iteraciones.
     */
    @Data
    @NoArgsConstructor
    public static class PasoIteracion {
        // Número de iteración
        private int iteracion;

        // Vector solución x en esta iteración
        private List<Double> x;

        // Error (norma de la diferencia entre iteraciones consecutivas)
        private double error;

        // Norma del residuo ||b - Ax|| (solo para Gradiente Conjugado)
        private Double residuo;

        public PasoIteracion(int iteracion, List<Double> x, double error, Double residuo) {
            this.iteracion = iteracion;
            this.x = x;
            this.error = error;
            this.residuo = residuo;
        }
    }
}
