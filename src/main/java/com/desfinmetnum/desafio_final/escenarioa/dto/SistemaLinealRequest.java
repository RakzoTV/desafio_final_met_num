package com.desfinmetnum.desafio_final.escenarioa.dto;

import lombok.Data;
import java.util.List;

@Data
public class SistemaLinealRequest {
    private List<List<Double>> matriz;
    private List<Double> terminos;
    private double tolerancia;
    private int maxIteraciones;
    private Double omega;
}
