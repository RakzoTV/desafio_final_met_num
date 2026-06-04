package com.desfinmetnum.desafio_final.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Datos de entrada para la simulación del vaciado de reservas (Escenario B).
 * El usuario envía estos parámetros desde el formulario del frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EscenarioBRequest {

    // Nivel inicial de la reserva de carburante (en litros, barriles, etc.)
    private double reservaInicial;

    // Cantidad de carburante que llega diariamente a la planta (entrada constante)
    private double entradaDiaria;

    // Consumo diario inicial de la planta
    private double consumoInicial;

    // Tasa de crecimiento del consumo (simula pánico, bloqueos, etc.)
    // Ejemplo: 0.05 = el consumo crece un 5% adicional cada día
    private double tasaCrecimientoConsumo;

    // Número total de días a simular
    private int dias;

    // Tamaño del paso de tiempo (h) para los métodos numéricos
    // Ejemplo: 0.5 = medio día, 1.0 = un día
    private double pasoTiempo;

    // Nivel crítico de reserva (debajo de este valor se considera emergencia)
    private double nivelCritico;
}
