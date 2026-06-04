package com.desfinmetnum.desafio_final.escenariof.dto;

import lombok.Data;
import java.util.List;

@Data
public class CondicionamientoResponse {
    private String metodo;
    private List<Double> solucion;
    private double numeroDCondicion;
    private String clasificacion;
    private boolean estable;
    private String interpretacion;

    // campos para perturbacion
    private String nivelRumor;
    private Double porcentajePerturbacion;
    private List<Double> solucionOriginal;
    private List<Double> solucionPerturbada;
    private List<Double> terminosOriginales;
    private List<Double> terminosPerturbados;
    private Double cambioRelativoTerminos;
    private Double cambioRelativoSolucion;

    // campo para comparacion-rumores
    private List<EscenarioRumor> escenarios;

    @Data
    public static class EscenarioRumor {
        private String nivel;
        private double perturbacion;
        private List<Double> solucion;
        private double cambioRelativo;

        public EscenarioRumor(String nivel, double perturbacion, List<Double> solucion, double cambioRelativo) {
            this.nivel = nivel;
            this.perturbacion = perturbacion;
            this.solucion = solucion;
            this.cambioRelativo = cambioRelativo;
        }
    }
}
