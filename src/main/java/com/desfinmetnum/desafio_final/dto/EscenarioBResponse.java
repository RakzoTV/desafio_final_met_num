package com.desfinmetnum.desafio_final.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Respuesta de la simulación del vaciado de reservas (Escenario B).
 * Contiene los resultados de los tres métodos numéricos (Euler, Heun, RK4).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EscenarioBResponse {

    // Resultado del método de Euler
    private ResultadoMetodoODE euler;

    // Resultado del método de Heun
    private ResultadoMetodoODE heun;

    // Resultado del método de Runge-Kutta de cuarto orden
    private ResultadoMetodoODE rk4;

    /**
     * Contiene la serie temporal y el día crítico para UN método numérico.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResultadoMetodoODE {

        // Nombre del método utilizado (Euler, Heun, RK4)
        private String metodo;

        // Lista de puntos (t, R(t)) calculados por el método
        private List<PuntoTemporal> puntos;

        // Día en que la reserva cae por debajo del nivel crítico (null si no ocurre)
        private Double diaCritico;

        // Valor final de la reserva al terminar la simulación
        private double reservaFinal;
    }

    /**
     * Un punto en la serie temporal: tiempo t y valor de la reserva R(t).
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PuntoTemporal {
        private double t;      // Tiempo (días)
        private double valor;  // Reserva R(t) en ese momento
    }
}
