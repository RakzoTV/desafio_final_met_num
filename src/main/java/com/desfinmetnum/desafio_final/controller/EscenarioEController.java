package com.desfinmetnum.desafio_final.controller;

import com.desfinmetnum.desafio_final.dto.EscenarioERequest;
import com.desfinmetnum.desafio_final.dto.EscenarioEResponse;
import com.desfinmetnum.desafio_final.dto.EscenarioEResponse.ResultadoRaiz;
import com.desfinmetnum.desafio_final.dto.EscenarioEResponse.PuntoGrafico;
import com.desfinmetnum.desafio_final.service.EscenarioEService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para el Escenario E: Umbrales Críticos (Búsqueda de Raíces).
 *
 * Encuentra el valor x donde f(x) = 0, interpretado como el umbral
 * crítico donde el sistema cambia de comportamiento (ej: día en que
 * el gasto supera el ingreso familiar).
 *
 * Endpoints por método:
 *   POST /api/escenario-e/biseccion         → Método de Bisección
 *   POST /api/escenario-e/newton-raphson    → Método de Newton-Raphson
 *   POST /api/escenario-e/secante           → Método de la Secante
 *   POST /api/escenario-e/todos             → Los tres métodos + gráfica juntos
 */
@RestController
@RequestMapping("/api/escenario-e")
public class EscenarioEController {

    private final EscenarioEService service;

    public EscenarioEController(EscenarioEService service) {
        this.service = service;
    }

    /**
     * POST /api/escenario-e/biseccion
     *
     * Método de Bisección:
     *   c = (a + b) / 2  → la raíz está en la mitad donde f cambia de signo.
     *
     * REQUIERE: f(a)·f(b) < 0 (Teorema de Bolzano).
     * Convergencia lineal (p ≈ 1). Siempre converge si se cumplen las condiciones.
     *
     * Ejemplo JSON:
     * {
     *   "tipoFuncion": "POLINOMIAL",
     *   "coeficientes": [1, -6, 11, -6],
     *   "intervaloA": 0.5,
     *   "intervaloB": 1.5,
     *   "x0": 1.0,
     *   "x1": 1.5,
     *   "tolerancia": 0.000001,
     *   "maxIteraciones": 100,
     *   "puntosGrafico": 200
     * }
     */
    @PostMapping("/biseccion")
    public ResponseEntity<ResultadoRaiz> biseccion(@RequestBody EscenarioERequest req) {
        double[] coef = req.getCoeficientes().stream().mapToDouble(Double::doubleValue).toArray();
        String tipo = req.getTipoFuncion().toUpperCase();
        return ResponseEntity.ok(service.resolverBiseccion(tipo, coef, req));
    }

    /**
     * POST /api/escenario-e/newton-raphson
     *
     * Método de Newton-Raphson:
     *   x_{n+1} = x_n - f(x_n) / f'(x_n)
     *
     * Usa la derivada analítica. Convergencia cuadrática (p ≈ 2).
     * Muy rápido cerca de la raíz, pero puede diverger si f'(x) ≈ 0.
     * Solo necesita el punto inicial x0.
     */
    @PostMapping("/newton-raphson")
    public ResponseEntity<ResultadoRaiz> newtonRaphson(@RequestBody EscenarioERequest req) {
        double[] coef = req.getCoeficientes().stream().mapToDouble(Double::doubleValue).toArray();
        String tipo = req.getTipoFuncion().toUpperCase();
        return ResponseEntity.ok(service.resolverNewtonRaphson(tipo, coef, req));
    }

    /**
     * POST /api/escenario-e/secante
     *
     * Método de la Secante:
     *   x_{n+1} = x_n - f(x_n)·(x_n - x_{n-1}) / (f(x_n) - f(x_{n-1}))
     *
     * No necesita la derivada analítica. Convergencia superlineal (p ≈ 1.618 φ).
     * Necesita dos puntos iniciales: x0 y x1.
     */
    @PostMapping("/secante")
    public ResponseEntity<ResultadoRaiz> secante(@RequestBody EscenarioERequest req) {
        double[] coef = req.getCoeficientes().stream().mapToDouble(Double::doubleValue).toArray();
        String tipo = req.getTipoFuncion().toUpperCase();
        return ResponseEntity.ok(service.resolverSecante(tipo, coef, req));
    }

    /**
     * POST /api/escenario-e/grafica
     *
     * Devuelve solo los puntos (x, f(x)) para graficar la función.
     * Útil para previsualizar la función antes de elegir el intervalo de búsqueda.
     */
    @PostMapping("/grafica")
    public ResponseEntity<List<PuntoGrafico>> grafica(@RequestBody EscenarioERequest req) {
        double[] coef = req.getCoeficientes().stream().mapToDouble(Double::doubleValue).toArray();
        String tipo = req.getTipoFuncion().toUpperCase();
        return ResponseEntity.ok(service.generarGrafica(tipo, coef, req));
    }

    /**
     * POST /api/escenario-e/todos
     *
     * Ejecuta los tres métodos con los mismos parámetros y devuelve
     * todos los resultados + la gráfica juntos para comparar.
     */
    @PostMapping("/todos")
    public ResponseEntity<EscenarioEResponse> todos(@RequestBody EscenarioERequest req) {
        return ResponseEntity.ok(service.resolver(req));
    }
}
