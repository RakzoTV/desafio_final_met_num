package com.desfinmetnum.desafio_final.service;

import com.desfinmetnum.desafio_final.dto.EscenarioERequest;
import com.desfinmetnum.desafio_final.dto.EscenarioEResponse;
import com.desfinmetnum.desafio_final.dto.EscenarioEResponse.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio que implementa tres métodos de búsqueda de raíces:
 *   - Bisección
 *   - Newton-Raphson
 *   - Secante
 *
 * Se aplican a funciones no lineales que representan umbrales críticos
 * en el contexto de abastecimiento, precios y dinámica social.
 */
@Service
public class EscenarioEService {

    /**
     * Ejecuta los tres métodos de búsqueda de raíces y devuelve resultados comparativos.
     * Esto permite comparar velocidad de convergencia y robustez entre métodos.
     */
    public EscenarioEResponse resolver(EscenarioERequest req) {
        // Extraer coeficientes como arreglo primitivo
        double[] coef = req.getCoeficientes().stream().mapToDouble(Double::doubleValue).toArray();
        String tipo = req.getTipoFuncion().toUpperCase();

        // Construir la expresión legible de la función para mostrar al usuario
        String expresion = construirExpresion(tipo, coef);

        // Generar puntos para el gráfico de f(x) en el rango de interés
        List<PuntoGrafico> graficaFuncion = generarGrafica(tipo, coef, req);

        // Ejecutar los tres métodos
        ResultadoRaiz biseccion     = resolverBiseccion(tipo, coef, req);
        ResultadoRaiz newtonRaphson = resolverNewtonRaphson(tipo, coef, req);
        ResultadoRaiz secante       = resolverSecante(tipo, coef, req);

        return EscenarioEResponse.builder()
                .expresionFuncion(expresion)
                .biseccion(biseccion)
                .newtonRaphson(newtonRaphson)
                .secante(secante)
                .graficaFuncion(graficaFuncion)
                .build();
    }

    // ==================== EVALUACIÓN DE FUNCIONES ====================

    /**
     * Evalúa f(x) según el tipo de función seleccionado por el usuario.
     *
     * POLINOMIAL:     f(x) = a·x³ + b·x² + c·x + d
     * EXPONENCIAL:    f(x) = a·e^(b·x) + c·x + d
     * LOGARITMICA:    f(x) = a·ln(b·x) + c·x + d   (requiere b·x > 0)
     * TRIGONOMETRICA: f(x) = a·sin(b·x) + c·x + d
     *
     * @param tipo tipo de función (String en mayúsculas)
     * @param coef arreglo [a, b, c, d] con los coeficientes
     * @param x    punto donde evaluar
     * @return     f(x)
     */
    private double f(String tipo, double[] coef, double x) {
        double a = coef[0], b = coef[1], c = coef[2], d = coef[3];

        return switch (tipo) {
            case "POLINOMIAL" ->
                // f(x) = a·x³ + b·x² + c·x + d
                    a * x * x * x + b * x * x + c * x + d;

            case "EXPONENCIAL" ->
                // f(x) = a·e^(b·x) + c·x + d
                    a * Math.exp(b * x) + c * x + d;

            case "LOGARITMICA" ->
                // f(x) = a·ln(b·x) + c·x + d  (indefinida para b·x ≤ 0)
                    a * Math.log(b * x) + c * x + d;

            case "TRIGONOMETRICA" ->
                // f(x) = a·sin(b·x) + c·x + d
                    a * Math.sin(b * x) + c * x + d;

            default -> throw new IllegalArgumentException(
                    "Tipo de función no soportado: " + tipo +
                    ". Opciones válidas: POLINOMIAL, EXPONENCIAL, LOGARITMICA, TRIGONOMETRICA");
        };
    }

    /**
     * Evalúa f'(x) (derivada analítica) según el tipo de función.
     * Necesaria exclusivamente para el método de Newton-Raphson.
     */
    private double fPrima(String tipo, double[] coef, double x) {
        double a = coef[0], b = coef[1], c = coef[2];

        return switch (tipo) {
            case "POLINOMIAL" ->
                // f'(x) = 3a·x² + 2b·x + c
                    3 * a * x * x + 2 * b * x + c;

            case "EXPONENCIAL" ->
                // f'(x) = a·b·e^(b·x) + c
                    a * b * Math.exp(b * x) + c;

            case "LOGARITMICA" ->
                // f'(x) = a/x + c   (derivada de a·ln(b·x) = a/x)
                    a / x + c;

            case "TRIGONOMETRICA" ->
                // f'(x) = a·b·cos(b·x) + c
                    a * b * Math.cos(b * x) + c;

            default -> throw new IllegalArgumentException("Tipo no soportado: " + tipo);
        };
    }

