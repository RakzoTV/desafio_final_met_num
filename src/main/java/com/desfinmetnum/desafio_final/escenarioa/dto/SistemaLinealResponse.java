package com.desfinmetnum.desafio_final.escenarioa.dto;

import lombok.Data;
import java.util.List;

@Data
public class SistemaLinealResponse {
    private String metodo;
    private List<Double> solucion;
    private Integer iteraciones;
    private boolean convergencia;
    private boolean esDiagonalmenteDominante;
    private String advertencia;
    private Double omega;
    private List<List<Double>> matrizL;
    private List<List<Double>> matrizU;
    private List<PasoIteracion> historial;
    private String interpretacion;

    @Data
    public static class PasoIteracion {
        private int iteracion;
        private List<Double> valores;
        private double error;
        private Double residuo;

        public PasoIteracion(int iteracion, List<Double> valores, double error, Double residuo) {
            this.iteracion = iteracion;
            this.valores = valores;
            this.error = error;
            this.residuo = residuo;
        }
    }
}
