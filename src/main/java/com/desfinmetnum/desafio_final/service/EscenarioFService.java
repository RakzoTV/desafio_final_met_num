package com.desfinmetnum.desafio_final.service;

import com.desfinmetnum.desafio_final.dto.CondicionamientoRequest;
import com.desfinmetnum.desafio_final.dto.CondicionamientoResponse;
import com.desfinmetnum.desafio_final.dto.CondicionamientoResponse.EscenarioRumor;
import com.desfinmetnum.desafio_final.dto.PerturbacionRequest;
import org.apache.commons.math3.linear.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para el Escenario F: Rumores de Desabastecimiento y Pánico.
 *
 * Modela cómo los rumores generan perturbaciones en la demanda percibida
 * y cómo el sistema de distribución responde ante esas perturbaciones.
 *
 * Concepto clave — Número de Condición κ(A):
 *   κ(A) = ||A|| · ||A⁻¹||  (calculado via SVD como σ_max / σ_min)
 *
 *   El número de condición indica cuánto se amplifica el error en la solución
 *   ante un error en los datos de entrada:
 *     ||Δx||/||x|| ≤ κ(A) · ||Δb||/||b||
 *
 *   Ejemplo: si κ = 1000 y el rumor aumenta la demanda un 1% (||Δb||/||b|| = 0.01),
 *   la distribución puede cambiar hasta un 1000 × 0.01 = 10%.
 */
@Service
public class EscenarioFService {

    // ==================== ANÁLISIS DE CONDICIONAMIENTO ====================

    /**
     * Calcula el número de condición κ de la matriz del sistema de distribución.
     * Usa Descomposición en Valores Singulares (SVD): κ = σ_max / σ_min.
     *
     * También resuelve el sistema para obtener la distribución actual.
     */
    public CondicionamientoResponse condicionamiento(CondicionamientoRequest req) {
        double[][] A = toMatrix(req.getMatriz());
        double[] b = toVector(req.getTerminos());

        // Calcular el número de condición usando SVD (más estable que el cálculo directo)
        SingularValueDecomposition svd = new SingularValueDecomposition(
                new Array2DRowRealMatrix(A));
        double kappa = svd.getConditionNumber(); // σ_max / σ_min

        // Resolver el sistema con LU para obtener la distribución actual
        LUDecomposition lu = new LUDecomposition(new Array2DRowRealMatrix(A));
        if (!lu.getSolver().isNonSingular()) {
            throw new IllegalArgumentException(
                "La matriz es singular (det ≈ 0). No tiene solución única.");
        }
        double[] sol = lu.getSolver().solve(new ArrayRealVector(b)).toArray();

        String clasificacion = clasificar(kappa);
        boolean estable = kappa < 100;
        double kappaRedondeado = round(kappa);

        CondicionamientoResponse res = new CondicionamientoResponse();
        res.setMetodo("Análisis de Condicionamiento (SVD)");
        res.setSolucion(roundList(sol));
        res.setNumeroDCondicion(kappaRedondeado);
        res.setClasificacion(clasificacion);
        res.setEstable(estable);
        res.setInterpretacion(
            "El sistema tiene κ = " + kappaRedondeado + " (" + clasificacion + "). " +
            "Un rumor que aumente la demanda un 1% puede generar un cambio de hasta " +
            round(kappa * 1.0) + "% en la distribución requerida. " +
            (estable ? "El sistema responde de forma proporcional." :
                      "¡El sistema es MUY sensible a los rumores!")
        );
        return res;
    }

    // ==================== PERTURBACIÓN POR NIVEL DE RUMOR ====================

    /**
     * Simula el efecto de un rumor perturbando el vector b.
     *
     * El rumor aumenta la demanda percibida en un porcentaje:
     *   b_perturbado = b_original × (1 + porcentaje/100)
     *
     * Luego se compara la solución original con la perturbada para ver
     * cuánto cambia la distribución por efecto del rumor.
     */
    public CondicionamientoResponse perturbacion(PerturbacionRequest req) {
        double[][] A = toMatrix(req.getMatriz());
        double[] b = toVector(req.getTerminosOriginales());

        // Determinar el porcentaje de perturbación según el nivel de rumor
        double porcentaje = resolvePorcentaje(req);

        // Perturbar el vector b: simula la demanda artificial generada por el rumor
        double[] bPert = perturbar(b, porcentaje);

        // Resolver ambos sistemas con LU
        LUDecomposition lu = new LUDecomposition(new Array2DRowRealMatrix(A));
        if (!lu.getSolver().isNonSingular()) {
            throw new IllegalArgumentException("La matriz es singular. No tiene solución única.");
        }
        DecompositionSolver solver = lu.getSolver();
        double[] solOrig = solver.solve(new ArrayRealVector(b)).toArray();    // Sin rumor
        double[] solPert = solver.solve(new ArrayRealVector(bPert)).toArray(); // Con rumor

        // Calcular el número de condición
        SingularValueDecomposition svd = new SingularValueDecomposition(
                new Array2DRowRealMatrix(A));
        double kappa = svd.getConditionNumber();

        // Calcular cambios relativos para análisis
        double cambioTerminos = cambioRelativo(b, bPert);
        double cambioSolucion = cambioRelativo(solOrig, solPert);
        String clasificacion = clasificar(kappa);

        String nivel = req.getNivelRumor() != null ?
                req.getNivelRumor().toUpperCase() : "PERSONALIZADO";

        CondicionamientoResponse res = new CondicionamientoResponse();
        res.setMetodo("Perturbación por Rumor");
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
        res.setEstable(kappa < 100);
        res.setInterpretacion(
            "Rumor " + nivel + " (" + porcentaje + "% de aumento en demanda percibida): " +
            "la distribución cambió un " + round(cambioSolucion) + "%. " +
            "El sistema es " + clasificacion + ": " +
            (kappa < 100 ?
                "el rumor tiene impacto proporcional y controlado." :
                "¡un rumor pequeño provoca cambios desproporcionados en la distribución!")
        );
        return res;
    }

