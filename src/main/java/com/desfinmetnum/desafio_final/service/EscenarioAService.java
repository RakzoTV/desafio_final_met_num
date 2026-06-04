package com.desfinmetnum.desafio_final.service;

import com.desfinmetnum.desafio_final.dto.SistemaLinealRequest;
import com.desfinmetnum.desafio_final.dto.SistemaLinealResponse;
import com.desfinmetnum.desafio_final.dto.SistemaLinealResponse.PasoIteracion;
import org.apache.commons.math3.linear.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Servicio que implementa los cinco métodos para resolver sistemas lineales Ax = b.
 *
 * Aplicación: distribuir productos/carburantes desde plantas a zonas de distribución.
 * La solución x_i representa la cantidad enviada a la zona i.
 *
 * Métodos disponibles:
 *   - Jacobi           → iterativo, usa valores del paso anterior
 *   - Gauss-Seidel     → iterativo, usa valores actualizados en el mismo paso
 *   - SOR              → iterativo, Gauss-Seidel con factor de relajación ω
 *   - LU               → directo, factoriza A = L·U (usa commons-math3)
 *   - Gradiente Conjugado → iterativo, ideal para matrices simétricas definidas positivas
 */
@Service
public class EscenarioAService {

    // ==================== JACOBI ====================

    /**
     * Método de Jacobi:
     *   x_i^(k+1) = (b_i - Σ_{j≠i} a_ij · x_j^(k)) / a_ii
     *
     * En cada iteración, los valores nuevos se calculan SOLO con los valores viejos x^(k).
     * Converge si la matriz es estrictamente diagonalmente dominante.
     */
    public SistemaLinealResponse jacobi(SistemaLinealRequest req) {
        double[][] A = toMatrix(req.getMatriz());
        double[] b = toVector(req.getTerminos());
        int n = b.length;
        double tol = req.getTolerancia();
        int maxIter = req.getMaxIteraciones();

        double[] x = new double[n];       // Vector solución (inicializado en 0)
        double[] xNew = new double[n];    // Nueva aproximación
        List<PasoIteracion> historial = new ArrayList<>();
        boolean convergio = false;
        int iter = 0;

        for (iter = 1; iter <= maxIter; iter++) {
            for (int i = 0; i < n; i++) {
                double suma = 0;
                for (int j = 0; j < n; j++) {
                    if (j != i) suma += A[i][j] * x[j]; // Usa solo valores viejos
                }
                // Despejar x_i de la ecuación i
                xNew[i] = (b[i] - suma) / A[i][i];
            }

            double error = norma(diff(xNew, x));
            System.arraycopy(xNew, 0, x, 0, n); // Actualizar x con los valores nuevos
            historial.add(new PasoIteracion(iter, roundList(xNew), round(error), null));

            if (error < tol) {
                convergio = true;
                break;
            }
        }

        return buildResponse("Jacobi", x, iter, convergio, A, historial);
    }

    // ==================== GAUSS-SEIDEL ====================

    /**
     * Método de Gauss-Seidel:
     *   x_i^(k+1) = (b_i - Σ_{j<i} a_ij · x_j^(k+1) - Σ_{j>i} a_ij · x_j^(k)) / a_ii
     *
     * Usa los valores ya actualizados de la iteración actual para i < j.
     * Generalmente converge el doble de rápido que Jacobi.
     */
    public SistemaLinealResponse gaussSeidel(SistemaLinealRequest req) {
        double[][] A = toMatrix(req.getMatriz());
        double[] b = toVector(req.getTerminos());
        int n = b.length;
        double tol = req.getTolerancia();
        int maxIter = req.getMaxIteraciones();

        double[] x = new double[n];
        List<PasoIteracion> historial = new ArrayList<>();
        boolean convergio = false;
        int iter = 0;

        for (iter = 1; iter <= maxIter; iter++) {
            double[] xOld = Arrays.copyOf(x, n); // Guardar valores antes de la iteración

            for (int i = 0; i < n; i++) {
                double suma = 0;
                for (int j = 0; j < n; j++) {
                    // Para j < i ya está actualizado; para j > i usa el valor viejo
                    if (j != i) suma += A[i][j] * x[j];
                }
                x[i] = (b[i] - suma) / A[i][i];
            }

            double error = norma(diff(x, xOld));
            historial.add(new PasoIteracion(iter, roundList(x), round(error), null));

            if (error < tol) {
                convergio = true;
                break;
            }
        }

        return buildResponse("Gauss-Seidel", x, iter, convergio, A, historial);
    }

    // ==================== SOR ====================

