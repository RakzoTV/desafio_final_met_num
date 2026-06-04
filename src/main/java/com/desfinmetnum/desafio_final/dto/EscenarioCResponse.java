package com.desfinmetnum.desafio_final.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Respuesta de la interpolación de precios (Escenario C).
 * Contiene resultados de Lagrange, Newton (diferencias divididas) y Splines Cúbicos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EscenarioCResponse {

    // Nombre del producto interpolado
    private String producto;

    // Resultado del método de Lagrange
    private ResultadoInterpolacion lagrange;

    // Resultado del método de Newton (diferencias divididas)
    private ResultadoInterpolacion newton;

    // Resultado del método de Splines Cúbicos Naturales
    private ResultadoInterpolacion splines;

    /**
     * Resultado de UN método de interpolación.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResultadoInterpolacion {

        // Nombre del método utilizado
        private String metodo;

        // Valores interpolados en los días solicitados por el usuario
        // Cada elemento: {x: día, y: precio estimado}
        private List<PuntoXY> valoresInterpolados;

        // Curva suave con muchos puntos para graficar la función interpolada
        private List<PuntoXY> curva;

        // Tabla de diferencias divididas (solo para Newton, null en otros métodos)
        private List<List<Double>> diferenciaDivididas;

        // Coeficientes del spline por tramo (solo para Splines, null en otros)
        private List<CoeficienteSpline> coeficientesSpline;
    }

    /**
     * Punto (x, y) usado para gráficos y tablas de resultados.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PuntoXY {
        private double x;  // Día
        private double y;  // Precio en ese día
    }

    /**
     * Coeficientes de un tramo del spline cúbico.
     * Polinomio por tramo: S_i(x) = a + b*(x-xi) + c*(x-xi)^2 + d*(x-xi)^3
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoeficienteSpline {
        private int tramo;   // Número del tramo (0, 1, 2, ...)
        private double xi;   // Punto inicial del tramo (x_i)
        private double a;    // Término constante a_i = y_i
        private double b;    // Coeficiente lineal b_i
        private double c;    // Coeficiente cuadrático c_i
        private double d;    // Coeficiente cúbico d_i
    }
}
