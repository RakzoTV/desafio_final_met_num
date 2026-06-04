package com.desfinmetnum.desafio_final.service;

import com.desfinmetnum.desafio_final.dto.IntegracionRequest;
import com.desfinmetnum.desafio_final.dto.IntegracionResponse;
import com.desfinmetnum.desafio_final.dto.IntegracionResponse.PasoIntegracion;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio que implementa los tres métodos de integración numérica
 * para calcular el costo acumulado familiar y la pérdida del poder adquisitivo.
 *
 * Idea: si p(t) es el precio diario de un producto, entonces el gasto
 * total en el período [a, b] es la integral ∫ p(t) dt.
 * Los métodos numéricos aproximan esta integral con los datos discretos disponibles.
 *
 * Métodos:
 *   - Trapecio  → O(h²) de error, funciona con cualquier espaciado
 *   - Simpson 1/3 → O(h⁴), más preciso, requiere n par de intervalos
 *   - Simpson 3/8 → O(h⁴), requiere intervalos múltiplos de 3
 */
@Service
public class EscenarioDService {

    // ==================== REGLA DEL TRAPECIO ====================

    /**
     * Regla del Trapecio:
     *   ∫ f(x)dx ≈ Σᵢ (hᵢ/2) * (f(xᵢ) + f(xᵢ₊₁))
     *
     * Aproxima el área bajo la curva como suma de trapecios.
     * Funciona con intervalos irregulares (días no equiespaciados).
     * Error: O(h²) — suficientemente preciso para datos mensuales.
     */
    public IntegracionResponse trapecio(IntegracionRequest req) {
        List<Double> dias   = req.getDias();
        List<Double> precios = req.getPrecios();
        int n = dias.size() - 1; // Número de intervalos

        List<PasoIntegracion> pasos = new ArrayList<>();
        double total = 0.0;

        for (int i = 0; i < n; i++) {
            // Ancho del intervalo (puede ser irregular)
            double h = dias.get(i + 1) - dias.get(i);

            // Área del trapecio: h * (f_i + f_{i+1}) / 2
            double aporte = h * (precios.get(i) + precios.get(i + 1)) / 2.0;
            total += aporte;

            String intervalo = "[" + dias.get(i).intValue() + ", " + dias.get(i + 1).intValue() + "]";
            pasos.add(new PasoIntegracion(intervalo, Math.round(aporte * 100.0) / 100.0));
        }

        return buildResponse("Trapecio", total, req, dias, pasos);
    }

    // ==================== SIMPSON 1/3 ====================

    /**
     * Regla de Simpson 1/3:
     *   Para cada grupo de 3 puntos [x₀, x₁, x₂]:
     *   ∫ f(x)dx ≈ (h/3) * (f₀ + 4f₁ + f₂)
     *
     * Usa un polinomio de segundo grado para aproximar f en cada intervalo doble.
     * Mucho más preciso que el Trapecio: error O(h⁴).
     * RESTRICCIÓN: número par de intervalos (número impar de puntos).
     */
    public IntegracionResponse simpson13(IntegracionRequest req) {
        List<Double> dias   = req.getDias();
        List<Double> precios = req.getPrecios();
        int n = dias.size() - 1; // Número de intervalos

        // Verificar que el número de intervalos sea par
        if (n % 2 != 0) {
            throw new IllegalArgumentException(
                "Simpson 1/3 requiere un número PAR de intervalos (número IMPAR de puntos). " +
                "Actualmente tienes " + dias.size() + " puntos (" + n + " intervalos). " +
                "Agrega o quita un punto de datos.");
        }

        List<PasoIntegracion> pasos = new ArrayList<>();
        double total = 0.0;

        // Procesar grupos de dos intervalos (tres puntos)
        for (int i = 0; i < n; i += 2) {
            double x0 = dias.get(i), x2 = dias.get(i + 2);
            double f0 = precios.get(i), f1 = precios.get(i + 1), f2 = precios.get(i + 2);

            // h = semiancho del par de intervalos
            double h = (x2 - x0) / 2.0;

            // Fórmula de Simpson 1/3: (h/3) * (f0 + 4f1 + f2)
            double aporte = (h / 3.0) * (f0 + 4 * f1 + f2);
            total += aporte;

            String intervalo = "[" + (int) x0 + ", " + (int) x2 + "]";
            pasos.add(new PasoIntegracion(intervalo, Math.round(aporte * 100.0) / 100.0));
        }

        return buildResponse("Simpson 1/3", total, req, dias, pasos);
    }

