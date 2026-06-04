package com.desfinmetnum.desafio_final.controller;

import com.desfinmetnum.desafio_final.dto.CondicionamientoRequest;
import com.desfinmetnum.desafio_final.dto.PerturbacionRequest;
import com.desfinmetnum.desafio_final.service.EscenarioFService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el Escenario F: Rumores de Desabastecimiento y Pánico.
 *
 * Modela cómo los rumores generan cambios artificiales en la demanda
 * y cómo el sistema de distribución responde (o colapsa) ante esas perturbaciones.
 *
 * El concepto central es el NÚMERO DE CONDICIÓN κ(A):
 *   κ pequeño → sistema estable, el rumor tiene efecto proporcional
 *   κ grande  → sistema mal condicionado, un rumor pequeño puede colapsar la distribución
 *
 * Endpoints:
 *   POST /api/escenario-f/condicionamiento    → Analiza κ y estabilidad del sistema
 *   POST /api/escenario-f/perturbacion        → Simula el efecto de un nivel de rumor
 *   POST /api/escenario-f/comparacion-rumores → Compara BAJO, MEDIO, ALTO y PANICO
 */
@RestController
@RequestMapping("/api/escenario-f")
public class EscenarioFController {

    private final EscenarioFService service;

    public EscenarioFController(EscenarioFService service) {
        this.service = service;
    }

    /**
     * POST /api/escenario-f/condicionamiento
     *
     * Calcula el número de condición κ del sistema de distribución
     * y determina si el sistema es estable o susceptible a los rumores.
     *
     * Ejemplo JSON:
     * {
     *   "matriz": [[10, 2, 1], [1, 10, 2], [2, 1, 10]],
     *   "terminos": [100, 150, 120]
     * }
     */
    @PostMapping("/condicionamiento")
    public ResponseEntity<?> condicionamiento(@RequestBody CondicionamientoRequest req) {
        try {
            return ResponseEntity.ok(service.condicionamiento(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST /api/escenario-f/perturbacion
     *
     * Simula el efecto de un rumor en la distribución.
     * Perturba el vector b en un porcentaje y resuelve el sistema perturbado.
     * Compara la solución original con la perturbada.
     *
     * Ejemplo JSON (rumor alto = +15% en demanda):
     * {
     *   "matriz": [[10, 2, 1], [1, 10, 2], [2, 1, 10]],
     *   "terminosOriginales": [100, 150, 120],
     *   "nivelRumor": "ALTO",
     *   "porcentajePerturbacion": null
     * }
     *
     * O con porcentaje personalizado:
     * {
     *   "matriz": [[10, 2, 1], [1, 10, 2], [2, 1, 10]],
     *   "terminosOriginales": [100, 150, 120],
     *   "nivelRumor": null,
     *   "porcentajePerturbacion": 8.0
     * }
     */
    @PostMapping("/perturbacion")
    public ResponseEntity<?> perturbacion(@RequestBody PerturbacionRequest req) {
        try {
            if (req.getTerminosOriginales() == null || req.getTerminosOriginales().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Los términos originales (terminosOriginales) no pueden estar vacíos.");
            }
            return ResponseEntity.ok(service.perturbacion(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST /api/escenario-f/comparacion-rumores
     *
     * Compara automáticamente los cuatro niveles de rumor:
     *   BAJO (1%), MEDIO (5%), ALTO (15%), PANICO (30%)
     *
     * Permite ver de un vistazo cómo escala el impacto según el nivel de alarma social.
     *
     * Ejemplo JSON (mismo que condicionamiento):
     * {
     *   "matriz": [[10, 2, 1], [1, 10, 2], [2, 1, 10]],
     *   "terminos": [100, 150, 120]
     * }
     */
    @PostMapping("/comparacion-rumores")
    public ResponseEntity<?> comparacionRumores(@RequestBody CondicionamientoRequest req) {
        try {
            return ResponseEntity.ok(service.comparacionRumores(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