    // ==================== MÉTODO DE BISECCIÓN ====================

    /**
     * Método de Bisección:
     * Dado un intervalo [a,b] donde f(a)·f(b) < 0 (Teorema de Bolzano),
     * divide el intervalo a la mitad en cada iteración.
     *
     *   c = (a + b) / 2
     *   Si f(a)·f(c) < 0 → la raíz está en [a, c] → b = c
     *   Si no            → la raíz está en [c, b] → a = c
     *
     * Convergencia: lineal (orden p ≈ 1). El error se reduce a la mitad cada vez.
     * Ventaja: siempre converge si se cumplen las condiciones iniciales.
     */
    public ResultadoRaiz resolverBiseccion(String tipo, double[] coef, EscenarioERequest req) {
        double a = req.getIntervaloA();
        double b = req.getIntervaloB();
        double tol = req.getTolerancia();
        int maxIter = req.getMaxIteraciones();

        List<IteracionRaiz> tabla = new ArrayList<>();

        double fa = f(tipo, coef, a);
        double fb = f(tipo, coef, b);

        // Verificar la condición de Bolzano: f(a) y f(b) deben tener signos contrarios
        if (fa * fb > 0) {
            return ResultadoRaiz.builder()
                    .metodo("Bisección")
                    .convergio(false)
                    .iteraciones(0)
                    .mensaje("Error: f(a) y f(b) deben tener signos opuestos (Teorema de Bolzano). " +
                             "f(" + String.format("%.4f", a) + ")=" + String.format("%.6f", fa) +
                             ", f(" + String.format("%.4f", b) + ")=" + String.format("%.6f", fb))
                    .tablaIteraciones(tabla)
                    .build();
        }

        double c = a;
        double fc;
        int iter = 0;
        List<Double> errores = new ArrayList<>();

        while (iter < maxIter) {
            // Punto medio del intervalo actual
            c = (a + b) / 2.0;
            fc = f(tipo, coef, c);
            double error = (b - a) / 2.0; // La mitad del intervalo actual es el error máximo

            tabla.add(new IteracionRaiz(iter + 1, a, b, c, fc, error));
            errores.add(error);

            iter++;

            // Criterio de parada: intervalo suficientemente pequeño o f(c) ≈ 0
            if (error < tol || Math.abs(fc) < tol) {
                break;
            }

            // Determinar en cuál mitad está la raíz y ajustar el intervalo
            if (fa * fc < 0) {
                // La raíz está en la mitad izquierda [a, c]
                b = c;
                fb = fc;
            } else {
                // La raíz está en la mitad derecha [c, b]
                a = c;
                fa = fc;
            }
        }

        Double orden = estimarOrdenConvergencia(errores);

        return ResultadoRaiz.builder()
                .metodo("Bisección")
                .raiz(c)
                .fRaiz(f(tipo, coef, c))
                .iteraciones(iter)
                .convergio(true)
                .mensaje("Bisección completada en " + iter + " iteraciones")
                .tablaIteraciones(tabla)
                .ordenConvergencia(orden)
                .build();
    }

    // ==================== MÉTODO DE NEWTON-RAPHSON ====================

