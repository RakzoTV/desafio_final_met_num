package com.desfinmetnum.desafio_final.escenariod;

import com.desfinmetnum.desafio_final.escenariod.dto.IntegracionRequest;
import com.desfinmetnum.desafio_final.escenariod.dto.IntegracionResponse;
import com.desfinmetnum.desafio_final.escenariod.dto.IntegracionResponse.PasoIntegracion;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EscenarioDService {

    public IntegracionResponse trapecio(IntegracionRequest req) {
        List<Double> dias = req.getDias();
        List<Double> precios = req.getPrecios();
        int n = dias.size() - 1;

        List<PasoIntegracion> pasos = new ArrayList<>();
        double total = 0.0;

        for (int i = 0; i < n; i++) {
            double h = dias.get(i + 1) - dias.get(i);
            double aporte = h * (precios.get(i) + precios.get(i + 1)) / 2.0;
            total += aporte;
            String intervalo = "[" + dias.get(i).intValue() + ", " + dias.get(i + 1).intValue() + "]";
            pasos.add(new PasoIntegracion(intervalo, Math.round(aporte * 100.0) / 100.0));
        }

        return buildResponse("Trapecio", total, req.getPrecioBase(), dias, pasos);
    }

    public IntegracionResponse simpson13(IntegracionRequest req) {
        List<Double> dias = req.getDias();
        List<Double> precios = req.getPrecios();
        int n = dias.size() - 1;

        if (n % 2 != 0) {
            throw new IllegalArgumentException(
                "Simpson 1/3 requiere un número par de intervalos. Agregue o quite un punto de datos."
            );
        }

        List<PasoIntegracion> pasos = new ArrayList<>();
        double total = 0.0;

        for (int i = 0; i < n; i += 2) {
            double x0 = dias.get(i), x2 = dias.get(i + 2);
            double f0 = precios.get(i), f1 = precios.get(i + 1), f2 = precios.get(i + 2);
            double h = (x2 - x0) / 2.0;
            double aporte = (h / 3.0) * (f0 + 4 * f1 + f2);
            total += aporte;
            String intervalo = "[" + (int) x0 + ", " + (int) x2 + "]";
            pasos.add(new PasoIntegracion(intervalo, Math.round(aporte * 100.0) / 100.0));
        }

        return buildResponse("Simpson 1/3", total, req.getPrecioBase(), dias, pasos);
    }

    public IntegracionResponse simpson38(IntegracionRequest req) {
        List<Double> dias = req.getDias();
        List<Double> precios = req.getPrecios();
        int n = dias.size() - 1;

        if (n % 3 != 0) {
            throw new IllegalArgumentException(
                "Simpson 3/8 requiere que el número de intervalos sea múltiplo de 3."
            );
        }

        List<PasoIntegracion> pasos = new ArrayList<>();
        double total = 0.0;

        for (int i = 0; i < n; i += 3) {
            double x0 = dias.get(i), x3 = dias.get(i + 3);
            double f0 = precios.get(i), f1 = precios.get(i + 1),
                   f2 = precios.get(i + 2), f3 = precios.get(i + 3);
            double h = (x3 - x0) / 3.0;
            double aporte = (3.0 * h / 8.0) * (f0 + 3 * f1 + 3 * f2 + f3);
            total += aporte;
            String intervalo = "[" + (int) x0 + ", " + (int) x3 + "]";
            pasos.add(new PasoIntegracion(intervalo, Math.round(aporte * 100.0) / 100.0));
        }

        return buildResponse("Simpson 3/8", total, req.getPrecioBase(), dias, pasos);
    }

    private IntegracionResponse buildResponse(String metodo, double costoAcumulado,
                                               double precioBase, List<Double> dias,
                                               List<PasoIntegracion> pasos) {
        double duracion = dias.get(dias.size() - 1) - dias.get(0);
        double costoSinInflacion = precioBase * duracion;
        double perdida = costoAcumulado - costoSinInflacion;
        double porcentaje = (perdida / costoSinInflacion) * 100.0;

        costoAcumulado = Math.round(costoAcumulado * 100.0) / 100.0;
        costoSinInflacion = Math.round(costoSinInflacion * 100.0) / 100.0;
        perdida = Math.round(perdida * 100.0) / 100.0;
        porcentaje = Math.round(porcentaje * 10.0) / 10.0;

        IntegracionResponse res = new IntegracionResponse();
        res.setMetodo(metodo);
        res.setCostoAcumulado(costoAcumulado);
        res.setCostoSinInflacion(costoSinInflacion);
        res.setPerdidaAdquisitiva(perdida);
        res.setPorcentajePerdida(porcentaje);
        res.setTablaPasos(pasos);
        res.setInterpretacion(
            "La familia gastó " + costoAcumulado + " Bs en el mes. " +
            "Sin inflación hubiera gastado " + costoSinInflacion + " Bs. " +
            "Pérdida: " + perdida + " Bs (" + porcentaje + "%)"
        );
        return res;
    }
}
