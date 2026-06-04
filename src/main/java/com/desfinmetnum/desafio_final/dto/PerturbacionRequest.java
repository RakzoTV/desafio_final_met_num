package com.desfinmetnum.desafio_final.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Datos de entrada para el análisis de perturbación por rumores (Escenario F).
 * Permite simular el efecto de un rumor en la demanda percibida del sistema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerturbacionRequest {

    // Matriz A del sistema de distribución
    private List<List<Double>> matriz;

    // Vector b original (demanda sin rumor)
    private List<Double> terminosOriginales;

    // Nivel de rumor predefinido: BAJO (1%), MEDIO (5%), ALTO (15%), PANICO (30%)
    // Si se especifica, se ignora porcentajePerturbacion
    private String nivelRumor;

    // Porcentaje de perturbación manual (si nivelRumor es null)
    // Ejemplo: 10.0 = aumentar la demanda en un 10%
    private Double porcentajePerturbacion;
}
