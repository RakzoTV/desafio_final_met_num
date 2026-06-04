package com.desfinmetnum.desafio_final.controller;

import com.desfinmetnum.desafio_final.dto.EscenarioBRequest;
import com.desfinmetnum.desafio_final.dto.EscenarioBResponse;
import com.desfinmetnum.desafio_final.dto.EscenarioBResponse.ResultadoMetodoODE;
import com.desfinmetnum.desafio_final.service.EscenarioBService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el Escenario B: Vaciado Crítico de Reservas de Carburantes.
 *
 * Resuelve la EDO: R'(t) = entrada - consumo(t)
 * Donde: consumo(t) = consumoInicial * (1 + tasaCrecimiento * t)
 *
 * Endpoints por método:
 *   POST /api/escenario-b/euler          → Método de Euler (orden 1)
 *   POST /api/escenario-b/heun           → Método de Heun (orden 2)
 *   POST /api/escenario-b/rk4            → Runge-Kutta orden 4
 *   POST /api/escenario-b/todos          → Los tres métodos juntos para comparar
 */
@RestController
@RequestMapping("/api/escenario-b")
public class EscenarioBController {

    private final EscenarioBService service;

    public EscenarioBController(EscenarioBService service) {
        this.service = service;
    }

    /**
     * POST /api/escenario-b/euler
     *
     * Método de Euler (primer orden):
     *   R_{n+1} = R_n + h * f(t_n, R_n)
     *
     * El más simple. Usa solo la pendiente al inicio del intervalo.
     * Error global O(h). Puede acumular error en simulaciones largas.
     *
     * Ejemplo JSON:
     * {
     *   "reservaInicial": 10000,
     *   "entradaDiaria": 200,
     *   "consumoInicial": 300,
     *   "tasaCrecimientoConsumo": 0.03,
     *   "dias": 30,
     *   "pasoTiempo": 1.0,
     *   "nivelCritico": 500
     * }
     */
    @PostMapping("/euler")
    public ResponseEntity<ResultadoMetodoODE> euler(@RequestBody EscenarioBRequest req) {
        return ResponseEntity.ok(service.resolverEuler(req));
    }

    /**
     * POST /api/escenario-b/heun
     *
     * Método de Heun (Euler mejorado, segundo orden):
     *   k1 = f(t_n, R_n)
     *   k2 = f(t_n + h, R_n + h*k1)
     *   R_{n+1} = R_n + (h/2) * (k1 + k2)
     *
     * Usa el promedio de dos pendientes (inicio y fin del intervalo).
     * Error global O(h²). Más preciso que Euler con el mismo costo.
     */
    @PostMapping("/heun")
    public ResponseEntity<ResultadoMetodoODE> heun(@RequestBody EscenarioBRequest req) {
        return ResponseEntity.ok(service.resolverHeun(req));
    }

    /**
     * POST /api/escenario-b/rk4
     *
     * Runge-Kutta de cuarto orden (RK4):
     *   k1 = f(t, R),  k2 = f(t+h/2, R+h/2·k1)
     *   k3 = f(t+h/2, R+h/2·k2),  k4 = f(t+h, R+h·k3)
     *   R_{n+1} = R_n + (h/6)*(k1 + 2k2 + 2k3 + k4)
     *
     * Error global O(h⁴). El método estándar de facto para EDOs.
     * El más preciso de los tres métodos disponibles.
     */
    @PostMapping("/rk4")
    public ResponseEntity<ResultadoMetodoODE> rk4(@RequestBody EscenarioBRequest req) {
        return ResponseEntity.ok(service.resolverRK4(req));
    }

    /**
     * POST /api/escenario-b/todos
     *
     * Ejecuta los tres métodos con los mismos parámetros y devuelve
     * todos los resultados juntos para facilitar la comparación.
     * Útil para graficar las tres curvas R(t) en la misma imagen.
     */
    @PostMapping("/todos")
    public ResponseEntity<EscenarioBResponse> todos(@RequestBody EscenarioBRequest req) {
        return ResponseEntity.ok(service.simular(req));
    }
}
