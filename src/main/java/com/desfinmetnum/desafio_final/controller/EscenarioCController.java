package com.desfinmetnum.desafio_final.controller;

import com.desfinmetnum.desafio_final.dto.EscenarioCRequest;
import com.desfinmetnum.desafio_final.dto.EscenarioCResponse;
import com.desfinmetnum.desafio_final.service.EscenarioCService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el Escenario C: Desabastecimiento y Curva Continua de Precios.
 *
 * Expone el endpoint POST /api/escenario-c/interpolar que recibe datos
 * dispersos de precios y devuelve curvas interpoladas con tres métodos.
 *
 * El frontend envía los datos conocidos (día, precio) y los días de interés,
 * y recibe la curva suave y los valores estimados con Lagrange, Newton y Splines.
 */
@RestController
@RequestMapping("/api/escenario-c")
public class EscenarioCController {

    private final EscenarioCService service;

    public EscenarioCController(EscenarioCService service) {
        this.service = service;
    }

    /**
     * POST /api/escenario-c/interpolar
     *
     * Interpola la curva de precios con Lagrange, Newton y Splines Cúbicos.
     * Devuelve los valores estimados en los días solicitados y la curva completa.
     *
     * Ejemplo de JSON de entrada (datos del documento):
     * {
     *   "producto": "Papa",
     *   "diasConocidos": [1, 5, 10, 15, 20, 30],
     *   "preciosConocidos": [8, 10, 13, 16, 19, 22],
     *   "diasEvaluar": [3, 7, 12, 18, 25],
     *   "puntosCurva": 100
     * }
     *
     * @param request JSON con los datos del producto y los días de evaluación
     * @return 200 OK con los resultados de interpolación de los tres métodos
     */
    @PostMapping("/interpolar")
    public ResponseEntity<EscenarioCResponse> interpolar(@RequestBody EscenarioCRequest request) {
        EscenarioCResponse response = service.interpolar(request);
        return ResponseEntity.ok(response);
    }
}
