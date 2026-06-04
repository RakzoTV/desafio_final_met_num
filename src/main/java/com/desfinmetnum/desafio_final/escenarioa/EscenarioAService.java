package com.desfinmetnum.desafio_final.escenarioa;

import com.desfinmetnum.desafio_final.escenarioa.dto.SistemaLinealRequest;
import com.desfinmetnum.desafio_final.escenarioa.dto.SistemaLinealResponse;
import com.desfinmetnum.desafio_final.escenarioa.dto.SistemaLinealResponse.PasoIteracion;
import org.apache.commons.math3.linear.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class EscenarioAService {

    public SistemaLinealResponse jacobi(SistemaLinealRequest req) {
        double[][] A = toMatrix(req.getMatriz());
        double[] b = toVector(req.getTerminos());
        int n = b.length;
        double tol = req.getTolerancia();
        int maxIter = req.getMaxIteraciones();

        double[] x = new double[n];
        double[] xNew = new double[n];
        List<PasoIteracion> historial = new ArrayList<>();

        boolean convergio = false;
        int iter = 0;

        for (iter = 1; iter <= maxIter; iter++) {
            for (int i = 0; i < n; i++) {
                double suma = 0;
                for (int j = 0; j < n; j++) {
                    if (j != i) suma += A[i][j] * x[j];
                }
                xNew[i] = (b[i] - suma) / A[i][i];
            }

            double error = norma(diff(xNew, x));
            System.arraycopy(xNew, 0, x, 0, n);
            historial.add(new PasoIteracion(iter, roundList(xNew), round(error), null));

            if (error < tol) {
                convergio = true;
                break;
            }
        }

        return buildResponse("Jacobi", x, iter, convergio, A, historial, null);
    }

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
            double[] xOld = Arrays.copyOf(x, n);

            for (int i = 0; i < n; i++) {
                double suma = 0;
                for (int j = 0; j < n; j++) {
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

        return buildResponse("Gauss-Seidel", x, iter, convergio, A, historial, null);
    }

    public SistemaLinealResponse sor(SistemaLinealRequest req) {
        double[][] A = toMatrix(req.getMatriz());
        double[] b = toVector(req.getTerminos());
        int n = b.length;
        double tol = req.getTolerancia();
        int maxIter = req.getMaxIteraciones();
        double omega = req.getOmega();

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
                double xGs = (b[i] - suma) / A[i][i];
                x[i] = (1 - omega) * xOld[i] + omega * xGs;
            }

            double error = norma(diff(x, xOld));
            historial.add(new PasoIteracion(iter, roundList(x), round(error), null));

            if (error < tol) {
                convergio = true;
                break;
            }
        }

        SistemaLinealResponse res = buildResponse("SOR", x, iter, convergio, A, historial, null);
        res.setOmega(omega);
        return res;
    }

    public SistemaLinealResponse lu(SistemaLinealRequest req) {
        double[][] A = toMatrix(req.getMatriz());
        double[] b = toVector(req.getTerminos());

        RealMatrix matA = new Array2DRowRealMatrix(A);
        LUDecomposition lu = new LUDecomposition(matA);

        if (!lu.getSolver().isNonSingular()) {
            throw new IllegalArgumentException("La matriz es singular y no tiene solución única.");
        }

        RealVector sol = lu.getSolver().solve(new ArrayRealVector(b));
        RealMatrix L = lu.getL();
        RealMatrix U = lu.getU();

        SistemaLinealResponse res = buildResponse("LU", sol.toArray(), null, true,
                A, new ArrayList<>(), null);
        res.setMatrizL(matrixToList(L));
        res.setMatrizU(matrixToList(U));
        return res;
    }

    public SistemaLinealResponse gradienteConjugado(SistemaLinealRequest req) {
        double[][] A = toMatrix(req.getMatriz());
        double[] b = toVector(req.getTerminos());
        int n = b.length;
        double tol = req.getTolerancia();
        int maxIter = req.getMaxIteraciones();

        double[] x = new double[n];
        double[] r = diff(b, multiply(A, x));
        double[] p = Arrays.copyOf(r, n);
        List<PasoIteracion> historial = new ArrayList<>();

        boolean convergio = false;
        int iter = 0;

        for (iter = 1; iter <= maxIter; iter++) {
            double[] Ap = multiply(A, p);
            double rDotR = dot(r, r);
            double alpha = rDotR / dot(p, Ap);

            double[] xNew = add(x, scale(p, alpha));
            double[] rNew = diff(r, scale(Ap, alpha));

            double residuoNorma = norma(rNew);
            double error = norma(diff(xNew, x));
            historial.add(new PasoIteracion(iter, roundList(xNew), round(error), round(residuoNorma)));

            x = xNew;

            if (residuoNorma < tol) {
                convergio = true;
                r = rNew;
                break;
            }

            double beta = dot(rNew, rNew) / rDotR;
            p = add(rNew, scale(p, beta));
            r = rNew;
        }

        return buildResponse("Gradiente Conjugado", x, iter, convergio, A, historial, null);
    }

    // --- Helpers ---

    private SistemaLinealResponse buildResponse(String metodo, double[] sol, Integer iter,
                                                 boolean convergio, double[][] A,
                                                 List<PasoIteracion> historial, String extra) {
        int n = sol.length;
        boolean dd = esDiagonalmenteDominante(A);

        String[] zonas = {"Norte", "Centro", "Sur", "Este", "Oeste"};
        StringBuilder interp = new StringBuilder("Distribuir ");
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
            "La matriz no es diagonalmente dominante. La convergencia no está garantizada.");
        res.setHistorial(historial);
        res.setInterpretacion(interp.toString());
        return res;
    }

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

    private double[] multiply(double[][] A, double[] x) {
        int n = A.length;
        double[] result = new double[n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                result[i] += A[i][j] * x[j];
        return result;
    }

    private double dot(double[] a, double[] b) {
        double s = 0;
        for (int i = 0; i < a.length; i++) s += a[i] * b[i];
        return s;
    }

    private double[] diff(double[] a, double[] b) {
        double[] r = new double[a.length];
        for (int i = 0; i < a.length; i++) r[i] = a[i] - b[i];
        return r;
    }

    private double[] add(double[] a, double[] b) {
        double[] r = new double[a.length];
        for (int i = 0; i < a.length; i++) r[i] = a[i] + b[i];
        return r;
    }

    private double[] scale(double[] a, double s) {
        double[] r = new double[a.length];
        for (int i = 0; i < a.length; i++) r[i] = a[i] * s;
        return r;
    }

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
