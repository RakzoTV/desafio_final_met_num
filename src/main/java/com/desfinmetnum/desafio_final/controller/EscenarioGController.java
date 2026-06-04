package com.desfinmetnum.desafio_final.controller;

import com.desfinmetnum.desafio_final.dto.EscenarioGRequest;
import com.desfinmetnum.desafio_final.dto.EscenarioGResponse;
import com.desfinmetnum.desafio_final.service.EscenarioGService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el Escenario G: Modelo de Difusión de Opinión Social.
 *
 * Expone el endpoint POST /api/escenario-g/simular que simula la dinámica
 * entre ciudadanos neutrales, manifestantes y mediadores usando Heun y RK4.
 *
 * El frontend puede visualizar cómo evoluciona el conflicto social con distintos
 * valores de los parámetros (tasa de contagio, efectividad del diálogo, etc.)
 */
@RestController
@RequestMapping("/api/escenario-g")
public class EscenarioGController {

    private final EscenarioGService service;

    public EscenarioGController(EscenarioGService service) {
        this.service = service;
    }

    /**
     * POST /api/escenario-g/simular
     *
     * Simula la dinámica social N(t), M(t), D(t) con Heun y RK4.
     * Devuelve las series temporales de las tres poblaciones para comparar métodos
     * y analizar la estabilidad del conflicto.
     *
     * Ejemplo de JSON de entrada:
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
     *
     * Preguntas que responde la simulación:
     *   - ¿El conflicto tiende a estabilizarse o escala?
     *   - ¿Cuándo alcanza su pico el número de manifestantes?
     *   - ¿Qué pasa si se mejora la tasa de diálogo (paramC)?
     *   - ¿Qué ocurre si no hay mediadores (d0 = 0, paramK = 0)?
     *
     * @param request JSON con las condiciones iniciales y parámetros del modelo
     * @return 200 OK con series temporales de las tres poblaciones (Heun y RK4)
     */
    @PostMapping("/simular")
    public ResponseEntity<EscenarioGResponse> simular(@RequestBody EscenarioGRequest request) {
        EscenarioGResponse response = service.simular(request);
        return ResponseEntity.ok(response);
    }
}
