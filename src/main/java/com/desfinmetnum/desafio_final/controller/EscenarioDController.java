package com.desfinmetnum.desafio_final.controller;

import com.desfinmetnum.desafio_final.dto.IntegracionRequest;
import com.desfinmetnum.desafio_final.service.EscenarioDService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el Escenario D: Costo Acumulado y Pérdida del Poder Adquisitivo.
 *
 * Calcula el gasto total de una familia durante el mes integrando numéricamente
 * la curva de precios de productos básicos.
 *
 * Endpoints disponibles:
 *   POST /api/escenario-d/trapecio   → Regla del Trapecio
 *   POST /api/escenario-d/simpson13  → Simpson 1/3 (requiere número par de intervalos)
 *   POST /api/escenario-d/simpson38  → Simpson 3/8 (requiere intervalos múltiplo de 3)
 */
@RestController
@RequestMapping("/api/escenario-d")
public class EscenarioDController {

    private final EscenarioDService service;

    public EscenarioDController(EscenarioDService service) {
        this.service = service;
    }

    /**
     * POST /api/escenario-d/trapecio
     *
     * Regla del Trapecio: aproxima el área bajo la curva de precios
     * como suma de trapecios entre puntos consecutivos.
     * Funciona con cualquier número de puntos y espaciado irregular.
     *
     * Ejemplo JSON:
     * {
     *   "producto": "Papa",
     *   "dias": [1, 5, 10, 15, 20, 30],
     *   "precios": [8, 10, 13, 16, 19, 22],
     *   "precioBase": 8.0
     * }
     */
    @PostMapping("/trapecio")
    public ResponseEntity<?> trapecio(@RequestBody IntegracionRequest req) {
        try {
            return ResponseEntity.ok(service.trapecio(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST /api/escenario-d/simpson13
     *
     * Simpson 1/3: usa polinomios de segundo grado por grupos de 3 puntos.
     * Fórmula: (h/3) * (f0 + 4f1 + f2) para cada par de intervalos.
     * REQUIERE número par de intervalos (número impar de puntos).
     */
    @PostMapping("/simpson13")
    public ResponseEntity<?> simpson13(@RequestBody IntegracionRequest req) {
        try {
            return ResponseEntity.ok(service.simpson13(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST /api/escenario-d/simpson38
     *
     * Simpson 3/8: usa polinomios de tercer grado por grupos de 4 puntos.
     * Fórmula: (3h/8) * (f0 + 3f1 + 3f2 + f3) para cada grupo de 3 intervalos.
     * REQUIERE que el número de intervalos sea múltiplo de 3.
     */
    @PostMapping("/simpson38")
    public ResponseEntity<?> simpson38(@RequestBody IntegracionRequest req) {
        try {
            return ResponseEntity.ok(service.simpson38(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
