# 🧮 Desafío Final — Métodos Numéricos
### *Simulación numérica de abastecimiento, precios y conflicto social en contexto de crisis*

---

## 📖 Descripción

Backend REST desarrollado en **Java + Spring Boot 4** que implementa los principales algoritmos de Métodos Numéricos aplicados a una simulación de crisis socioeconómica. Cada escenario modela un problema real (reservas de carburantes, inflación de precios, distribución de recursos, conflicto social) y lo resuelve con 2 a 5 métodos numéricos distintos para comparar su precisión, velocidad de convergencia y robustez.

---

## 🛠 Tecnologías

| Herramienta | Versión |
|---|---|
| Java | 24 / 25 |
| Spring Boot | 4.0.6 |
| Maven | 3.x |
| Apache Commons Math | 3.6.1 |
| Lombok | Latest |

---

## 🚀 Cómo correr el proyecto

```bash
# 1. Clonar el repositorio
git clone <url-del-repo>
cd desafio_final_met_num

# 2. Correr con Maven Wrapper (no necesita Maven instalado)
./mvnw spring-boot:run
```

El servidor arranca en: **`http://localhost:8080`**

> **Nota:** El proyecto no requiere base de datos. JPA/DataSource están deshabilitados en `application.properties`.

---

## 📁 Estructura del Proyecto

```
src/main/java/com/desfinmetnum/desafio_final/
│
├── config/
│   └── WebConfig.java              ← CORS global habilitado
│
├── controller/                     ← Endpoints REST (uno por escenario)
│   ├── EscenarioAController.java
│   ├── EscenarioBController.java
│   ├── EscenarioCController.java
│   ├── EscenarioDController.java
│   ├── EscenarioEController.java
│   ├── EscenarioFController.java
│   └── EscenarioGController.java
│
├── service/                        ← Lógica matemática / algoritmos
│   ├── EscenarioAService.java
│   ├── EscenarioBService.java
│   ├── EscenarioCService.java
│   ├── EscenarioDService.java
│   ├── EscenarioEService.java
│   ├── EscenarioFService.java
│   └── EscenarioGService.java
│
└── dto/                            ← Clases Request y Response (JSON)
    ├── SistemaLinealRequest/Response
    ├── EscenarioB/C/E/G Request/Response
    ├── IntegracionRequest/Response
    ├── CondicionamientoRequest/Response
    └── PerturbacionRequest
```

---

## 🗺 Mapa Completo de Endpoints

> Todos los endpoints usan `POST` con `Content-Type: application/json`
> Base URL: `http://localhost:8080`

---

### 📦 Escenario A — Distribución Óptima de Recursos
**Resuelve sistemas de ecuaciones lineales `Ax = b`**

| Endpoint | Método | Descripción |
|---|---|---|
| `/api/escenario-a/jacobi` | Jacobi | Iterativo, usa valores del paso anterior |
| `/api/escenario-a/gauss-seidel` | Gauss-Seidel | Iterativo, usa valores actualizados |
| `/api/escenario-a/sor` | SOR | Gauss-Seidel con factor de relajación ω |
| `/api/escenario-a/lu` | LU | Directo, factoriza A = L·U |
| `/api/escenario-a/gradiente-conjugado` | Gradiente Conjugado | Para matrices simétricas definidas positivas |

**JSON de ejemplo:**
```json
{
  "matriz": [[10, 2, 1], [1, 10, 2], [2, 1, 10]],
  "terminos": [14, 16, 14],
  "tolerancia": 0.0001,
  "maxIteraciones": 100,
  "omega": 1.25
}
```
> `omega` solo requerido para `/sor`, ignorado en los demás.

---

### ⛽ Escenario B — Vaciado de Reservas de Carburantes
**EDO: `R'(t) = entrada - consumoInicial*(1 + tasaCrecimiento*t)`**

| Endpoint | Método | Error Global |
|---|---|---|
| `/api/escenario-b/euler` | Euler | O(h) |
| `/api/escenario-b/heun` | Heun (Euler mejorado) | O(h²) |
| `/api/escenario-b/rk4` | Runge-Kutta 4 | O(h⁴) |
| `/api/escenario-b/todos` | Los 3 juntos | — |

