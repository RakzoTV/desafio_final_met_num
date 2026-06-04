package com.desfinmetnum.desafio_final.controller;

import com.desfinmetnum.desafio_final.dto.EscenarioERequest;
import com.desfinmetnum.desafio_final.dto.EscenarioEResponse;
import com.desfinmetnum.desafio_final.service.EscenarioEService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el Escenario E: Umbrales Críticos de Abastecimiento.
 *
 * Expone el endpoint POST /api/escenario-e/resolver que busca la raíz
 * (umbral crítico) de una función no lineal usando tres métodos distintos.
 *
 * Casos de uso típicos:
 *   - Día en que el costo diario supera el ingreso familiar
 *   - Tasa de reposición que iguala el consumo de carburante
 *   - Umbral de opinión donde el descontento se masifica
 */
@RestController
@RequestMapping("/api/escenario-e")
public class EscenarioEController {

    private final EscenarioEService service;

    public EscenarioEController(EscenarioEService service) {
        this.service = service;
    }

    /**
     * POST /api/escenario-e/resolver
     *
     * Encuentra la raíz de f(x) = 0 con Bisección, Newton-Raphson y Secante.
     * Devuelve tabla de iteraciones, raíz encontrada y orden de convergencia.
     *
     * Ejemplo 1 — Función polinomial (x³ - 6x² + 11x - 6):
     * {
     *   "tipoFuncion": "POLINOMIAL",
     *   "coeficientes": [1, -6, 11, -6],
     *   "intervaloA": 0.5,
     *   "intervaloB": 1.5,
     *   "x0": 0.5,
     *   "x1": 1.5,
     *   "tolerancia": 0.000001,
     *   "maxIteraciones": 100,
     *   "puntosGrafico": 200
     * }
     *
     * Ejemplo 2 — Día donde el gasto diario supera los 80 Bs (exponencial):
     * {
     *   "tipoFuncion": "EXPONENCIAL",
     *   "coeficientes": [50, 0.03, 0, -80],
     *   "intervaloA": 0.1,
     *   "intervaloB": 30,
     *   "x0": 10,
     *   "x1": 20,
     *   "tolerancia": 0.000001,
     *   "maxIteraciones": 100,
     *   "puntosGrafico": 200
     * }
     *
     * @param request JSON con la función, intervalo, puntos iniciales y tolerancia
     * @return 200 OK con resultados de los tres métodos y gráfico de la función
     */
    @PostMapping("/resolver")
    public ResponseEntity<EscenarioEResponse> resolver(@RequestBody EscenarioERequest request) {
        EscenarioEResponse response = service.resolver(request);
        return ResponseEntity.ok(response);
    }
}
