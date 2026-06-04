package com.desfinmetnum.desafio_final.escenariof.dto;

import lombok.Data;
import java.util.List;

@Data
public class PerturbacionRequest {
    private List<List<Double>> matriz;
    private List<Double> terminosOriginales;
    private Double porcentajePerturbacion;
    private String nivelRumor;
}
