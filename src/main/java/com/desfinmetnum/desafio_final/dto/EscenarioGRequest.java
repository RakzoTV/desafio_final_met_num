package com.desfinmetnum.desafio_final.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Datos de entrada para la simulación de difusión de opinión social (Escenario G).
 *
 * Modelo de 3 EDOs acopladas:
 *   N'(t) = -a·N·M + b·D     → Ciudadanos neutrales
 *   M'(t) =  a·N·M - c·M·D   → Manifestantes activos
 *   D'(t) =  k·M   - r·D     → Mediadores / actores de diálogo
 *
 * Parámetros del modelo:
 *   a = tasa de influencia / contagio del descontento social
 *   b = tasa de recuperación / retorno a la neutralidad gracias al diálogo
 *   c = efectividad del diálogo para reducir manifestantes
 *   k = reacción institucional (generación de mediadores ante protestas)
 *   r = tasa de desgaste de los mediadores con el tiempo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EscenarioGRequest {

    // ===== CONDICIONES INICIALES =====

    // N(0): Número inicial de ciudadanos neutrales
    private double n0;

    // M(0): Número inicial de manifestantes activos
    private double m0;

    // D(0): Número inicial de mediadores / actores de diálogo
    private double d0;

    // ===== PARÁMETROS DEL MODELO =====

    // a: Tasa de influencia del descontento (contagio social N → M)
    // Valores típicos: 0.0001 a 0.001
    private double paramA;

    // b: Tasa de retorno a la neutralidad (N recupera personas gracias a D)
    // Valores típicos: 0.01 a 0.1
    private double paramB;

    // c: Efectividad del diálogo para reducir manifestantes (M → N)
    // Valores típicos: 0.0001 a 0.01
    private double paramC;

    // k: Reacción institucional — cuántos mediadores se generan por manifestante
    // Valores típicos: 0.01 a 0.1
    private double paramK;

    // r: Tasa de desgaste de los mediadores
    // Valores típicos: 0.01 a 0.1
    private double paramR;

    // ===== CONFIGURACIÓN DE LA SIMULACIÓN =====

    // Tiempo final de la simulación (en días)
    private double tiempoFinal;

    // Tamaño del paso de tiempo h (en días)
    // Valores recomendados: 0.1 a 1.0
    private double pasoTiempo;
}
