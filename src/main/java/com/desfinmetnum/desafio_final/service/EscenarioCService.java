package com.desfinmetnum.desafio_final.service;

import com.desfinmetnum.desafio_final.dto.EscenarioCRequest;
import com.desfinmetnum.desafio_final.dto.EscenarioCResponse;
import com.desfinmetnum.desafio_final.dto.EscenarioCResponse.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio que implementa los tres métodos de interpolación:
 *   - Lagrange
 *   - Newton (Diferencias Divididas)
 *   - Splines Cúbicos Naturales
 *
 * Dado un conjunto de puntos dispersos (día, precio), reconstruye
 * una curva continua para estimar precios en días sin datos registrados.
 */
@Service
public class EscenarioCService {

    /**
     * Ejecuta los tres métodos de interpolación sobre los datos de entrada
     * y devuelve resultados comparativos para mostrar en el frontend.
     */
    public EscenarioCResponse interpolar(EscenarioCRequest req) {
        // Convertir las listas de Double a arreglos primitivos double[]
        double[] x = req.getDiasConocidos().stream().mapToDouble(Double::doubleValue).toArray();
        double[] y = req.getPreciosConocidos().stream().mapToDouble(Double::doubleValue).toArray();
        double[] xEval = req.getDiasEvaluar().stream().mapToDouble(Double::doubleValue).toArray();

        // Generar puntos equiespaciados entre x[0] y x[n-1] para la curva suave del gráfico
        double xMin = x[0];
        double xMax = x[x.length - 1];
        int nCurva = req.getPuntosCurva() > 0 ? req.getPuntosCurva() : 100;
        double[] xCurva = new double[nCurva];
        for (int i = 0; i < nCurva; i++) {
            // Distribuir los puntos de forma equiespaciada en el rango [xMin, xMax]
            xCurva[i] = xMin + i * (xMax - xMin) / (nCurva - 1);
        }

        // Ejecutar cada método de interpolación
        ResultadoInterpolacion lagrange = calcularLagrange(x, y, xEval, xCurva);
        ResultadoInterpolacion newton   = calcularNewton(x, y, xEval, xCurva);
        ResultadoInterpolacion splines  = calcularSplines(x, y, xEval, xCurva);

        return EscenarioCResponse.builder()
                .producto(req.getProducto())
                .lagrange(lagrange)
                .newton(newton)
                .splines(splines)
                .build();
    }

    // ==================== INTERPOLACIÓN DE LAGRANGE ====================

    /**
     * Interpolación de Lagrange:
     *   P(x) = Σᵢ yᵢ · Lᵢ(x)
     *
     * Donde el polinomio base Lᵢ(x) vale 1 en xᵢ y 0 en todos los demás puntos:
     *   Lᵢ(x) = Π_{j≠i} (x - xⱼ) / (xᵢ - xⱼ)
     *
     * El polinomio resultante pasa exactamente por todos los puntos dados.
     * Desventaja: para muchos puntos puede oscilar mucho (fenómeno de Runge).
     */
    public ResultadoInterpolacion calcularLagrange(double[] x, double[] y,
                                                    double[] xEval, double[] xCurva) {
        // Evaluar en los días específicos que pidió el usuario
        List<PuntoXY> valoresInterpolados = new ArrayList<>();
        for (double xe : xEval) {
            valoresInterpolados.add(new PuntoXY(xe, evaluarLagrange(x, y, xe)));
        }

        // Generar la curva suave para el gráfico
        List<PuntoXY> curva = new ArrayList<>();
        for (double xc : xCurva) {
            curva.add(new PuntoXY(xc, evaluarLagrange(x, y, xc)));
        }

        return ResultadoInterpolacion.builder()
                .metodo("Lagrange")
                .valoresInterpolados(valoresInterpolados)
                .curva(curva)
                .build();
    }

    /**
     * Evalúa el polinomio de Lagrange en un punto xEval.
     * Fórmula: P(xEval) = Σᵢ yᵢ · Lᵢ(xEval)
     */
    private double evaluarLagrange(double[] x, double[] y, double xEval) {
        int n = x.length;
        double resultado = 0.0;

        for (int i = 0; i < n; i++) {
            // Calcular el polinomio base Lᵢ(xEval)
            double Li = 1.0;
            for (int j = 0; j < n; j++) {
                if (j != i) {
                    // Multiplicar (xEval - xⱼ) / (xᵢ - xⱼ) para cada j ≠ i
                    Li *= (xEval - x[j]) / (x[i] - x[j]);
                }
            }
            // Acumular la contribución de este punto base
            resultado += y[i] * Li;
        }

        return resultado;
    }

