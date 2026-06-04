package com.desfinmetnum.desafio_final.service;

import com.desfinmetnum.desafio_final.dto.EscenarioGRequest;
import com.desfinmetnum.desafio_final.dto.EscenarioGResponse;
import com.desfinmetnum.desafio_final.dto.EscenarioGResponse.PuntoSocial;
import com.desfinmetnum.desafio_final.dto.EscenarioGResponse.ResultadoMetodoSocial;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio que simula la dinámica de opinión y descontento social
 * mediante un sistema de 3 ecuaciones diferenciales ordinarias acopladas.
 *
 * Modelo (tipo SIR social):
 *   N'(t) = -a·N·M + b·D     → Ciudadanos neutrales
 *   M'(t) =  a·N·M - c·M·D   → Manifestantes activos
 *   D'(t) =  k·M   - r·D     → Mediadores / actores de diálogo
 *
 * Parámetros:
 *   a = tasa de contagio del descontento (N se convierte en M)
 *   b = tasa de retorno a la neutralidad (influencia de mediadores sobre N)
 *   c = efectividad del diálogo para desescalar manifestaciones
 *   k = reacción institucional: mediadores generados por manifestantes
 *   r = desgaste de los mediadores con el tiempo
 *
 * Métodos implementados: Heun y RK4.
 */
@Service
public class EscenarioGService {

    /**
     * Ejecuta la simulación con ambos métodos (Heun y RK4) para comparación.
     */
    public EscenarioGResponse simular(EscenarioGRequest req) {
        ResultadoMetodoSocial resultadoHeun = resolverHeun(req);
        ResultadoMetodoSocial resultadoRK4  = resolverRK4(req);

        // Población total inicial (referencia para verificar la conservación del modelo)
        double poblacionTotal = req.getN0() + req.getM0() + req.getD0();

        return EscenarioGResponse.builder()
                .heun(resultadoHeun)
                .rk4(resultadoRK4)
                .poblacionTotal(poblacionTotal)
                .build();
    }

    // ==================== FUNCIONES DEL SISTEMA DE EDOs ====================

    /**
     * dN/dt = -a·N·M + b·D
     *
     * Los neutrales N disminuyen porque el descontento social se "contagia" (a·N·M).
     * Los neutrales N aumentan porque los mediadores D convencen a algunos manifestantes
     * de volver a la neutralidad (b·D).
     */
    private double fN(double N, double M, double D, EscenarioGRequest req) {
        return -req.getParamA() * N * M + req.getParamB() * D;
    }

    /**
     * dM/dt = a·N·M - c·M·D
     *
     * Los manifestantes M crecen por contagio del descontento desde neutrales (a·N·M).
     * Los manifestantes M disminuyen cuando los mediadores D tienen éxito en el diálogo (c·M·D).
     */
    private double fM(double N, double M, double D, EscenarioGRequest req) {
        return req.getParamA() * N * M - req.getParamC() * M * D;
    }

    /**
     * dD/dt = k·M - r·D
     *
     * Los mediadores D aparecen como respuesta institucional proporcional
     * al número de manifestantes (k·M).
     * Los mediadores D se desgastan y salen del proceso con el tiempo (r·D).
     */
    private double fD(double N, double M, double D, EscenarioGRequest req) {
        return req.getParamK() * M - req.getParamR() * D;
    }

    // ==================== MÉTODO DE HEUN PARA SISTEMAS ====================

