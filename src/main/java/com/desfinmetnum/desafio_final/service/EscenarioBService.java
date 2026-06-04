package com.desfinmetnum.desafio_final.service;

import com.desfinmetnum.desafio_final.dto.EscenarioBRequest;
import com.desfinmetnum.desafio_final.dto.EscenarioBResponse;
import com.desfinmetnum.desafio_final.dto.EscenarioBResponse.PuntoTemporal;
import com.desfinmetnum.desafio_final.dto.EscenarioBResponse.ResultadoMetodoODE;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio que implementa los tres métodos numéricos para resolver la EDO
 * del vaciado de reservas de carburantes.
 *
 * Modelo: R'(t) = entrada - consumo(t)
 * Donde:  consumo(t) = consumoInicial * (1 + tasaCrecimiento * t)
 *
 * A medida que pasan los días, el consumo aumenta (por pánico, bloqueos, etc.)
 * y la reserva R(t) va disminuyendo progresivamente.
 */
@Service
public class EscenarioBService {

    /**
     * Ejecuta la simulación con los tres métodos y devuelve resultados comparativos.
     * Esto permite al estudiante comparar la precisión de Euler, Heun y RK4.
     */
    public EscenarioBResponse simular(EscenarioBRequest req) {
        // Ejecutar cada método numérico de forma independiente
        ResultadoMetodoODE resultadoEuler = resolverEuler(req);
        ResultadoMetodoODE resultadoHeun  = resolverHeun(req);
        ResultadoMetodoODE resultadoRK4   = resolverRK4(req);

        // Empaquetar los tres resultados en la respuesta
        return EscenarioBResponse.builder()
                .euler(resultadoEuler)
                .heun(resultadoHeun)
                .rk4(resultadoRK4)
                .build();
    }

    // ==================== FUNCIÓN DE LA EDO: f(t, R) ====================

    /**
     * Función de la EDO: f(t, R) = entrada - consumo(t)
     *
     * consumo(t) = consumoInicial * (1 + tasaCrecimiento * t)
     * → Simula que cada día el consumo sube proporcionalmente (panic buying, crisis).
     *
     * @param t   tiempo actual en días
     * @param R   reserva actual (no entra directamente en este modelo lineal en t,
     *            pero se incluye por generalidad para posibles modelos futuros)
     * @param req parámetros del problema
     * @return    derivada dR/dt = tasa de cambio de la reserva
     */
    private double f(double t, double R, EscenarioBRequest req) {
        // El consumo crece linealmente con el tiempo
        double consumo = req.getConsumoInicial() * (1.0 + req.getTasaCrecimientoConsumo() * t);
        // La reserva aumenta con lo que entra y disminuye con lo que se consume
        return req.getEntradaDiaria() - consumo;
    }

    // ==================== MÉTODO DE EULER ====================

    /**
     * Método de Euler (primer orden):
     *   R_{n+1} = R_n + h * f(t_n, R_n)
     *
     * Es el método más simple: usa solo la pendiente al inicio del intervalo.
     * Error local: O(h²)  |  Error global: O(h)
     * → Menos preciso, puede acumular errores en problemas largos.
     */
    private ResultadoMetodoODE resolverEuler(EscenarioBRequest req) {
        double h = req.getPasoTiempo();        // Tamaño del paso de tiempo
        double t = 0.0;                         // Tiempo inicial t₀ = 0
        double R = req.getReservaInicial();     // Condición inicial R(0)
        Double diaCritico = null;               // Primer día con R < nivelCritico

        List<PuntoTemporal> puntos = new ArrayList<>();
        puntos.add(new PuntoTemporal(t, R));    // Agregar el punto inicial

        // Iterar hasta cubrir el número de días solicitado
        while (t < req.getDias()) {
            // Fórmula de Euler: R_{n+1} = R_n + h * f(t_n, R_n)
            R = R + h * f(t, R, req);
            t = t + h;

            // Redondear t para evitar errores de punto flotante acumulados
            t = Math.round(t * 1000.0) / 1000.0;

            puntos.add(new PuntoTemporal(t, R));

            // Detectar el primer cruce del nivel crítico
            if (diaCritico == null && R <= req.getNivelCritico()) {
                diaCritico = t;
            }
        }

        return ResultadoMetodoODE.builder()
                .metodo("Euler")
                .puntos(puntos)
                .diaCritico(diaCritico)
                .reservaFinal(R)
                .build();
    }