    // ==================== COMPARACIÓN MÚLTIPLE DE ESCENARIOS ====================

    /**
     * Compara automáticamente los cuatro niveles de rumor:
     *   BAJO (1%) → MEDIO (5%) → ALTO (15%) → PANICO (30%)
     *
     * Esto permite visualizar la escala del impacto y el comportamiento
     * no lineal de sistemas mal condicionados.
     */
    public CondicionamientoResponse comparacionRumores(CondicionamientoRequest req) {
        double[][] A = toMatrix(req.getMatriz());
        double[] b = toVector(req.getTerminos());

        // Calcular número de condición
        SingularValueDecomposition svd = new SingularValueDecomposition(
                new Array2DRowRealMatrix(A));
        double kappa = svd.getConditionNumber();

        // Resolver sistema original
        LUDecomposition lu = new LUDecomposition(new Array2DRowRealMatrix(A));
        if (!lu.getSolver().isNonSingular()) {
            throw new IllegalArgumentException("La matriz es singular. No tiene solución única.");
        }
        DecompositionSolver solver = lu.getSolver();
        double[] solOrig = solver.solve(new ArrayRealVector(b)).toArray();

        // Definir los cuatro niveles de rumor y su porcentaje de perturbación
        String[][] niveles = {
            {"BAJO",   "1"},   // Un simple rumor sin fundamento
            {"MEDIO",  "5"},   // Rumor con algo de credibilidad
            {"ALTO",   "15"},  // Rumor ampliamente difundido
            {"PANICO", "30"}   // Pánico colectivo de compra
        };

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
        res.setMetodo("Comparación de Escenarios de Rumor");
        res.setSolucion(roundList(solOrig)); // Solución base (sin rumor)
        res.setNumeroDCondicion(round(kappa));
        res.setClasificacion(clasificacion);
        res.setEscenarios(escenarios);
        res.setEstable(kappa < 100);
        res.setInterpretacion(
            "Con κ = " + round(kappa) + ", el sistema es " + clasificacion + ". " +
            "El impacto de los rumores es " +
            (kappa < 10 ?
                "proporcional: los rumores tienen efecto controlado." :
                "amplificado: el mal condicionamiento magnifica el caos generado por los rumores.")
        );
        return res;
    }

    // ==================== UTILIDADES ====================

    /**
     * Determina el porcentaje de perturbación según el nivel de rumor.
     * Si se especifica porcentajePerturbacion, tiene prioridad.
     */
    private double resolvePorcentaje(PerturbacionRequest req) {
        if (req.getPorcentajePerturbacion() != null) return req.getPorcentajePerturbacion();
        if (req.getNivelRumor() == null) return 5.0; // Default: rumor medio
        return switch (req.getNivelRumor().toUpperCase()) {
            case "BAJO"   -> 1.0;
            case "MEDIO"  -> 5.0;
            case "ALTO"   -> 15.0;
            case "PANICO" -> 30.0;
            default       -> 5.0;
        };
    }

    /**
     * Aplica una perturbación porcentual al vector b.
     * Simula el aumento artificial de demanda por efecto del rumor.
     */
    private double[] perturbar(double[] b, double porcentaje) {
        double[] bPert = new double[b.length];
        for (int i = 0; i < b.length; i++) {
            bPert[i] = b[i] * (1 + porcentaje / 100.0);
        }
        return bPert;
    }

    /**
     * Calcula el cambio relativo entre dos vectores en porcentaje.
     * cambioRelativo = (||perturbado - original|| / ||original||) × 100
     */
    private double cambioRelativo(double[] original, double[] perturbado) {
        double normOrig = norma(original);
        if (normOrig == 0) return 0;
        double[] delta = new double[original.length];
        for (int i = 0; i < original.length; i++) delta[i] = perturbado[i] - original[i];
        return (norma(delta) / normOrig) * 100.0;
    }

    /**
     * Clasifica el sistema según su número de condición κ.
     */
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
