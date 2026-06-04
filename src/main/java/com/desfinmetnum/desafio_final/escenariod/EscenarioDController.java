package com.desfinmetnum.desafio_final.escenariod;

import com.desfinmetnum.desafio_final.escenariod.dto.IntegracionRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/escenario-d")
public class EscenarioDController {

    private final EscenarioDService service;

    public EscenarioDController(EscenarioDService service) {
        this.service = service;
    }

    @PostMapping("/trapecio")
    public ResponseEntity<?> trapecio(@RequestBody IntegracionRequest req) {
        try {
            return ResponseEntity.ok(service.trapecio(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/simpson13")
    public ResponseEntity<?> simpson13(@RequestBody IntegracionRequest req) {
        try {
            return ResponseEntity.ok(service.simpson13(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/simpson38")
    public ResponseEntity<?> simpson38(@RequestBody IntegracionRequest req) {
        try {
            return ResponseEntity.ok(service.simpson38(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