    /**
     * Método de Newton-Raphson:
     *   x_{n+1} = x_n - f(x_n) / f'(x_n)
     *
     * Geométricamente: traza la tangente a la curva en x_n y encuentra
     * su intersección con el eje x para obtener la siguiente aproximación.
     *
     * Convergencia: cuadrática (orden p ≈ 2) cerca de la raíz.
     * → Muy rápido pero puede diverger si f'(x) ≈ 0 o si x₀ está lejos.
     */
    public ResultadoRaiz resolverNewtonRaphson(String tipo, double[] coef, EscenarioERequest req) {
        double x = req.getX0(); // Punto inicial
        double tol = req.getTolerancia();
        int maxIter = req.getMaxIteraciones();

        List<IteracionRaiz> tabla = new ArrayList<>();
        List<Double> errores = new ArrayList<>();

        int iter = 0;

        while (iter < maxIter) {
            double fx  = f(tipo, coef, x);      // Evaluar f(xₙ)
            double fpx = fPrima(tipo, coef, x); // Evaluar f'(xₙ)

            // Verificar que la derivada no sea prácticamente cero
            if (Math.abs(fpx) < 1e-15) {
                return ResultadoRaiz.builder()
                        .metodo("Newton-Raphson")
                        .raiz(x)
                        .fRaiz(fx)
                        .iteraciones(iter)
                        .convergio(false)
                        .mensaje("Error: f'(x) ≈ 0 en x = " + String.format("%.6f", x) +
                                 ". La tangente es horizontal, no hay intersección con el eje x.")
                        .tablaIteraciones(tabla)
                        .build();
            }

            double xAnterior = x;

            // Aplicar la fórmula de Newton: x_{n+1} = x_n - f(x_n)/f'(x_n)
            x = x - fx / fpx;

            double error = Math.abs(x - xAnterior);
            errores.add(error);

            // Registrar esta iteración en la tabla
            tabla.add(new IteracionRaiz(iter + 1, null, null, x, f(tipo, coef, x), error));

            iter++;

            // Criterio de parada: el cambio en x es muy pequeño
            if (error < tol || Math.abs(f(tipo, coef, x)) < tol) {
                break;
            }
        }

        Double orden = estimarOrdenConvergencia(errores);

        return ResultadoRaiz.builder()
                .metodo("Newton-Raphson")
                .raiz(x)
                .fRaiz(f(tipo, coef, x))
                .iteraciones(iter)
                .convergio(iter < maxIter)
                .mensaje("Newton-Raphson completado en " + iter + " iteraciones")
                .tablaIteraciones(tabla)
                .ordenConvergencia(orden)
                .build();
    }

    // ==================== MÉTODO DE LA SECANTE ====================

    /**
     * Método de la Secante:
     *   x_{n+1} = x_n - f(x_n) · (x_n - x_{n-1}) / (f(x_n) - f(x_{n-1}))
     *
     * Similar a Newton-Raphson pero NO necesita la derivada analítica.
     * Aproxima f'(x) con la pendiente de la recta secante entre dos puntos.
     *
     * Convergencia: superlineal (orden p ≈ 1.618, el número áureo φ).
     * → Más rápido que Bisección, más robusto que Newton cuando f' es difícil.
     */
    public ResultadoRaiz resolverSecante(String tipo, double[] coef, EscenarioERequest req) {
        double x0 = req.getX0(); // Primer punto inicial
        double x1 = req.getX1(); // Segundo punto inicial
        double tol = req.getTolerancia();
        int maxIter = req.getMaxIteraciones();

        List<IteracionRaiz> tabla = new ArrayList<>();
        List<Double> errores = new ArrayList<>();

        double fx0 = f(tipo, coef, x0);
        double fx1 = f(tipo, coef, x1);

        // Registrar los dos puntos iniciales en la tabla
        tabla.add(new IteracionRaiz(0, null, null, x0, fx0, null));
        tabla.add(new IteracionRaiz(1, null, null, x1, fx1, Math.abs(x1 - x0)));

        int iter = 2;
        double x2;

        while (iter <= maxIter) {
            // Verificar que la secante no sea horizontal (f(x1) ≈ f(x0))
            if (Math.abs(fx1 - fx0) < 1e-15) {
                return ResultadoRaiz.builder()
                        .metodo("Secante")
                        .raiz(x1)
                        .fRaiz(fx1)
                        .iteraciones(iter - 2)
                        .convergio(false)
                        .mensaje("Error: f(x₀) ≈ f(x₁), la secante es horizontal. " +
                                 "Elige dos puntos iniciales más distintos.")
                        .tablaIteraciones(tabla)
                        .build();
            }

            // Fórmula de la Secante: x₂ = x₁ - f(x₁)·(x₁-x₀) / (f(x₁)-f(x₀))
            x2 = x1 - fx1 * (x1 - x0) / (fx1 - fx0);

            double error = Math.abs(x2 - x1);
            errores.add(error);

            double fx2 = f(tipo, coef, x2);
            tabla.add(new IteracionRaiz(iter, null, null, x2, fx2, error));

            // Criterio de parada
            if (error < tol || Math.abs(fx2) < tol) {
                Double orden = estimarOrdenConvergencia(errores);
                return ResultadoRaiz.builder()
                        .metodo("Secante")
                        .raiz(x2)
                        .fRaiz(fx2)
                        .iteraciones(iter - 1)
                        .convergio(true)
                        .mensaje("Secante completada en " + (iter - 1) + " iteraciones")
                        .tablaIteraciones(tabla)
                        .ordenConvergencia(orden)
                        .build();
            }

            // Descartar el punto más antiguo (ventana deslizante de 2 puntos)
            x0  = x1;
            fx0 = fx1;
            x1  = x2;
            fx1 = fx2;

            iter++;
        }

        Double orden = estimarOrdenConvergencia(errores);

        return ResultadoRaiz.builder()
                .metodo("Secante")
                .raiz(x1)
                .fRaiz(f(tipo, coef, x1))
                .iteraciones(maxIter)
                .convergio(false)
                .mensaje("Secante no convergió en " + maxIter + " iteraciones. " +
                         "Intenta ajustar los puntos iniciales o aumentar maxIteraciones.")
                .tablaIteraciones(tabla)
                .ordenConvergencia(orden)
                .build();
    }

