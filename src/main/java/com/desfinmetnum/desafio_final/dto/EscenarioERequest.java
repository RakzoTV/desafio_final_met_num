package com.desfinmetnum.desafio_final.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Datos de entrada para encontrar raíces de ecuaciones (Escenario E).
 *
 * El usuario selecciona un tipo de función y proporciona los parámetros
 * para los métodos de búsqueda de raíces (Bisección, Newton-Raphson, Secante).
 *
 * Tipos de función disponibles:
 *   POLINOMIAL:     f(x) = a·x³ + b·x² + c·x + d
 *   EXPONENCIAL:    f(x) = a·e^(b·x) + c·x + d
 *   LOGARITMICA:    f(x) = a·ln(b·x) + c·x + d
 *   TRIGONOMETRICA: f(x) = a·sin(b·x) + c·x + d
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EscenarioERequest {

    // Tipo de función: POLINOMIAL, EXPONENCIAL, LOGARITMICA, TRIGONOMETRICA
    private String tipoFuncion;

    // Coeficientes de la función: [a, b, c, d]
    // Su interpretación varía según el tipo de función
    private List<Double> coeficientes;

    // Extremo izquierdo del intervalo [a, b] para Bisección
    private double intervaloA;

    // Extremo derecho del intervalo [a, b] para Bisección
    private double intervaloB;

    // Valor inicial x0 para Newton-Raphson y como primer punto de Secante
    private double x0;

    // Segundo valor inicial x1 para el método de la Secante
    private double x1;

    // Tolerancia del error para el criterio de parada
    // Ejemplo: 0.000001 (1e-6)
    private double tolerancia;

    // Número máximo de iteraciones permitidas
    private int maxIteraciones;

    // Número de puntos para generar el gráfico de f(x)
    private int puntosGrafico;
}