    // ==================== INTERPOLACIÓN DE NEWTON ====================

    /**
     * Interpolación de Newton con Diferencias Divididas:
     *   P(x) = f[x₀] + f[x₀,x₁](x-x₀) + f[x₀,x₁,x₂](x-x₀)(x-x₁) + ...
     *
     * Las diferencias divididas se construyen recursivamente:
     *   f[xᵢ] = yᵢ
     *   f[xᵢ,xᵢ₊₁] = (f[xᵢ₊₁] - f[xᵢ]) / (xᵢ₊₁ - xᵢ)
     *   ...y así sucesivamente para órdenes superiores.
     *
     * Ventaja: agregar un punto nuevo solo requiere una nueva columna en la tabla.
     */
    public ResultadoInterpolacion calcularNewton(double[] x, double[] y,
                                                  double[] xEval, double[] xCurva) {
        int n = x.length;

        // PASO 1: Construir la tabla de diferencias divididas
        // tabla[i][j] = f[xᵢ, xᵢ₊₁, ..., xᵢ₊ⱼ]
        double[][] tabla = new double[n][n];

        // La primera columna es simplemente los valores yᵢ
        for (int i = 0; i < n; i++) {
            tabla[i][0] = y[i];
        }

        // Calcular diferencias divididas de orden j (columna j)
        for (int j = 1; j < n; j++) {
            for (int i = 0; i < n - j; i++) {
                // f[xᵢ,...,xᵢ₊ⱼ] = (f[xᵢ₊₁,...,xᵢ₊ⱼ] - f[xᵢ,...,xᵢ₊ⱼ₋₁]) / (xᵢ₊ⱼ - xᵢ)
                tabla[i][j] = (tabla[i + 1][j - 1] - tabla[i][j - 1]) / (x[i + j] - x[i]);
            }
        }

        // Convertir la tabla triangular a List<List<Double>> para el JSON de respuesta
        List<List<Double>> tablaLista = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            List<Double> fila = new ArrayList<>();
            for (int j = 0; j < n - i; j++) {
                fila.add(tabla[i][j]);
            }
            tablaLista.add(fila);
        }

        // PASO 2: Los coeficientes del polinomio de Newton son la primera fila
        // coef[k] = f[x₀, x₁, ..., xₖ] = tabla[0][k]
        double[] coef = new double[n];
        for (int i = 0; i < n; i++) {
            coef[i] = tabla[0][i];
        }

        // PASO 3: Evaluar el polinomio en los puntos solicitados
        List<PuntoXY> valoresInterpolados = new ArrayList<>();
        for (double xe : xEval) {
            valoresInterpolados.add(new PuntoXY(xe, evaluarNewton(coef, x, xe)));
        }

        // Generar la curva suave
        List<PuntoXY> curva = new ArrayList<>();
        for (double xc : xCurva) {
            curva.add(new PuntoXY(xc, evaluarNewton(coef, x, xc)));
        }