    /**
     * Método de Heun aplicado al sistema de 3 EDOs acopladas.
     *
     * En cada paso se calculan las pendientes para las tres variables simultáneamente:
     *
     *   Predictor (Euler) para N, M, D:
     *     N* = N + h·fN(N, M, D)
     *     M* = M + h·fM(N, M, D)
     *     D* = D + h·fD(N, M, D)
     *
     *   Corrector (promedio de pendientes):
     *     N_{n+1} = N + (h/2)·[fN(N, M, D) + fN(N*, M*, D*)]
     *     M_{n+1} = M + (h/2)·[fM(N, M, D) + fM(N*, M*, D*)]
     *     D_{n+1} = D + (h/2)·[fD(N, M, D) + fD(N*, M*, D*)]
     *
     * Es crucial aplicar el predictor a las tres variables ANTES del corrector.
     */
    private ResultadoMetodoSocial resolverHeun(EscenarioGRequest req) {
        double h = req.getPasoTiempo();
        double t = 0.0;
        double N = req.getN0(); // Estado inicial de neutrales
        double M = req.getM0(); // Estado inicial de manifestantes
        double D = req.getD0(); // Estado inicial de mediadores

        List<PuntoSocial> puntos = new ArrayList<>();
        puntos.add(new PuntoSocial(t, N, M, D)); // Agregar condición inicial

        double picoM = M;       // Máximo de manifestantes observado
        double diaPicoM = 0.0;  // Día en que se alcanzó ese máximo

        while (t < req.getTiempoFinal()) {
            // === PREDICTOR (paso de Euler) ===
            // Calcular las pendientes en el punto actual (t, N, M, D)
            double k1N = fN(N, M, D, req);
            double k1M = fM(N, M, D, req);
            double k1D = fD(N, M, D, req);

            // Predecir los valores al final del paso
            double Np = N + h * k1N;
            double Mp = M + h * k1M;
            double Dp = D + h * k1D;

            // === CORRECTOR ===
            // Calcular las pendientes en el punto predicho (t+h, N*, M*, D*)
            double k2N = fN(Np, Mp, Dp, req);
            double k2M = fM(Np, Mp, Dp, req);
            double k2D = fD(Np, Mp, Dp, req);

            // Actualizar usando el promedio de la pendiente inicial y la predicha
            N = N + (h / 2.0) * (k1N + k2N);
            M = M + (h / 2.0) * (k1M + k2M);
            D = D + (h / 2.0) * (k1D + k2D);

            // Forzar no-negatividad: las poblaciones no pueden ser negativas
            N = Math.max(0.0, N);
            M = Math.max(0.0, M);
            D = Math.max(0.0, D);

            t += h;
            t = Math.round(t * 1000.0) / 1000.0;

            puntos.add(new PuntoSocial(t, N, M, D));

            // Rastrear el pico de manifestantes
            if (M > picoM) {
                picoM = M;
                diaPicoM = t;
            }
        }

        boolean estable = verificarEstabilidad(puntos);

        return ResultadoMetodoSocial.builder()
                .metodo("Heun")
                .puntos(puntos)
                .picoManifestantes(picoM)
                .diaPicoManifestantes(diaPicoM)
                .tieneEstabilidad(estable)
                .neutralesFinal(N)
                .manifestantesFinal(M)
                .mediadoresFinal(D)
                .build();
    }

    // ==================== MÉTODO RK4 PARA SISTEMAS ====================