    // ==================== SIMPSON 3/8 ====================

    /**
     * Regla de Simpson 3/8:
     *   Para cada grupo de 4 puntos [x₀, x₁, x₂, x₃]:
     *   ∫ f(x)dx ≈ (3h/8) * (f₀ + 3f₁ + 3f₂ + f₃)
     *
     * Usa un polinomio de tercer grado.
     * Error O(h⁴), similar a Simpson 1/3 pero con más puntos por grupo.
     * RESTRICCIÓN: número de intervalos múltiplo de 3.
     */
    public IntegracionResponse simpson38(IntegracionRequest req) {
        List<Double> dias   = req.getDias();
        List<Double> precios = req.getPrecios();
        int n = dias.size() - 1; // Número de intervalos

        // Verificar que el número de intervalos sea múltiplo de 3
        if (n % 3 != 0) {
            throw new IllegalArgumentException(
                "Simpson 3/8 requiere que el número de intervalos sea MÚLTIPLO DE 3. " +
                "Actualmente tienes " + n + " intervalos. " +
                "Ajusta el número de puntos para que sea 4, 7, 10, 13...");
        }

        List<PasoIntegracion> pasos = new ArrayList<>();
        double total = 0.0;

        // Procesar grupos de tres intervalos (cuatro puntos)
        for (int i = 0; i < n; i += 3) {
            double x0 = dias.get(i), x3 = dias.get(i + 3);
            double f0 = precios.get(i),   f1 = precios.get(i + 1),
                   f2 = precios.get(i + 2), f3 = precios.get(i + 3);

            // h = ancho de cada subintervalo
            double h = (x3 - x0) / 3.0;

            // Fórmula de Simpson 3/8: (3h/8) * (f0 + 3f1 + 3f2 + f3)
            double aporte = (3.0 * h / 8.0) * (f0 + 3 * f1 + 3 * f2 + f3);
            total += aporte;

            String intervalo = "[" + (int) x0 + ", " + (int) x3 + "]";
            pasos.add(new PasoIntegracion(intervalo, Math.round(aporte * 100.0) / 100.0));
        }

        return buildResponse("Simpson 3/8", total, req, dias, pasos);
    }

    // ==================== UTILIDADES ====================

    /**
     * Construye la respuesta con el cálculo de la pérdida adquisitiva.
     *
     * @param costoAcumulado integral calculada (gasto real con inflación)
     * @param req            datos originales del request
     * @param dias           lista de días
     * @param pasos          desglose por intervalos
     */
    private IntegracionResponse buildResponse(String metodo, double costoAcumulado,
                                               IntegracionRequest req, List<Double> dias,
                                               List<PasoIntegracion> pasos) {
        // Duración del período en días
        double duracion = dias.get(dias.size() - 1) - dias.get(0);

        // Costo sin inflación: si el precio se hubiera mantenido constante en precioBase
        double costoSinInflacion = req.getPrecioBase() * duracion;

        // Pérdida = lo que se gastó de más por la inflación
        double perdida = costoAcumulado - costoSinInflacion;

        // Porcentaje de pérdida respecto al costo base
        double porcentaje = costoSinInflacion > 0 ? (perdida / costoSinInflacion) * 100.0 : 0;

        // Redondear para presentación
        costoAcumulado   = Math.round(costoAcumulado   * 100.0) / 100.0;
        costoSinInflacion = Math.round(costoSinInflacion * 100.0) / 100.0;
        perdida           = Math.round(perdida           * 100.0) / 100.0;
        porcentaje        = Math.round(porcentaje        * 10.0)  / 10.0;

        IntegracionResponse res = new IntegracionResponse();
        res.setMetodo(metodo);
        res.setProducto(req.getProducto() != null ? req.getProducto() : "Producto");
        res.setCostoAcumulado(costoAcumulado);
        res.setCostoSinInflacion(costoSinInflacion);
        res.setPerdidaAdquisitiva(perdida);
        res.setPorcentajePerdida(porcentaje);
        res.setTablaPasos(pasos);
        res.setInterpretacion(
            "La familia gastó " + costoAcumulado + " Bs en el período. " +
            "Sin inflación hubiera gastado " + costoSinInflacion + " Bs. " +
            "Pérdida del poder adquisitivo: " + perdida + " Bs (" + porcentaje + "% más caro)."
        );
        return res;
    }
}