**JSON de ejemplo:**
```json
{
  "reservaInicial": 10000,
  "entradaDiaria": 200,
  "consumoInicial": 300,
  "tasaCrecimientoConsumo": 0.03,
  "dias": 30,
  "pasoTiempo": 1.0,
  "nivelCritico": 500
}
```

---

### 📈 Escenario C — Curva Continua de Precios (Interpolación)
**Reconstruye la curva de precios a partir de datos dispersos**

| Endpoint | Método | Característica |
|---|---|---|
| `/api/escenario-c/lagrange` | Lagrange | Polinomio global, puede oscilar |
| `/api/escenario-c/newton` | Newton (Dif. Divididas) | Devuelve tabla de diferencias |
| `/api/escenario-c/splines` | Splines Cúbicos Naturales | La curva más suave |
| `/api/escenario-c/todos` | Los 3 juntos | — |

**JSON de ejemplo:**
```json
{
  "producto": "Papa",
  "diasConocidos": [1, 5, 10, 15, 20, 30],
  "preciosConocidos": [8, 10, 13, 16, 19, 22],
  "diasEvaluar": [3, 7, 12, 18, 25],
  "puntosCurva": 100
}
```

---

### 💸 Escenario D — Pérdida del Poder Adquisitivo (Integración Numérica)
**Calcula el gasto total ∫p(t)dt como área bajo la curva de precios**

| Endpoint | Método | Requisito |
|---|---|---|
| `/api/escenario-d/trapecio` | Regla del Trapecio | Cualquier número de puntos |
| `/api/escenario-d/simpson13` | Simpson 1/3 | Número PAR de intervalos |
| `/api/escenario-d/simpson38` | Simpson 3/8 | Intervalos múltiplo de 3 |

**JSON de ejemplo:**
```json
{
  "producto": "Aceite",
  "dias": [1, 5, 10, 15, 20, 30],
  "precios": [12, 14, 17, 20, 23, 27],
  "precioBase": 12.0
}
```

---

### 🎯 Escenario E — Umbrales Críticos (Búsqueda de Raíces)
**Encuentra x tal que f(x) = 0** (ej: día en que el gasto supera el ingreso)

Tipos de función disponibles: `POLINOMIAL` · `EXPONENCIAL` · `LOGARITMICA` · `TRIGONOMETRICA`

| Endpoint | Método | Convergencia | Requiere |
|---|---|---|---|
| `/api/escenario-e/biseccion` | Bisección | Lineal (p≈1) | Intervalo [a,b] |
| `/api/escenario-e/newton-raphson` | Newton-Raphson | Cuadrática (p≈2) | x₀ inicial |
| `/api/escenario-e/secante` | Secante | Superlineal (p≈1.618) | x₀ y x₁ |
| `/api/escenario-e/grafica` | — | — | Solo puntos f(x) |
| `/api/escenario-e/todos` | Los 3 + gráfica | — | — |

**JSON de ejemplo:**
```json
{
  "tipoFuncion": "EXPONENCIAL",
  "coeficientes": [50, 0.03, 0, -80],
  "intervaloA": 0.1,
  "intervaloB": 30,
  "x0": 10,
  "x1": 20,
  "tolerancia": 0.000001,
  "maxIteraciones": 100,
  "puntosGrafico": 200
}
```

---

### 📢 Escenario F — Rumores de Desabastecimiento y Pánico
**Analiza cómo los rumores perturban el sistema de distribución mediante el número de condición κ(A)**

| Endpoint | Descripción |
|---|---|
| `/api/escenario-f/condicionamiento` | Calcula κ y clasifica la estabilidad del sistema |
| `/api/escenario-f/perturbacion` | Simula el efecto de un nivel de rumor en la distribución |
| `/api/escenario-f/comparacion-rumores` | Compara BAJO (1%), MEDIO (5%), ALTO (15%), PÁNICO (30%) |

**JSON para `/condicionamiento` y `/comparacion-rumores`:**
```json
{
  "matriz": [[10, 2, 1], [1, 10, 2], [2, 1, 10]],
  "terminos": [100, 150, 120]
}
```

**JSON para `/perturbacion`:**
```json
{
  "matriz": [[10, 2, 1], [1, 10, 2], [2, 1, 10]],
  "terminosOriginales": [100, 150, 120],
  "nivelRumor": "ALTO",
  "porcentajePerturbacion": null
}
```
> `nivelRumor`: `BAJO` | `MEDIO` | `ALTO` | `PANICO`
> Si se pasa `porcentajePerturbacion` (número), se ignora `nivelRumor`.

