package com.desfinmetnum.desafio_final.escenariod.dto;

import lombok.Data;
import java.util.List;

@Data
public class IntegracionResponse {
    private String metodo;
    private double costoAcumulado;
    private double costoSinInflacion;
    private double perdidaAdquisitiva;
    private double porcentajePerdida;
    private List<PasoIntegracion> tablaPasos;
    private String interpretacion;

    @Data
    public static class PasoIntegracion {
        private String intervalo;
        private double aporte;

        public PasoIntegracion(String intervalo, double aporte) {
            this.intervalo = intervalo;
            this.aporte = aporte;
        }
    }
}
