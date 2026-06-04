package com.desfinmetnum.desafio_final.escenariod.dto;

import lombok.Data;
import java.util.List;

@Data
public class IntegracionRequest {
    private List<Double> dias;
    private List<Double> precios;
    private double precioBase;
}