    // ==================== UTILIDADES ====================

    /**
     * Estima el orden de convergencia p empíricamente usando errores consecutivos.
     *
     * Fórmula: p ≈ ln(e_{n+1} / e_n) / ln(e_n / e_{n-1})
     *
     * Interpretación:
     *   p ≈ 1.0  → convergencia lineal  (Bisección)
     *   p ≈ 1.62 → convergencia superlineal (Secante, número áureo)
     *   p ≈ 2.0  → convergencia cuadrática (Newton-Raphson)
     */
    private Double estimarOrdenConvergencia(List<Double> errores) {
        if (errores.size() < 3) return null;

        int n = errores.size();
        double e1 = errores.get(n - 3); // e_{n-2}
        double e2 = errores.get(n - 2); // e_{n-1}
        double e3 = errores.get(n - 1); // e_n

        // Evitar divisiones por cero o logaritmos de cero/negativo
        if (e1 <= 0 || e2 <= 0 || e3 <= 0) return null;
        if (Math.abs(Math.log(e2 / e1)) < 1e-15) return null;

        return Math.log(e3 / e2) / Math.log(e2 / e1);
    }

    /**
     * Construye una cadena legible con la expresión de la función f(x)
     * usando los coeficientes y el tipo de función seleccionados.
     */
    public String construirExpresion(String tipo, double[] coef) {
        double a = coef[0], b = coef[1], c = coef[2], d = coef[3];

        return switch (tipo) {
            case "POLINOMIAL" ->
                    String.format("f(x) = %.4f·x³ + %.4f·x² + %.4f·x + %.4f", a, b, c, d);
            case "EXPONENCIAL" ->
                    String.format("f(x) = %.4f·e^(%.4f·x) + %.4f·x + %.4f", a, b, c, d);
            case "LOGARITMICA" ->
                    String.format("f(x) = %.4f·ln(%.4f·x) + %.4f·x + %.4f", a, b, c, d);
            case "TRIGONOMETRICA" ->
                    String.format("f(x) = %.4f·sin(%.4f·x) + %.4f·x + %.4f", a, b, c, d);
            default -> "f(x) = función no reconocida";
        };
    }

    /**
     * Genera puntos (x, f(x)) para el gráfico de la función.
     * El rango del gráfico se extiende un 20% más allá del intervalo [a, b].
     */
    public List<PuntoGrafico> generarGrafica(String tipo, double[] coef, EscenarioERequest req) {
        int nPuntos = req.getPuntosGrafico() > 0 ? req.getPuntosGrafico() : 200;
        double xMin = req.getIntervaloA();
        double xMax = req.getIntervaloB();

        // Expandir el rango un 20% en cada lado para mejor visualización
        double margen = (xMax - xMin) * 0.2;
        xMin -= margen;
        xMax += margen;

        List<PuntoGrafico> puntos = new ArrayList<>();
        double paso = (xMax - xMin) / (nPuntos - 1);

        for (int i = 0; i < nPuntos; i++) {
            double x = xMin + i * paso;
            try {
                double y = f(tipo, coef, x);
                // Solo incluir valores finitos (evitar NaN, Infinito)
                if (Double.isFinite(y)) {
                    puntos.add(new PuntoGrafico(x, y));
                }
            } catch (Exception e) {
                // Saltar puntos problemáticos (ej: ln de negativo)
            }
        }

        return puntos;
    }
}