---

### 👥 Escenario G — Dinámica de Conflicto Social (Sistema de 3 EDOs)
**Modelo tipo SIR social:**
```
N'(t) = -a·N·M + b·D     (ciudadanos neutrales)
M'(t) =  a·N·M - c·M·D   (manifestantes activos)
D'(t) =  k·M   - r·D     (mediadores / diálogo)
```

| Endpoint | Método | Error Global |
|---|---|---|
| `/api/escenario-g/heun` | Heun | O(h²) |
| `/api/escenario-g/rk4` | Runge-Kutta 4 | O(h⁴) |
| `/api/escenario-g/todos` | Ambos para comparar | — |

**JSON de ejemplo:**
```json
{
  "n0": 800,
  "m0": 150,
  "d0": 50,
  "paramA": 0.0003,
  "paramB": 0.05,
  "paramC": 0.001,
  "paramK": 0.04,
  "paramR": 0.03,
  "tiempoFinal": 60,
  "pasoTiempo": 0.5
}
```

**Parámetros del modelo G:**

| Parámetro | Significado | Rango típico |
|---|---|---|
| `n0, m0, d0` | Condiciones iniciales (personas) | > 0 |
| `paramA` | Tasa de contagio del descontento | 0.0001 – 0.001 |
| `paramB` | Retorno a la neutralidad (por mediadores) | 0.01 – 0.1 |
| `paramC` | Efectividad del diálogo | 0.0001 – 0.01 |
| `paramK` | Reacción institucional (generación de mediadores) | 0.01 – 0.1 |
| `paramR` | Desgaste de los mediadores | 0.01 – 0.1 |

---

## 🧪 Resumen de todos los endpoints

| # | Escenario | Endpoint | Algoritmo |
|---|---|---|---|
| 1 | A | `/api/escenario-a/jacobi` | Jacobi |
| 2 | A | `/api/escenario-a/gauss-seidel` | Gauss-Seidel |
| 3 | A | `/api/escenario-a/sor` | SOR |
| 4 | A | `/api/escenario-a/lu` | LU |
| 5 | A | `/api/escenario-a/gradiente-conjugado` | Gradiente Conjugado |
| 6 | B | `/api/escenario-b/euler` | Euler |
| 7 | B | `/api/escenario-b/heun` | Heun |
| 8 | B | `/api/escenario-b/rk4` | RK4 |
| 9 | B | `/api/escenario-b/todos` | Los 3 juntos |
| 10 | C | `/api/escenario-c/lagrange` | Lagrange |
| 11 | C | `/api/escenario-c/newton` | Newton |
| 12 | C | `/api/escenario-c/splines` | Splines Cúbicos |
| 13 | C | `/api/escenario-c/todos` | Los 3 juntos |
| 14 | D | `/api/escenario-d/trapecio` | Trapecio |
| 15 | D | `/api/escenario-d/simpson13` | Simpson 1/3 |
| 16 | D | `/api/escenario-d/simpson38` | Simpson 3/8 |
| 17 | E | `/api/escenario-e/biseccion` | Bisección |
| 18 | E | `/api/escenario-e/newton-raphson` | Newton-Raphson |
| 19 | E | `/api/escenario-e/secante` | Secante |
| 20 | E | `/api/escenario-e/grafica` | Gráfica de f(x) |
| 21 | E | `/api/escenario-e/todos` | Los 3 + gráfica |
| 22 | F | `/api/escenario-f/condicionamiento` | Número de condición κ |
| 23 | F | `/api/escenario-f/perturbacion` | Efecto de rumor |
| 24 | F | `/api/escenario-f/comparacion-rumores` | 4 niveles de rumor |
| 25 | G | `/api/escenario-g/heun` | Heun |
| 26 | G | `/api/escenario-g/rk4` | RK4 |
| 27 | G | `/api/escenario-g/todos` | Ambos juntos |

**Total: 27 endpoints**

---

## 🌐 CORS

CORS está habilitado globalmente para todos los orígenes (`*`), lo que permite conectar cualquier frontend (React, Vue, Angular, HTML puro) sin configuración adicional.

---

## 👤 Autor

Proyecto académico — Desafío Final de Métodos Numéricos  
Carrera de Ingeniería en Sistemas / Informática  
Semestre 7 · 2026
