package com.desfinmetnum.desafio_final.controller;

import com.desfinmetnum.desafio_final.dto.EscenarioCRequest;
import com.desfinmetnum.desafio_final.dto.EscenarioCResponse;
import com.desfinmetnum.desafio_final.dto.EscenarioCResponse.ResultadoInterpolacion;
import com.desfinmetnum.desafio_final.service.EscenarioCService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el Escenario C: Curva Continua de Precios (Interpolación).
 *
 * Dado un conjunto de puntos dispersos (día, precio), reconstruye
 * una curva continua para estimar precios en días sin datos registrados.
 *
 * Endpoints por método:
 *   POST /api/escenario-c/lagrange   → Interpolación de Lagrange
 *   POST /api/escenario-c/newton     → Newton con Diferencias Divididas
 *   POST /api/escenario-c/splines    → Splines Cúbicos Naturales
 *   POST /api/escenario-c/todos      → Los tres métodos juntos para comparar
 */
@RestController
@RequestMapping("/api/escenario-c")
public class EscenarioCController {

    private final EscenarioCService service;

    public EscenarioCController(EscenarioCService service) {
        this.service = service;
    }

    /**
     * POST /api/escenario-c/lagrange
     *
     * Interpolación de Lagrange:
     *   P(x) = Σᵢ yᵢ · Lᵢ(x)   donde Lᵢ(x) = Π_{j≠i} (x-xⱼ)/(xᵢ-xⱼ)
     *
     * El polinomio pasa exactamente por todos los puntos conocidos.
     * Con muchos puntos puede oscilar (fenómeno de Runge).
     *
     * Ejemplo JSON:
     * {
     *   "producto": "Papa",
     *   "diasConocidos": [1, 5, 10, 15, 20, 30],
     *   "preciosConocidos": [8, 10, 13, 16, 19, 22],
     *   "diasEvaluar": [3, 7, 12, 18, 25],
     *   "puntosCurva": 100
     * }
     */
    @PostMapping("/lagrange")
    public ResponseEntity<ResultadoInterpolacion> lagrange(@RequestBody EscenarioCRequest req) {
        double[] x     = req.getDiasConocidos().stream().mapToDouble(Double::doubleValue).toArray();
        double[] y     = req.getPreciosConocidos().stream().mapToDouble(Double::doubleValue).toArray();
        double[] xEval = req.getDiasEvaluar().stream().mapToDouble(Double::doubleValue).toArray();
        double[] xCurva = generarXCurva(x, req.getPuntosCurva());
        return ResponseEntity.ok(service.calcularLagrange(x, y, xEval, xCurva));
    }

    /**
     * POST /api/escenario-c/newton
     *
     * Interpolación de Newton con Diferencias Divididas:
     *   P(x) = f[x₀] + f[x₀,x₁](x-x₀) + f[x₀,x₁,x₂](x-x₀)(x-x₁) + ...
     *
     * Ventaja: agregar un punto nuevo requiere solo una nueva columna.
     * Devuelve también la tabla de diferencias divididas.
     */
    @PostMapping("/newton")
    public ResponseEntity<ResultadoInterpolacion> newton(@RequestBody EscenarioCRequest req) {
        double[] x     = req.getDiasConocidos().stream().mapToDouble(Double::doubleValue).toArray();
        double[] y     = req.getPreciosConocidos().stream().mapToDouble(Double::doubleValue).toArray();
        double[] xEval = req.getDiasEvaluar().stream().mapToDouble(Double::doubleValue).toArray();
        double[] xCurva = generarXCurva(x, req.getPuntosCurva());
        return ResponseEntity.ok(service.calcularNewton(x, y, xEval, xCurva));
    }

    /**
     * POST /api/escenario-c/splines
     *
     * Splines Cúbicos Naturales:
     *   Sᵢ(x) = aᵢ + bᵢ(x-xᵢ) + cᵢ(x-xᵢ)² + dᵢ(x-xᵢ)³
     *
     * Un polinomio cúbico independiente por cada intervalo [xᵢ, xᵢ₊₁].
     * Primera y segunda derivada continua en todos los nodos.
     * La curva más suave y natural de los tres métodos.
     * Devuelve los coeficientes de cada tramo.
     */
    @PostMapping("/splines")
    public ResponseEntity<ResultadoInterpolacion> splines(@RequestBody EscenarioCRequest req) {
        double[] x     = req.getDiasConocidos().stream().mapToDouble(Double::doubleValue).toArray();
        double[] y     = req.getPreciosConocidos().stream().mapToDouble(Double::doubleValue).toArray();
        double[] xEval = req.getDiasEvaluar().stream().mapToDouble(Double::doubleValue).toArray();
        double[] xCurva = generarXCurva(x, req.getPuntosCurva());
        return ResponseEntity.ok(service.calcularSplines(x, y, xEval, xCurva));
    }

    /**
     * POST /api/escenario-c/todos
     *
     * Ejecuta los tres métodos con los mismos datos y devuelve
     * todos los resultados juntos para comparar las curvas.
     */
    @PostMapping("/todos")
    public ResponseEntity<EscenarioCResponse> todos(@RequestBody EscenarioCRequest req) {
        return ResponseEntity.ok(service.interpolar(req));
    }

    // Helper: genera puntos equiespaciados entre x[0] y x[n-1] para la curva del gráfico
    private double[] generarXCurva(double[] x, int puntosCurva) {
        int n = puntosCurva > 0 ? puntosCurva : 100;
        double xMin = x[0], xMax = x[x.length - 1];
        double[] xCurva = new double[n];
        for (int i = 0; i < n; i++) {
            xCurva[i] = xMin + i * (xMax - xMin) / (n - 1);
        }
        return xCurva;
    }
}