    /**
     * Método SOR (Successive Over-Relaxation):
     *   x_i^(k+1) = (1 - ω) · x_i^(k) + ω · x_i^(GS)
     *
     * Donde x_i^(GS) es el valor que daría Gauss-Seidel.
     * ω = 1 → idéntico a Gauss-Seidel.
     * ω ∈ (1, 2) → sobre-relajación, acelera convergencia.
     * ω ∈ (0, 1) → sub-relajación, estabiliza sistemas difíciles.
     */
    public SistemaLinealResponse sor(SistemaLinealRequest req) {
        double[][] A = toMatrix(req.getMatriz());
        double[] b = toVector(req.getTerminos());
        int n = b.length;
        double tol = req.getTolerancia();
        int maxIter = req.getMaxIteraciones();
        double omega = req.getOmega(); // Factor de relajación

        double[] x = new double[n];
        List<PasoIteracion> historial = new ArrayList<>();
        boolean convergio = false;
        int iter = 0;

        for (iter = 1; iter <= maxIter; iter++) {
            double[] xOld = Arrays.copyOf(x, n);

            for (int i = 0; i < n; i++) {
                double suma = 0;
                for (int j = 0; j < n; j++) {
                    if (j != i) suma += A[i][j] * x[j];
                }
                // Calcular el valor Gauss-Seidel puro
                double xGs = (b[i] - suma) / A[i][i];
                // Aplicar la relajación: combinar el valor viejo con el GS
                x[i] = (1 - omega) * xOld[i] + omega * xGs;
            }

            double error = norma(diff(x, xOld));
            historial.add(new PasoIteracion(iter, roundList(x), round(error), null));

            if (error < tol) {
                convergio = true;
                break;
            }
        }

        SistemaLinealResponse res = buildResponse("SOR (ω=" + omega + ")", x, iter, convergio, A, historial);
        res.setOmega(omega);
        return res;
    }

    // ==================== LU ====================

    /**
     * Descomposición LU (método directo usando Apache Commons Math):
     *   A = L · U   donde L = triangular inferior, U = triangular superior
     *
     * Resuelve el sistema en dos pasos:
     *   1. Ly = b  (sustitución hacia adelante)
     *   2. Ux = y  (sustitución hacia atrás)
     *
     * Ventaja: solución exacta sin iteraciones.
     * Devuelve además las matrices L y U para mostrar en el frontend.
     */
    public SistemaLinealResponse lu(SistemaLinealRequest req) {
        double[][] A = toMatrix(req.getMatriz());
        double[] b = toVector(req.getTerminos());

        // Usar la librería Apache Commons Math3 para la descomposición LU
        RealMatrix matA = new Array2DRowRealMatrix(A);
        LUDecomposition luDecomp = new LUDecomposition(matA);

        if (!luDecomp.getSolver().isNonSingular()) {
            throw new IllegalArgumentException(
                "La matriz es singular (det = 0). No tiene solución única.");
        }

        // Resolver el sistema
        RealVector sol = luDecomp.getSolver().solve(new ArrayRealVector(b));

        // Obtener las matrices L y U para devolver al frontend
        RealMatrix L = luDecomp.getL();
        RealMatrix U = luDecomp.getU();

        SistemaLinealResponse res = buildResponse("LU (Descomposición)", sol.toArray(),
                null, true, A, new ArrayList<>());
        res.setMatrizL(matrixToList(L)); // Matriz triangular inferior
        res.setMatrizU(matrixToList(U)); // Matriz triangular superior
        return res;
    }

    // ==================== GRADIENTE CONJUGADO ====================

    /**
     * Método del Gradiente Conjugado:
     *   Para sistemas donde A es simétrica y definida positiva.
     *
     * Algoritmo:
     *   r₀ = b - A·x₀,  p₀ = r₀
     *   α_k = (rₖᵀrₖ) / (pₖᵀApₖ)         ← paso óptimo
     *   x_{k+1} = x_k + α_k · p_k         ← actualizar solución
     *   r_{k+1} = r_k - α_k · A·p_k       ← actualizar residuo
     *   β_k = (r_{k+1}ᵀr_{k+1}) / (rₖᵀrₖ)
     *   p_{k+1} = r_{k+1} + β_k · p_k     ← nueva dirección conjugada
     */
    public SistemaLinealResponse gradienteConjugado(SistemaLinealRequest req) {
        double[][] A = toMatrix(req.getMatriz());
        double[] b = toVector(req.getTerminos());
        int n = b.length;
        double tol = req.getTolerancia();
        int maxIter = req.getMaxIteraciones();

        double[] x = new double[n];                      // Solución inicial x₀ = 0
        double[] r = diff(b, multiply(A, x));            // Residuo inicial r₀ = b - Ax₀
        double[] p = Arrays.copyOf(r, n);                // Dirección inicial p₀ = r₀
        List<PasoIteracion> historial = new ArrayList<>();
        boolean convergio = false;
        int iter = 0;

        for (iter = 1; iter <= maxIter; iter++) {
            double[] Ap = multiply(A, p);                // A·p_k
            double rDotR = dot(r, r);                    // rₖᵀ·rₖ
            double alpha = rDotR / dot(p, Ap);           // Paso óptimo α_k

            // Actualizar solución: x_{k+1} = x_k + α_k · p_k
            double[] xNew = add(x, scale(p, alpha));

            // Actualizar residuo: r_{k+1} = r_k - α_k · A·p_k
            double[] rNew = diff(r, scale(Ap, alpha));

            double residuoNorma = norma(rNew);            // ||r_{k+1}||
            double error = norma(diff(xNew, x));
            historial.add(new PasoIteracion(iter, roundList(xNew), round(error), round(residuoNorma)));

            x = xNew;

            // Criterio de parada: residuo suficientemente pequeño
            if (residuoNorma < tol) {
                convergio = true;
                r = rNew;
                break;
            }

            // Calcular beta y la nueva dirección conjugada
            double beta = dot(rNew, rNew) / rDotR;       // β_k
            p = add(rNew, scale(p, beta));                // p_{k+1}
            r = rNew;
        }

        return buildResponse("Gradiente Conjugado", x, iter, convergio, A, historial);
    }

