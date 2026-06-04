package com.desfinmetnum.desafio_final.escenariof;

import com.desfinmetnum.desafio_final.escenariof.dto.CondicionamientoRequest;
import com.desfinmetnum.desafio_final.escenariof.dto.PerturbacionRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/escenario-f")
public class EscenarioFController {

    private final EscenarioFService service;

    public EscenarioFController(EscenarioFService service) {
        this.service = service;
    }

    @PostMapping("/condicionamiento")
    public ResponseEntity<?> condicionamiento(@RequestBody CondicionamientoRequest req) {
        try {
            return ResponseEntity.ok(service.condicionamiento(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/perturbacion")
    public ResponseEntity<?> perturbacion(@RequestBody PerturbacionRequest req) {
        try {
            if (req.getTerminosOriginales() == null || req.getTerminosOriginales().isEmpty()) {
                return ResponseEntity.badRequest().body("Los términos originales no pueden estar vacíos.");
            }
            return ResponseEntity.ok(service.perturbacion(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/comparacion-rumores")
    public ResponseEntity<?> comparacionRumores(@RequestBody CondicionamientoRequest req) {
        try {
            return ResponseEntity.ok(service.comparacionRumores(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
