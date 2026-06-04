package com.desfinmetnum.desafio_final.controller;

import com.desfinmetnum.desafio_final.dto.SistemaLinealRequest;
import com.desfinmetnum.desafio_final.service.EscenarioAService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el Escenario A: Optimización del Abastecimiento.
 *
 * Resuelve sistemas de ecuaciones lineales Ax = b que representan
 * la distribución óptima de productos desde plantas a zonas de distribución.
 *
 * Endpoints disponibles:
 *   POST /api/escenario-a/jacobi           → Método iterativo de Jacobi
 *   POST /api/escenario-a/gauss-seidel     → Método iterativo de Gauss-Seidel
 *   POST /api/escenario-a/sor              → Método SOR (Successive Over-Relaxation)
 *   POST /api/escenario-a/lu               → Descomposición LU (directo)
 *   POST /api/escenario-a/gradiente-conjugado → Gradiente Conjugado
 */
@RestController
@RequestMapping("/api/escenario-a")
public class EscenarioAController {

    // Spring inyecta el servicio automáticamente por constructor
    private final EscenarioAService service;

    public EscenarioAController(EscenarioAService service) {
        this.service = service;
    }

    /**
     * POST /api/escenario-a/jacobi
     *
     * Resuelve Ax = b usando el método iterativo de Jacobi.
     * Cada iteración calcula x_i usando solo los valores del paso anterior.
     * Requiere que la matriz sea diagonalmente dominante para garantizar convergencia.
     *
     * Ejemplo JSON:
     * {
     *   "matriz": [[10,2,1],[1,10,2],[2,1,10]],
     *   "terminos": [14, 16, 14],
     *   "tolerancia": 0.0001,
     *   "maxIteraciones": 100,
     *   "omega": null
     * }
     */
    @PostMapping("/jacobi")
    public ResponseEntity<?> jacobi(@RequestBody SistemaLinealRequest req) {
        try {
            return ResponseEntity.ok(service.jacobi(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST /api/escenario-a/gauss-seidel
     *
     * Resuelve Ax = b usando Gauss-Seidel.
     * A diferencia de Jacobi, usa los valores ya actualizados de la misma iteración.
     * Generalmente converge más rápido que Jacobi.
     */
    @PostMapping("/gauss-seidel")
    public ResponseEntity<?> gaussSeidel(@RequestBody SistemaLinealRequest req) {
        try {
            return ResponseEntity.ok(service.gaussSeidel(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST /api/escenario-a/sor
     *
     * Resuelve Ax = b usando SOR (Successive Over-Relaxation).
     * Introduce el parámetro omega ω ∈ (0, 2) para acelerar la convergencia.
     * ω = 1 → equivale a Gauss-Seidel.
     * ω > 1 → sobre-relajación (acelera convergencia si ω está bien elegido).
     * ω < 1 → sub-relajación (estabiliza sistemas difíciles).
     *
     * El campo "omega" en el JSON es obligatorio para este método.
     */
    @PostMapping("/sor")
    public ResponseEntity<?> sor(@RequestBody SistemaLinealRequest req) {
        try {
            if (req.getOmega() == null || req.getOmega() <= 0 || req.getOmega() >= 2) {
                return ResponseEntity.badRequest()
                        .body("El parámetro omega debe estar en el rango (0, 2). Ejemplo: 1.25");
            }
            return ResponseEntity.ok(service.sor(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST /api/escenario-a/lu
     *
     * Resuelve Ax = b mediante descomposición LU (método directo).
     * Factoriza A = L·U donde L es triangular inferior y U triangular superior.
     * Devuelve la solución exacta (sin iteraciones) y las matrices L y U.
     */
    @PostMapping("/lu")
    public ResponseEntity<?> lu(@RequestBody SistemaLinealRequest req) {
        try {
            return ResponseEntity.ok(service.lu(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * POST /api/escenario-a/gradiente-conjugado
     *
     * Resuelve Ax = b usando el método del Gradiente Conjugado.
     * Requiere que la matriz A sea simétrica y definida positiva.
     * Ideal para sistemas grandes y dispersos.
     */
    @PostMapping("/gradiente-conjugado")
    public ResponseEntity<?> gradienteConjugado(@RequestBody SistemaLinealRequest req) {
        try {
            return ResponseEntity.ok(service.gradienteConjugado(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