        return ResultadoInterpolacion.builder()
                .metodo("Newton")
                .valoresInterpolados(valoresInterpolados)
                .curva(curva)
                .diferenciaDivididas(tablaLista)
                .build();
    }

    /**
     * Evalúa el polinomio de Newton en xEval usando el esquema de Horner anidado.
     * P(x) = c₀ + c₁(x-x₀) + c₂(x-x₀)(x-x₁) + ...
     *
     * Evaluación eficiente de derecha a izquierda:
     *   resultado = cₙ₋₁
     *   resultado = cᵢ + (x - xᵢ) * resultado   (para i = n-2 hasta 0)
     */
    private double evaluarNewton(double[] coef, double[] x, double xEval) {
        int n = coef.length;
        double resultado = coef[n - 1]; // Empezar con el último coeficiente

        // Recorrer de derecha a izquierda (esquema de Horner para Newton)
        for (int i = n - 2; i >= 0; i--) {
            resultado = coef[i] + (xEval - x[i]) * resultado;
        }

        return resultado;
    }

    // ==================== SPLINES CÚBICOS NATURALES ====================

    /**
     * Splines Cúbicos Naturales:
     * En cada intervalo [xᵢ, xᵢ₊₁] se define un polinomio cúbico independiente:
     *   Sᵢ(x) = aᵢ + bᵢ(x-xᵢ) + cᵢ(x-xᵢ)² + dᵢ(x-xᵢ)³
     *
     * Las condiciones que deben cumplirse:
     *   1. Sᵢ(xᵢ)   = yᵢ            (pasa por los puntos)
     *   2. Sᵢ(xᵢ₊₁) = yᵢ₊₁          (continuidad en los nodos)
     *   3. S'ᵢ(xᵢ₊₁) = S'ᵢ₊₁(xᵢ₊₁)  (primera derivada continua)
     *   4. S''ᵢ(xᵢ₊₁) = S''ᵢ₊₁(xᵢ₊₁) (segunda derivada continua)
     *   5. S''₀(x₀) = 0  y  S''ₙ₋₁(xₙ) = 0  (condición natural)
     *
     * Se resuelve un sistema tridiagonal con el algoritmo de Thomas (O(n)).
     */
    public ResultadoInterpolacion calcularSplines(double[] x, double[] y,
                                                   double[] xEval, double[] xCurva) {
        int n = x.length;  // Número de puntos conocidos
        int m = n - 1;     // Número de intervalos (tramos del spline)

        // Caso degenerado: con 2 o menos puntos usar interpolación lineal
        if (n <= 2) {
            return construirSplineLineal(x, y, xEval, xCurva);
        }

        // PASO 1: Calcular los intervalos hᵢ = xᵢ₊₁ - xᵢ
        double[] h = new double[m];
        for (int i = 0; i < m; i++) {
            h[i] = x[i + 1] - x[i];
        }

        // PASO 2: Construir el sistema tridiagonal para los c (segundas derivadas escaladas)
        // El sistema tiene (n-2) ecuaciones para c₁, c₂, ..., cₙ₋₂
        // (c₀ = 0 y cₙ₋₁ = 0 por la condición de spline natural)
        int size = n - 2;
        double[] diagInf  = new double[size]; // Sub-diagonal
        double[] diagPrin = new double[size]; // Diagonal principal
        double[] diagSup  = new double[size]; // Super-diagonal
        double[] rhs      = new double[size]; // Vector del lado derecho

        for (int i = 0; i < size; i++) {
            int idx = i + 1; // Índice real en el sistema original (c₁ a cₙ₋₂)

            // Diagonal principal: 2*(hᵢ₋₁ + hᵢ)
            diagPrin[i] = 2.0 * (h[idx - 1] + h[idx]);

            // Lado derecho: 3*[(yᵢ₊₁ - yᵢ)/hᵢ - (yᵢ - yᵢ₋₁)/hᵢ₋₁]
            rhs[i] = 3.0 * ((y[idx + 1] - y[idx]) / h[idx]
                         - (y[idx] - y[idx - 1]) / h[idx - 1]);

            // Sub-diagonal: hᵢ₋₁ (excepto primera fila)
            if (i > 0) {
                diagInf[i] = h[idx - 1];
            }

            // Super-diagonal: hᵢ (excepto última fila)
            if (i < size - 1) {
                diagSup[i] = h[idx];
            }
        }

        // PASO 3: Resolver el sistema tridiagonal con el algoritmo de Thomas
        double[] cInterior = resolverTridiagonal(diagInf, diagPrin, diagSup, rhs);

        // Ensamblar el vector completo de c con c₀=0 y cₙ₋₁=0
        double[] c = new double[n];
        c[0] = 0.0;      // Condición natural: segunda derivada = 0 en los extremos
        c[n - 1] = 0.0;  // Condición natural
        for (int i = 0; i < size; i++) {
            c[i + 1] = cInterior[i];
        }

        // PASO 4: Calcular coeficientes a, b, d de cada tramo a partir de c
        double[] a = new double[m];
        double[] b = new double[m];
        double[] d = new double[m];
        List<CoeficienteSpline> coeficientes = new ArrayList<>();

        for (int i = 0; i < m; i++) {
            a[i] = y[i];

            // bᵢ = (yᵢ₊₁ - yᵢ)/hᵢ - hᵢ*(2cᵢ + cᵢ₊₁)/3
            b[i] = (y[i + 1] - y[i]) / h[i] - h[i] * (2.0 * c[i] + c[i + 1]) / 3.0;

            // dᵢ = (cᵢ₊₁ - cᵢ) / (3*hᵢ)
            d[i] = (c[i + 1] - c[i]) / (3.0 * h[i]);

            coeficientes.add(new CoeficienteSpline(i, x[i], a[i], b[i], c[i], d[i]));
        }

        // PASO 5: Evaluar el spline en los puntos solicitados
        List<PuntoXY> valoresInterpolados = new ArrayList<>();
        for (double xe : xEval) {
            valoresInterpolados.add(new PuntoXY(xe, evaluarSpline(x, a, b, c, d, m, xe)));
        }

        // Generar curva suave para el gráfico
        List<PuntoXY> curva = new ArrayList<>();
        for (double xc : xCurva) {
            curva.add(new PuntoXY(xc, evaluarSpline(x, a, b, c, d, m, xc)));
        }

        return ResultadoInterpolacion.builder()
                .metodo("Splines Cúbicos")
                .valoresInterpolados(valoresInterpolados)
                .curva(curva)
                .coeficientesSpline(coeficientes)
                .build();
    }

    /**
     * Evalúa el spline cúbico en un punto xEval.
     * Busca el tramo correcto y aplica:
     *   Sᵢ(x) = aᵢ + bᵢ·dx + cᵢ·dx² + dᵢ·dx³   donde dx = x - xᵢ
     */
    private double evaluarSpline(double[] x, double[] a, double[] b,
                                 double[] c, double[] d, int m, double xEval) {
        // Encontrar el tramo i tal que x[i] <= xEval < x[i+1]
        int i = m - 1; // Por defecto, el último tramo
        for (int j = 0; j < m - 1; j++) {
            if (xEval < x[j + 1]) {
                i = j;
                break;
            }
        }

        // dx = distancia desde el inicio del tramo
        double dx = xEval - x[i];

        // Evaluar el polinomio cúbico del tramo i en xEval
        return a[i] + b[i] * dx + c[i] * dx * dx + d[i] * dx * dx * dx;
    }

    /**
     * Algoritmo de Thomas para resolver un sistema tridiagonal Ax = d en O(n).
     * El sistema tiene la forma:
     *   [b₀ c₀  0 ...] [x₀]   [d₀]
     *   [a₁ b₁ c₁ ...] [x₁] = [d₁]
     *   [0  a₂ b₂ ...] [x₂]   [d₂]
     *   [           ...] ...   [...]
     *
     * Se realiza eliminación hacia adelante y luego sustitución hacia atrás.
     */
    private double[] resolverTridiagonal(double[] a, double[] b, double[] c, double[] d) {
        int n = b.length;
        if (n == 0) return new double[0];
        if (n == 1) return new double[]{d[0] / b[0]};

        // Copias de trabajo para no modificar los arreglos originales
        double[] cp = new double[n]; // c prima (c modificado)
        double[] dp = new double[n]; // d prima (d modificado)

        // === Fase 1: Eliminación hacia adelante (forward sweep) ===
        cp[0] = c[0] / b[0];
        dp[0] = d[0] / b[0];

        for (int i = 1; i < n; i++) {
            // Factor de eliminación para la fila i
            double denominador = b[i] - a[i] * cp[i - 1];
            cp[i] = (i < n - 1) ? c[i] / denominador : 0.0;
            dp[i] = (d[i] - a[i] * dp[i - 1]) / denominador;
        }

        // === Fase 2: Sustitución hacia atrás (back substitution) ===
        double[] sol = new double[n];
        sol[n - 1] = dp[n - 1];

        for (int i = n - 2; i >= 0; i--) {
            sol[i] = dp[i] - cp[i] * sol[i + 1];
        }

        return sol;
    }

    /**
     * Fallback para 2 puntos o menos: usar interpolación lineal (Lagrange de grado 1).
     */
    private ResultadoInterpolacion construirSplineLineal(double[] x, double[] y,
                                                        double[] xEval, double[] xCurva) {
        List<PuntoXY> valoresInterpolados = new ArrayList<>();
        for (double xe : xEval) {
            valoresInterpolados.add(new PuntoXY(xe, evaluarLagrange(x, y, xe)));
        }

        List<PuntoXY> curva = new ArrayList<>();
        for (double xc : xCurva) {
            curva.add(new PuntoXY(xc, evaluarLagrange(x, y, xc)));
        }

        return ResultadoInterpolacion.builder()
                .metodo("Splines Cúbicos (lineal — pocos puntos)")
                .valoresInterpolados(valoresInterpolados)
                .curva(curva)
                .build();
    }
}
