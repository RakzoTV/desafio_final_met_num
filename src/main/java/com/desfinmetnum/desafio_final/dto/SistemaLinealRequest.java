package com.desfinmetnum.desafio_final.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Datos de entrada para resolver un sistema de ecuaciones lineales Ax = b (Escenario A).
 *
 * La matriz A y el vector b representan el problema de distribución:
 *   Cada fila = restricción de una planta o ruta
 *   Cada columna = zona de distribución (Norte, Centro, Sur, etc.)
 *   b = demanda o capacidad de cada zona
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SistemaLinealRequest {

    // Matriz A del sistema (lista de listas de doubles)
    // Ejemplo 3x3: [[10,2,1],[1,10,2],[2,1,10]]
    private List<List<Double>> matriz;

    // Vector b (términos independientes = demandas por zona)
    // Ejemplo: [14, 16, 14]
    private List<Double> terminos;

    // Tolerancia para el criterio de parada en métodos iterativos
    // Ejemplo: 0.0001
    private double tolerancia;

    // Número máximo de iteraciones (para métodos iterativos: Jacobi, Gauss-Seidel, SOR)
    private int maxIteraciones;

    // Parámetro omega ω ∈ (0, 2) — SOLO para el método SOR
    // null para los demás métodos
    private Double omega;
}
