package com.desfinmetnum.desafio_final.controller;

import com.desfinmetnum.desafio_final.dto.EscenarioBRequest;
import com.desfinmetnum.desafio_final.dto.EscenarioBResponse;
import com.desfinmetnum.desafio_final.service.EscenarioBService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el Escenario B: Vaciado Crítico de Reservas de Carburantes.
 *
 * Expone el endpoint POST /api/escenario-b/simular que recibe parámetros
 * del problema y devuelve los resultados comparativos de Euler, Heun y RK4.
 *
 * El frontend envía un JSON con los parámetros y recibe como respuesta
 * las series temporales de R(t) para los tres métodos.
 */
@RestController
@RequestMapping("/api/escenario-b")
public class EscenarioBController {

    // Spring inyecta automáticamente el servicio (inyección por constructor)
    private final EscenarioBService service;

    public EscenarioBController(EscenarioBService service) {
        this.service = service;
    }

    /**
     * POST /api/escenario-b/simular
     *
     * Simula el vaciado de reservas con los tres métodos numéricos.
     * Devuelve un JSON con la serie temporal R(t) para Euler, Heun y RK4.
     *
     * Ejemplo de JSON de entrada:
     * {
     *   "reservaInicial": 10000,
     *   "entradaDiaria": 200,
     *   "consumoInicial": 300,
     *   "tasaCrecimientoConsumo": 0.03,
     *   "dias": 30,
     *   "pasoTiempo": 1.0,
     *   "nivelCritico": 500
     * }
     *
     * @param request JSON con los parámetros del problema
     * @return 200 OK con los resultados de los tres métodos
     */
    @PostMapping("/simular")
    public ResponseEntity<EscenarioBResponse> simular(@RequestBody EscenarioBRequest request) {
        // Delegar la lógica al servicio y devolver el resultado con HTTP 200
        EscenarioBResponse response = service.simular(request);
        return ResponseEntity.ok(response);
    }
}
