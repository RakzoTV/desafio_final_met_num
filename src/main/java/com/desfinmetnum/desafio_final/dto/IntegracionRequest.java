package com.desfinmetnum.desafio_final.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Datos de entrada para la integración numérica del costo acumulado (Escenario D).
 *
 * Representa los precios de un producto durante el mes.
 * La integración numérica calcula el área bajo la curva de precios,
 * que equivale al gasto total acumulado de una familia.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntegracionRequest {

    // Nombre del producto (ej: "Papa", "Arroz", "Aceite")
    private String producto;

    // Lista de días del mes con datos de precio
    // Ejemplo: [1, 5, 10, 15, 20, 30]
    private List<Double> dias;

    // Lista de precios en esos días (en Bs)
    // Ejemplo: [8, 10, 13, 16, 19, 22]
    private List<Double> precios;

    // Precio base inicial (sin inflación) para calcular la pérdida adquisitiva
    // Ejemplo: 8.0 (el precio del día 1)
    private double precioBase;
}
