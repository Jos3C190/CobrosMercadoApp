package com.example.cobrosmercadoapp.data.entity

/**
 * Modelo de datos utilizado para representar el total de cobros agrupados por día.
 *
 * Este objeto no es una entidad de Room; normalmente se emplea como resultado
 * de consultas agregadas (por ejemplo, `GROUP BY`) o para alimentar visualizaciones
 * como gráficos estadísticos dentro de la aplicación.
 *
 * @property dia Día representado en formato `yyyy-MM-dd` o una etiqueta formateada,
 * según la consulta o transformación aplicada.
 * @property total Monto total de cobros acumulados en ese día.
 */
data class CobroPorDia(
    val dia: String,
    val total: Double
)