    // ==================== MÉTODO DE HEUN ====================

    /**
     * Método de Heun (Euler mejorado, segundo orden):
     *   k1 = f(t_n, R_n)                          ← pendiente al inicio
     *   k2 = f(t_n + h, R_n + h*k1)               ← pendiente al final (predictor)
     *   R_{n+1} = R_n + (h/2) * (k1 + k2)         ← corrector: promedio de pendientes
     *
     * Error local: O(h³)  |  Error global: O(h²)
     * → Más preciso que Euler con el mismo costo computacional casi.
     */
    private ResultadoMetodoODE resolverHeun(EscenarioBRequest req) {
        double h = req.getPasoTiempo();
        double t = 0.0;
        double R = req.getReservaInicial();
        Double diaCritico = null;

        List<PuntoTemporal> puntos = new ArrayList<>();
        puntos.add(new PuntoTemporal(t, R));

        while (t < req.getDias()) {
            // Paso 1 — Pendiente al inicio del intervalo (predictor de Euler)
            double k1 = f(t, R, req);

            // Paso 2 — Pendiente al final del intervalo usando la predicción de Euler
            double k2 = f(t + h, R + h * k1, req);

            // Corrector: promediar ambas pendientes para una mejor aproximación
            R = R + (h / 2.0) * (k1 + k2);
            t = t + h;
            t = Math.round(t * 1000.0) / 1000.0;

            puntos.add(new PuntoTemporal(t, R));

            if (diaCritico == null && R <= req.getNivelCritico()) {
                diaCritico = t;
            }
        }

        return ResultadoMetodoODE.builder()
                .metodo("Heun")
                .puntos(puntos)
                .diaCritico(diaCritico)
                .reservaFinal(R)
                .build();
    }

    // ==================== MÉTODO DE RUNGE-KUTTA 4 (RK4) ====================

    /**
     * Runge-Kutta de cuarto orden (RK4):
     *   k1 = f(t_n,       R_n)
     *   k2 = f(t_n + h/2, R_n + h/2 * k1)
     *   k3 = f(t_n + h/2, R_n + h/2 * k2)
     *   k4 = f(t_n + h,   R_n + h   * k3)
     *   R_{n+1} = R_n + (h/6) * (k1 + 2·k2 + 2·k3 + k4)
     *
     * Error local: O(h⁵)  |  Error global: O(h⁴)
     * → Método estándar de facto. Excelente precisión con pasos razonables.
     */
    private ResultadoMetodoODE resolverRK4(EscenarioBRequest req) {
        double h = req.getPasoTiempo();
        double t = 0.0;
        double R = req.getReservaInicial();
        Double diaCritico = null;

        List<PuntoTemporal> puntos = new ArrayList<>();
        puntos.add(new PuntoTemporal(t, R));

        while (t < req.getDias()) {
            // k1: pendiente al inicio del intervalo
            double k1 = f(t, R, req);

            // k2: pendiente en el punto medio usando k1 para extrapolar
            double k2 = f(t + h / 2.0, R + (h / 2.0) * k1, req);

            // k3: pendiente en el punto medio usando k2 (corrección del medio)
            double k3 = f(t + h / 2.0, R + (h / 2.0) * k2, req);

            // k4: pendiente al final del intervalo usando k3
            double k4 = f(t + h, R + h * k3, req);

            // Combinar con pesos 1:2:2:1 (promedio ponderado de Simpson)
            R = R + (h / 6.0) * (k1 + 2.0 * k2 + 2.0 * k3 + k4);
            t = t + h;
            t = Math.round(t * 1000.0) / 1000.0;

            puntos.add(new PuntoTemporal(t, R));

            if (diaCritico == null && R <= req.getNivelCritico()) {
                diaCritico = t;
            }
        }

        return ResultadoMetodoODE.builder()
                .metodo("RK4")
                .puntos(puntos)
                .diaCritico(diaCritico)
                .reservaFinal(R)
                .build();
    }
}