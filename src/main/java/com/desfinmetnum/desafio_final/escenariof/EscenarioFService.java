package com.desfinmetnum.desafio_final.escenariof;

import com.desfinmetnum.desafio_final.escenariof.dto.CondicionamientoRequest;
import com.desfinmetnum.desafio_final.escenariof.dto.CondicionamientoResponse;
import com.desfinmetnum.desafio_final.escenariof.dto.CondicionamientoResponse.EscenarioRumor;
import com.desfinmetnum.desafio_final.escenariof.dto.PerturbacionRequest;
import org.apache.commons.math3.linear.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EscenarioFService {

    public CondicionamientoResponse condicionamiento(CondicionamientoRequest req) {
        double[][] A = toMatrix(req.getMatriz());
        double[] b = toVector(req.getTerminos());

        SingularValueDecomposition svd = new SingularValueDecomposition(new Array2DRowRealMatrix(A));
        double kappa = svd.getConditionNumber();

        LUDecomposition lu = new LUDecomposition(new Array2DRowRealMatrix(A));
        if (!lu.getSolver().isNonSingular()) {
            throw new IllegalArgumentException("La matriz es singular y no tiene solución única.");
        }
        double[] sol = lu.getSolver().solve(new ArrayRealVector(b)).toArray();

        String clasificacion = clasificar(kappa);
        boolean estable = kappa < 100;

        double kappaRedondeado = round(kappa);

        CondicionamientoResponse res = new CondicionamientoResponse();
        res.setMetodo("Análisis de Condicionamiento");
        res.setSolucion(roundList(sol));
        res.setNumeroDCondicion(kappaRedondeado);
        res.setClasificacion(clasificacion);
        res.setEstable(estable);
        res.setInterpretacion(
            "El sistema tiene κ = " + kappaRedondeado + " (" + clasificacion + "). " +
            "Un cambio del 1% en la demanda percibida puede generar un cambio de hasta " +
            round(kappa * 1.0) + "% en la distribución requerida."
        );
        return res;
    }

    public CondicionamientoResponse perturbacion(PerturbacionRequest req) {
        double[][] A = toMatrix(req.getMatriz());
        double[] b = toVector(req.getTerminosOriginales());

        double porcentaje = resolvePorcentaje(req);

        double[] bPert = perturbar(b, porcentaje);

        LUDecomposition lu = new LUDecomposition(new Array2DRowRealMatrix(A));
        if (!lu.getSolver().isNonSingular()) {
            throw new IllegalArgumentException("La matriz es singular y no tiene solución única.");
        }
        DecompositionSolver solver = lu.getSolver();
        double[] solOrig = solver.solve(new ArrayRealVector(b)).toArray();
        double[] solPert = solver.solve(new ArrayRealVector(bPert)).toArray();

        SingularValueDecomposition svd = new SingularValueDecomposition(new Array2DRowRealMatrix(A));
        double kappa = svd.getConditionNumber();

        double cambioTerminos = cambioRelativo(b, bPert);
        double cambioSolucion = cambioRelativo(solOrig, solPert);
        String clasificacion = clasificar(kappa);
        boolean estable = kappa < 100;

        String nivel = req.getNivelRumor() != null ? req.getNivelRumor().toUpperCase() : "PERSONALIZADO";

        CondicionamientoResponse res = new CondicionamientoResponse();
        res.setMetodo("Perturbación de datos");
        res.setNivelRumor(nivel);
        res.setPorcentajePerturbacion(porcentaje);
        res.setSolucionOriginal(roundList(solOrig));
        res.setSolucionPerturbada(roundList(solPert));
        res.setTerminosOriginales(roundList(b));
        res.setTerminosPerturbados(roundList(bPert));
        res.setNumeroDCondicion(round(kappa));
        res.setClasificacion(clasificacion);
        res.setCambioRelativoTerminos(round(cambioTerminos));
        res.setCambioRelativoSolucion(round(cambioSolucion));
        res.setEstable(estable);
        res.setInterpretacion(
            "Rumor " + nivel + " (" + porcentaje + "% de aumento en demanda percibida): " +
            "la distribución cambió un " + round(cambioSolucion) + "%. " +
            "El sistema es " + clasificacion + ": " +
            (estable ? "los rumores tienen impacto proporcional." : "un rumor pequeño colapsa la distribución.")
        );
        return res;
    }

    public CondicionamientoResponse comparacionRumores(CondicionamientoRequest req) {
        double[][] A = toMatrix(req.getMatriz());
        double[] b = toVector(req.getTerminos());

        SingularValueDecomposition svd = new SingularValueDecomposition(new Array2DRowRealMatrix(A));
        double kappa = svd.getConditionNumber();

        LUDecomposition lu = new LUDecomposition(new Array2DRowRealMatrix(A));
        if (!lu.getSolver().isNonSingular()) {
            throw new IllegalArgumentException("La matriz es singular y no tiene solución única.");
        }
        DecompositionSolver solver = lu.getSolver();
        double[] solOrig = solver.solve(new ArrayRealVector(b)).toArray();

        String[][] niveles = {{"BAJO", "1"}, {"MEDIO", "5"}, {"ALTO", "15"}, {"PANICO", "30"}};
        List<EscenarioRumor> escenarios = new ArrayList<>();

        for (String[] nivel : niveles) {
            double pct = Double.parseDouble(nivel[1]);
            double[] bPert = perturbar(b, pct);
            double[] solPert = solver.solve(new ArrayRealVector(bPert)).toArray();
            double cambio = round(cambioRelativo(solOrig, solPert));
            escenarios.add(new EscenarioRumor(nivel[0], pct, roundList(solPert), cambio));
        }

        String clasificacion = clasificar(kappa);

        CondicionamientoResponse res = new CondicionamientoResponse();
        res.setMetodo("Comparación de escenarios de rumor");
        res.setNumeroDCondicion(round(kappa));
        res.setClasificacion(clasificacion);
        res.setEscenarios(escenarios);
        res.setEstable(kappa < 100);
        res.setInterpretacion(
            "Con κ = " + round(kappa) + ", el sistema es " + clasificacion + ". " +
            "El impacto de los rumores es " +
            (kappa < 10 ? "proporcional a su magnitud." : "amplificado por el mal condicionamiento.")
        );
        return res;
    }

    // --- Helpers ---

    private double resolvePorcentaje(PerturbacionRequest req) {
        if (req.getPorcentajePerturbacion() != null) return req.getPorcentajePerturbacion();
        if (req.getNivelRumor() == null) return 5.0;
        return switch (req.getNivelRumor().toUpperCase()) {
            case "BAJO"   -> 1.0;
            case "MEDIO"  -> 5.0;
            case "ALTO"   -> 15.0;
            case "PANICO" -> 30.0;
            default       -> 5.0;
        };
    }

    private double[] perturbar(double[] b, double porcentaje) {
        double[] bPert = new double[b.length];
        for (int i = 0; i < b.length; i++) {
            bPert[i] = b[i] * (1 + porcentaje / 100.0);
        }
        return bPert;
    }

    private double cambioRelativo(double[] original, double[] perturbado) {
        double normOrig = norma(original);
        if (normOrig == 0) return 0;
        double[] delta = new double[original.length];
        for (int i = 0; i < original.length; i++) delta[i] = perturbado[i] - original[i];
        return (norma(delta) / normOrig) * 100.0;
    }

    private String clasificar(double kappa) {
        if (kappa < 10)    return "ESTABLE";
        if (kappa < 100)   return "MODERADO";
        if (kappa < 10000) return "MAL CONDICIONADO";
        return "SINGULAR O CASI SINGULAR";
    }

    private double norma(double[] v) {
        double s = 0;
        for (double d : v) s += d * d;
        return Math.sqrt(s);
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private List<Double> roundList(double[] arr) {
        List<Double> list = new ArrayList<>();
        for (double v : arr) list.add(round(v));
        return list;
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
}
