package com.desfinmetnum.desafio_final.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Datos de entrada para la interpolación de precios (Escenario C).
 * El usuario proporciona datos dispersos (día, precio) y los días donde
 * quiere estimar el precio mediante interpolación.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EscenarioCRequest {

    // Nombre del producto a analizar (ej: "Papa", "Arroz", "Aceite")
    private String producto;

    // Lista de días conocidos (eje X) — ej: [1, 5, 10, 15, 20, 30]
    private List<Double> diasConocidos;

    // Lista de precios conocidos (eje Y) — ej: [8, 10, 13, 16, 19, 22] en Bs
    private List<Double> preciosConocidos;

    // Lista de días donde se quiere estimar el precio (sin dato real)
    // ej: [3, 7, 12, 18, 25]
    private List<Double> diasEvaluar;

    // Número de puntos para generar la curva suave (para graficar)
    // ej: 100 puntos equiespaciados entre el primer y último día conocido
    private int puntosCurva;
}