    // ==================== UTILIDADES ====================

    /**
     * Construye el objeto de respuesta estándar con interpretación en lenguaje natural.
     */
    private SistemaLinealResponse buildResponse(String metodo, double[] sol, Integer iter,
                                                 boolean convergio, double[][] A,
                                                 List<PasoIteracion> historial) {
        int n = sol.length;
        boolean dd = esDiagonalmenteDominante(A);

        // Generar interpretación del resultado para el contexto del problema
        String[] zonas = {"Norte", "Centro", "Sur", "Este", "Oeste"};
        StringBuilder interp = new StringBuilder("Distribución óptima: ");
        for (int i = 0; i < n; i++) {
            interp.append(round(sol[i])).append(" unidades a Zona ")
                  .append(i < zonas.length ? zonas[i] : "Zona " + (i + 1));
            if (i < n - 1) interp.append(", ");
        }

        SistemaLinealResponse res = new SistemaLinealResponse();
        res.setMetodo(metodo);
        res.setSolucion(roundList(sol));
        res.setIteraciones(iter);
        res.setConvergencia(convergio);
        res.setEsDiagonalmenteDominante(dd);
        res.setAdvertencia(dd ? null :
            "⚠ La matriz NO es diagonalmente dominante. La convergencia no está garantizada.");
        res.setHistorial(historial);
        res.setInterpretacion(interp.toString());
        return res;
    }

    /**
     * Verifica si la matriz A es estrictamente diagonalmente dominante.
     * Condición: |a_ii| > Σ_{j≠i} |a_ij| para toda fila i.
     * Esta condición garantiza convergencia de Jacobi y Gauss-Seidel.
     */
    private boolean esDiagonalmenteDominante(double[][] A) {
        int n = A.length;
        for (int i = 0; i < n; i++) {
            double diag = Math.abs(A[i][i]);
            double suma = 0;
            for (int j = 0; j < n; j++) {
                if (j != i) suma += Math.abs(A[i][j]);
            }
            if (diag < suma) return false;
        }
        return true;
    }

    // --- Operaciones vectoriales y matriciales ---

    private double[][] toMatrix(List<List<Double>> matrix) {
        int n = matrix.size();
        double[][] arr = new double[n][];
        for (int i = 0; i < n; i++) {
            arr[i] = matrix.get(i).stream().mapToDouble(Double::doubleValue).toArray();
        }
        return arr;
    }

    private double[] toVector(List<Double> list) {
        return list.stream().mapToDouble(Double::doubleValue).toArray();
    }

    // Multiplicación matriz-vector: resultado = A · x
    private double[] multiply(double[][] A, double[] x) {
        int n = A.length;
        double[] result = new double[n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                result[i] += A[i][j] * x[j];
        return result;
    }

    // Producto punto: a · b = Σ aᵢ·bᵢ
    private double dot(double[] a, double[] b) {
        double s = 0;
        for (int i = 0; i < a.length; i++) s += a[i] * b[i];
        return s;
    }

    // Diferencia vectorial: a - b
    private double[] diff(double[] a, double[] b) {
        double[] r = new double[a.length];
        for (int i = 0; i < a.length; i++) r[i] = a[i] - b[i];
        return r;
    }

    // Suma vectorial: a + b
    private double[] add(double[] a, double[] b) {
        double[] r = new double[a.length];
        for (int i = 0; i < a.length; i++) r[i] = a[i] + b[i];
        return r;
    }

    // Escalado: s · a
    private double[] scale(double[] a, double s) {
        double[] r = new double[a.length];
        for (int i = 0; i < a.length; i++) r[i] = a[i] * s;
        return r;
    }

    // Norma euclidiana: ||v|| = √(Σvᵢ²)
    private double norma(double[] v) {
        double s = 0;
        for (double d : v) s += d * d;
        return Math.sqrt(s);
    }

    private double round(double v) {
        return Math.round(v * 1000.0) / 1000.0;
    }

    private List<Double> roundList(double[] arr) {
        List<Double> list = new ArrayList<>();
        for (double v : arr) list.add(round(v));
        return list;
    }

    private List<List<Double>> matrixToList(RealMatrix m) {
        List<List<Double>> result = new ArrayList<>();
        for (int i = 0; i < m.getRowDimension(); i++) {
            List<Double> row = new ArrayList<>();
            for (int j = 0; j < m.getColumnDimension(); j++)
                row.add(round(m.getEntry(i, j)));
            result.add(row);
        }
        return result;
    }
}
