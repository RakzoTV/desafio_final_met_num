package com.desfinmetnum.desafio_final.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Respuesta del cálculo de raíces de ecuaciones (Escenario E).
 * Contiene resultados de Bisección, Newton-Raphson y Secante,
 * más el gráfico de la función para visualización.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EscenarioEResponse {

    // Expresión matemática de la función (para mostrar en el frontend)
    private String expresionFuncion;

    // Resultado del método de Bisección
    private ResultadoRaiz biseccion;

    // Resultado del método de Newton-Raphson
    private ResultadoRaiz newtonRaphson;

    // Resultado del método de la Secante
    private ResultadoRaiz secante;

    // Puntos de la función para graficar f(x) vs x
    private List<PuntoGrafico> graficaFuncion;

    /**
     * Resultado de UN método de búsqueda de raíces.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResultadoRaiz {

        // Nombre del método utilizado
        private String metodo;

        // Raíz encontrada (valor aproximado de x donde f(x) ≈ 0)
        private Double raiz;

        // Valor de f(raíz) — debería ser muy cercano a 0
        private Double fRaiz;

        // Número de iteraciones realizadas hasta converger
        private int iteraciones;

        // true si el método convergió dentro de la tolerancia
        private boolean convergio;

        // Mensaje descriptivo del resultado o del error ocurrido
        private String mensaje;

        // Tabla de iteraciones paso a paso (para mostrar en la página)
        private List<IteracionRaiz> tablaIteraciones;

        // Orden de convergencia estimado empíricamente
        // p≈1: lineal (Bisección), p≈1.62: superlineal (Secante), p≈2: cuadrática (NR)
        private Double ordenConvergencia;
    }

    /**
     * Una fila de la tabla de iteraciones del método.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IteracionRaiz {
        private int n;        // Número de iteración
        private Double a;     // Extremo izquierdo del intervalo (solo Bisección)
        private Double b;     // Extremo derecho del intervalo (solo Bisección)
        private double xn;    // Aproximación actual de la raíz
        private double fxn;   // Valor de f(xn)
        private Double error; // Error absoluto en esta iteración
    }

    /**
     * Punto para graficar la función f(x) en el rango de interés.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PuntoGrafico {
        private double x;   // Valor de x
        private double y;   // Valor de f(x)
    }
}