    /**
     * Runge-Kutta de cuarto orden (RK4) aplicado al sistema de 3 EDOs.
     *
     * Para cada variable simultáneamente se calculan 4 etapas:
     *
     *   k1 = f(t, N, M, D)
     *   k2 = f(t+h/2, N+h/2·k1_N, M+h/2·k1_M, D+h/2·k1_D)
     *   k3 = f(t+h/2, N+h/2·k2_N, M+h/2·k2_M, D+h/2·k2_D)
     *   k4 = f(t+h,   N+h·k3_N,   M+h·k3_M,   D+h·k3_D)
     *
     *   Variable_{n+1} = Variable_n + (h/6)·(k1 + 2·k2 + 2·k3 + k4)
     *
     * IMPORTANTE: todas las kᵢ de N, M, D se calculan con los mismos valores
     * de las tres variables en ese punto intermedio (sistema acoplado).
     */
    private ResultadoMetodoSocial resolverRK4(EscenarioGRequest req) {
        double h = req.getPasoTiempo();
        double t = 0.0;
        double N = req.getN0();
        double M = req.getM0();
        double D = req.getD0();

        List<PuntoSocial> puntos = new ArrayList<>();
        puntos.add(new PuntoSocial(t, N, M, D));

        double picoM = M;
        double diaPicoM = 0.0;

        while (t < req.getTiempoFinal()) {
            // === k1: pendientes al inicio del intervalo ===
            double k1N = fN(N, M, D, req);
            double k1M = fM(N, M, D, req);
            double k1D = fD(N, M, D, req);

            // === k2: pendientes al punto medio usando k1 ===
            double N2 = N + (h / 2.0) * k1N;
            double M2 = M + (h / 2.0) * k1M;
            double D2 = D + (h / 2.0) * k1D;

            double k2N = fN(N2, M2, D2, req);
            double k2M = fM(N2, M2, D2, req);
            double k2D = fD(N2, M2, D2, req);

            // === k3: pendientes al punto medio usando k2 (corrección del medio) ===
            double N3 = N + (h / 2.0) * k2N;
            double M3 = M + (h / 2.0) * k2M;
            double D3 = D + (h / 2.0) * k2D;

            double k3N = fN(N3, M3, D3, req);
            double k3M = fM(N3, M3, D3, req);
            double k3D = fD(N3, M3, D3, req);

            // === k4: pendientes al final del intervalo usando k3 ===
            double N4 = N + h * k3N;
            double M4 = M + h * k3M;
            double D4 = D + h * k3D;

            double k4N = fN(N4, M4, D4, req);
            double k4M = fM(N4, M4, D4, req);
            double k4D = fD(N4, M4, D4, req);

            // === Actualizar con promedio ponderado (1:2:2:1) ===
            N = N + (h / 6.0) * (k1N + 2.0 * k2N + 2.0 * k3N + k4N);
            M = M + (h / 6.0) * (k1M + 2.0 * k2M + 2.0 * k3M + k4M);
            D = D + (h / 6.0) * (k1D + 2.0 * k2D + 2.0 * k3D + k4D);

            // Forzar no-negatividad
            N = Math.max(0.0, N);
            M = Math.max(0.0, M);
            D = Math.max(0.0, D);

            t += h;
            t = Math.round(t * 1000.0) / 1000.0;

            puntos.add(new PuntoSocial(t, N, M, D));

            if (M > picoM) {
                picoM = M;
                diaPicoM = t;
            }
        }

        boolean estable = verificarEstabilidad(puntos);

        return ResultadoMetodoSocial.builder()
                .metodo("RK4")
                .puntos(puntos)
                .picoManifestantes(picoM)
                .diaPicoManifestantes(diaPicoM)
                .tieneEstabilidad(estable)
                .neutralesFinal(N)
                .manifestantesFinal(M)
                .mediadoresFinal(D)
                .build();
    }

    // ==================== UTILIDADES ====================

    /**
     * Verifica si el sistema tiende a estabilizarse al final de la simulación.
     * Analiza el último 10% de los puntos: si la variación relativa de M
     * es menor al 5%, se considera que el sistema es estable.
     */
    private boolean verificarEstabilidad(List<PuntoSocial> puntos) {
        int n = puntos.size();
        if (n < 10) return false;

        // Tomar el último 10% de los puntos para analizar si el sistema se estabilizó
        int inicio = (int) (n * 0.9);

        double mMax  = puntos.stream().skip(inicio).mapToDouble(PuntoSocial::getManifestantes).max().orElse(0);
        double mMin  = puntos.stream().skip(inicio).mapToDouble(PuntoSocial::getManifestantes).min().orElse(0);
        double mProm = puntos.stream().skip(inicio).mapToDouble(PuntoSocial::getManifestantes).average().orElse(1);

        // Estable si la variación (max-min) es menor al 5% del promedio
        if (mProm < 1e-9) return true; // Prácticamente cero → estable
        return (mMax - mMin) / mProm < 0.05;
    }
}
