package com.desfinmetnum.desafio_final.escenarioa;

import com.desfinmetnum.desafio_final.escenarioa.dto.SistemaLinealRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/escenario-a")
public class EscenarioAController {

    private final EscenarioAService service;

    public EscenarioAController(EscenarioAService service) {
        this.service = service;
    }

    @PostMapping("/jacobi")
    public ResponseEntity<?> jacobi(@RequestBody SistemaLinealRequest req) {
        try {
            return ResponseEntity.ok(service.jacobi(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/gauss-seidel")
    public ResponseEntity<?> gaussSeidel(@RequestBody SistemaLinealRequest req) {
        try {
            return ResponseEntity.ok(service.gaussSeidel(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/sor")
    public ResponseEntity<?> sor(@RequestBody SistemaLinealRequest req) {
        try {
            if (req.getOmega() == null || req.getOmega() <= 0 || req.getOmega() >= 2) {
                return ResponseEntity.badRequest().body("El parámetro omega debe estar en el rango (0, 2).");
            }
            return ResponseEntity.ok(service.sor(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/lu")
    public ResponseEntity<?> lu(@RequestBody SistemaLinealRequest req) {
        try {
            return ResponseEntity.ok(service.lu(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/gradiente-conjugado")
    public ResponseEntity<?> gradienteConjugado(@RequestBody SistemaLinealRequest req) {
        try {
            return ResponseEntity.ok(service.gradienteConjugado(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
