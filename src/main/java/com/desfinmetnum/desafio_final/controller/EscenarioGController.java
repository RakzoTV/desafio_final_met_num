package com.desfinmetnum.desafio_final.controller;

import com.desfinmetnum.desafio_final.dto.EscenarioGRequest;
import com.desfinmetnum.desafio_final.dto.EscenarioGResponse;
import com.desfinmetnum.desafio_final.dto.EscenarioGResponse.ResultadoMetodoSocial;
import com.desfinmetnum.desafio_final.service.EscenarioGService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el Escenario G: Modelo de Difusión de Opinión Social.
 *
 * Sistema de 3 EDOs acopladas:
 *   N'(t) = -a·N·M + b·D   (ciudadanos neutrales)
 *   M'(t) =  a·N·M - c·M·D (manifestantes activos)
 *   D'(t) =  k·M   - r·D   (mediadores / diálogo)
 *
 * Endpoints por método:
 *   POST /api/escenario-g/heun   → Método de Heun (orden 2)
 *   POST /api/escenario-g/rk4    → Runge-Kutta orden 4
 *   POST /api/escenario-g/todos  → Ambos métodos para comparar
 */
@RestController
@RequestMapping("/api/escenario-g")
public class EscenarioGController {

    private final EscenarioGService service;

    public EscenarioGController(EscenarioGService service) {
        this.service = service;
    }

    /**
     * POST /api/escenario-g/heun
     *
     * Método de Heun aplicado al sistema de 3 EDOs acopladas.
     *   Predictor: N* = N + h·fN,  M* = M + h·fM,  D* = D + h·fD
     *   Corrector: Variable_{n+1} = Variable_n + (h/2)·(k1 + k2)
     *
     * Error global O(h²). Más rápido que RK4 con menor precisión.
     *
     * Ejemplo JSON:
     * {
     *   "n0": 800,
     *   "m0": 150,
     *   "d0": 50,
     *   "paramA": 0.0003,
     *   "paramB": 0.05,
     *   "paramC": 0.001,
     *   "paramK": 0.04,
     *   "paramR": 0.03,
     *   "tiempoFinal": 60,
     *   "pasoTiempo": 0.5
     * }
     */
    @PostMapping("/heun")
    public ResponseEntity<ResultadoMetodoSocial> heun(@RequestBody EscenarioGRequest req) {
        return ResponseEntity.ok(service.resolverHeun(req));
    }

    /**
     * POST /api/escenario-g/rk4
     *
     * Runge-Kutta de cuarto orden aplicado al sistema de 3 EDOs.
     *   Calcula k1, k2, k3, k4 para N, M y D simultáneamente en cada paso.
     *   Variable_{n+1} = Variable_n + (h/6)·(k1 + 2k2 + 2k3 + k4)
     *
     * Error global O(h⁴). El método más preciso para este sistema.
     * Recomendado para análisis finales y presentación de resultados.
     */
    @PostMapping("/rk4")
    public ResponseEntity<ResultadoMetodoSocial> rk4(@RequestBody EscenarioGRequest req) {
        return ResponseEntity.ok(service.resolverRK4(req));
    }

    /**
     * POST /api/escenario-g/todos
     *
     * Ejecuta Heun y RK4 con los mismos parámetros y devuelve
     * ambos resultados para comparar las curvas N(t), M(t), D(t).
     *
     * Preguntas que responde la simulación:
     *   - ¿El conflicto tiende a estabilizarse o escala?
     *   - ¿Cuándo alcanza su pico el número de manifestantes?
     *   - ¿Qué pasa si se mejora la tasa de diálogo (paramC)?
     *   - ¿Qué ocurre si no hay mediadores (d0=0, paramK=0)?
     */
    @PostMapping("/todos")
    public ResponseEntity<EscenarioGResponse> todos(@RequestBody EscenarioGRequest req) {
        return ResponseEntity.ok(service.simular(req));
    }
}
