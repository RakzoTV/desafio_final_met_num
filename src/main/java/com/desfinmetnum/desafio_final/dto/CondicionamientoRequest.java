package com.desfinmetnum.desafio_final.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Datos de entrada para el análisis de condicionamiento de sistemas (Escenario F).
 * Representa un sistema de distribución que puede verse afectado por rumores.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CondicionamientoRequest {

    // Matriz A del sistema de distribución
    // Un sistema bien condicionado responde proporcionalmente a los cambios
    // Un sistema mal condicionado amplifica pequeñas perturbaciones (rumores)
    private List<List<Double>> matriz;

    // Vector b (términos independientes = demanda actual por zona)
    private List<Double> terminos;
}
