package com.example.cobrosmercadoapp.ui.analytics

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.cobrosmercadoapp.R
import com.example.cobrosmercadoapp.data.entity.CobroDetalle
import com.example.cobrosmercadoapp.data.entity.CobroPorDia
import com.example.cobrosmercadoapp.data.entity.Comerciante
import com.example.cobrosmercadoapp.data.entity.Puesto
import com.example.cobrosmercadoapp.data.repository.AppRepository
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

/**
 * Pantalla principal de análisis financiero del mercado.
 *
 * Muestra métricas globales del sistema (no filtradas por usuario): ingresos diarios, semanales,
 * mensuales, acumulados, comparativas interanuales (YoY), tendencias mensuales, ranking de puestos,
 * distribución por comerciante y gráficos asociados.
 *
 * Todos los cálculos se realizan a partir de los datos completos almacenados en [AppRepository].
 *
 * @param navController Controlador de navegación para posibles redirecciones.
 * @param context Contexto de la aplicación, usado para acceder a SharedPreferences.
 * @param repository Repositorio que provee acceso a los datos persistentes de cobros, puestos y comerciantes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController: NavController,
    context: Context,
    repository: AppRepository
) {
    val scope = rememberCoroutineScope()
    val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    val usuarioLogin = sharedPref.getString("usuario_login", "") ?: ""

    // Datos globales (todos los usuarios)
    val cobros by repository.getAllCobrosDetalle().collectAsStateWithLifecycle(emptyList())
    val puestos by repository.getAllPuestos().collectAsStateWithLifecycle(emptyList<Puesto>())
    val comerciantes by repository.getAllComerciantes().collectAsStateWithLifecycle(emptyList<Comerciante>())

    val formato = NumberFormat.getCurrencyInstance(Locale("es", "SV")).apply { maximumFractionDigits = 0 }
    val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val mesActual = hoy.substring(0, 7)

    // --- Cálculo de rango semanal (lunes a domingo) ---
    val calendar = Calendar.getInstance()
    val diaSemana = calendar.get(Calendar.DAY_OF_WEEK)
    val diasHastaLunes = if (diaSemana == Calendar.SUNDAY) 6 else diaSemana - 2
    calendar.add(Calendar.DAY_OF_YEAR, -diasHastaLunes)
    val semanaInicio = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    calendar.add(Calendar.DAY_OF_YEAR, 6)
    val semanaFin = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

    // --- Métricas globales ---
    val totalHoy = cobros.filter { it.cobro.fecha_cobro == hoy }.sumOf { it.cobro.monto_cobrado }
    val totalSemanal = cobros.filter { it.cobro.fecha_cobro >= semanaInicio && it.cobro.fecha_cobro <= semanaFin }
        .sumOf { it.cobro.monto_cobrado }
    val totalMes = cobros.filter { it.cobro.fecha_cobro.startsWith(mesActual) }.sumOf { it.cobro.monto_cobrado }
    val totalAcumulado = cobros.sumOf { it.cobro.monto_cobrado }
    val diasConCobros = cobros.map { it.cobro.fecha_cobro }.toSet().size
    val promedioDiario = if (diasConCobros > 0) totalAcumulado / diasConCobros else 0.0

    val cantidadPuestos = puestos.size
    val cantidadComerciantes = comerciantes.size

    // --- Comparativa Year-over-Year (mismo mes del año anterior) ---
    val añoActual = mesActual.substring(0, 4).toInt()
    val mes = mesActual.substring(5, 7)
    val mesAnterior = "${añoActual - 1}-$mes"
    val totalMesAnterior = cobros.filter { it.cobro.fecha_cobro.startsWith(mesAnterior) }
        .sumOf { it.cobro.monto_cobrado }
    val yoyDelta = if (totalMesAnterior > 0) ((totalMes - totalMesAnterior) / totalMesAnterior * 100).roundToInt() else 0
    val yoyColor = if (yoyDelta >= 0) Color(0xFF00C853) else Color(0xFFE91E63)

    // --- Tendencia últimos 6 meses ---
    val meses = cobros.groupBy { it.cobro.fecha_cobro.substring(0, 7) }
        .map { (mes, lista) -> mes to lista.sumOf { it.cobro.monto_cobrado } }
        .sortedBy { it.first }
        .takeLast(6)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFDDE8F6), Color(0xFFFDFDFD))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Análisis de Cobros",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Resumen financiero",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Resúmenes rápidos
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummarySmall("Hoy", totalHoy, formato, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                        SummarySmall("Semana", totalSemanal, formato, Color(0xFF2196F3), Modifier.weight(1f))
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummarySmall("Mes", totalMes, formato, Color(0xFFFF9800), Modifier.weight(1f))
                        SummarySmall("Acum.", totalAcumulado, formato, Color(0xFFE91E63), Modifier.weight(1f))
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CountSmall("Puestos", cantidadPuestos, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                        CountSmall("Comerciantes", cantidadComerciantes, Color(0xFFFFC107), Modifier.weight(1f))
                    }
                }

                // YoY
                item {
                    SmallCard(title = "Comparación YoY (Mes)", iconRes = R.drawable.ic_compare) {
                        Text(
                            text = "${formato.format(totalMes)} vs. ${formato.format(totalMesAnterior)} anterior",
                            color = yoyColor,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Delta: $yoyDelta%",
                            color = yoyColor,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Últimos 7 días
                item {
                    val ultimos7Dias = getLast7Days()
                    val cobrosPorDia = ultimos7Dias.map { fecha ->
                        val total = cobros.filter { it.cobro.fecha_cobro == fecha }.sumOf { it.cobro.monto_cobrado }
                        CobroPorDia(dia = fecha.substring(8, 10), total = total)
                    }

                    if (cobrosPorDia.any { it.total > 0 }) {
                        SmallCard(title = "Últimos 7 días", iconRes = R.drawable.ic_bar_chart) {
                            BarChart(
                                data = cobrosPorDia,
                                maxValue = cobrosPorDia.maxOf { it.total },
                                formato = formato
                            )
                        }
                    }
                }

                // Tendencia mensual
                item {
                    if (meses.size >= 2) {
                        SmallCard(title = "Tendencia Mensual (Últimos 6 Meses)", iconRes = R.drawable.ic_trending_up) {
                            LineChart(data = meses.map { it.second.toFloat() })
                        }
                    } else {
                        SmallCard(title = "Tendencia Mensual (Últimos 6 Meses)", iconRes = R.drawable.ic_trending_up) {
                            Text(
                                text = "Insuficientes datos para mostrar tendencia (necesitas cobros en al menos 2 meses diferentes)",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Top 5 Puestos
                item {
                    val topPuestos = cobros
                        .groupBy { it.puesto.numero_puesto }
                        .map { (puesto, lista) -> puesto to lista.sumOf { it.cobro.monto_cobrado } }
                        .sortedByDescending { it.second }
                        .take(5)

                    if (topPuestos.isNotEmpty()) {
                        SmallCard(title = "Top 5 Puestos", iconRes = R.drawable.ic_trophy) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                topPuestos.forEachIndexed { index, (puesto, total) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${index + 1}",
                                                fontWeight = FontWeight.Bold,
                                                color = if (index == 0) Color(0xFFFFD700) else Color(0xFF9E9E9E),
                                                fontSize = 14.sp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Puesto #$puesto", fontWeight = FontWeight.Normal)
                                        }
                                        Text(formato.format(total), fontWeight = FontWeight.Bold, color = Color(0xFF00C853))
                                    }
                                }
                            }
                        }
                    }
                }

                // Distribución por comerciante
                item {
                    val porComerciante = cobros
                        .filter { it.comerciante != null }
                        .groupBy { it.comerciante!!.nombre_comerciante }
                        .map { (nombre, lista) -> nombre to lista.sumOf { it.cobro.monto_cobrado } }
                        .sortedByDescending { it.second }

                    if (porComerciante.isNotEmpty()) {
                        SmallCard(title = "Cobros por Comerciante", iconRes = R.drawable.ic_pie_chart) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                PieChart(data = porComerciante, formato = formato)
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}

/**
 * Tarjeta compacta que muestra un monto con título y color distintivo.
 *
 * @param title Título descriptivo (ej. "Hoy", "Mes").
 * @param amount Monto a mostrar.
 * @param formato Formateador de moneda (configurado para El Salvador).
 * @param color Color principal usado para el ícono y el texto del monto.
 * @param modifier Modificador de Compose (por defecto ocupa el peso disponible).
 */
@Composable
fun SummarySmall(
    title: String,
    amount: Double,
    formato: NumberFormat,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(88.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_today),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    formato.format(amount),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = color
                )
            }
        }
    }
}

/**
 * Tarjeta compacta para mostrar conteos (puestos, comerciantes, etc.).
 *
 * @param title Título del conteo.
 * @param count Valor numérico a mostrar.
 * @param color Color principal del ícono y texto.
 * @param modifier Modificador de layout.
 */
@Composable
fun CountSmall(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(88.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_store),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(4.dp))
                Text("$count", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = color)
            }
        }
    }
}

/**
 * Tarjeta reutilizable con título, ícono y contenido personalizado.
 *
 * @param title Título de la sección.
 * @param iconRes Recurso drawable del ícono.
 * @param content Lambda que define el contenido interior de la tarjeta.
 */
@Composable
fun SmallCard(
    title: String,
    iconRes: Int,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(id = iconRes), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

/**
 * Gráfico de barras horizontales que muestra los cobros de los últimos 7 días.
 *
 * @param data Lista de [CobroPorDia] con día (dos dígitos) y monto total.
 * @param maxValue Valor máximo para escalar las barras (normalmente el mayor monto de la lista).
 * @param formato Formateador de moneda para mostrar los montos.
 */
@Composable
fun BarChart(data: List<CobroPorDia>, maxValue: Double, formato: NumberFormat) {
    val colores = listOf(
        MaterialTheme.colorScheme.primary,
        Color(0xFF2196F3), Color(0xFFFF9800), Color(0xFFE91E63),
        Color(0xFF9C27B0), Color(0xFF00BCD4), Color(0xFFFFC107)
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        data.forEachIndexed { index, item ->
            val fraction = if (maxValue > 0) (item.total / maxValue).toFloat() else 0f
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.dia, modifier = Modifier.width(32.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f).height(28.dp).clip(RoundedCornerShape(8.dp))) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction)
                            .background(colores[index % colores.size])
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(formato.format(item.total), fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium)
            }
        }
    }
}

/**
 * Gráfico de línea suave que muestra la evolución mensual de ingresos.
 *
 * @param data Lista de valores (en orden cronológico) correspondientes a los últimos meses.
 *             Se normaliza automáticamente para ajustarse al alto del canvas.
 */
@Composable
fun LineChart(data: List<Float>) {
    val graphColor = MaterialTheme.colorScheme.primary
    val spacing = 16.dp

    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        if (data.isEmpty()) return@Canvas

        val spacePerPoint = (size.width - spacing.toPx()) / (data.size - 1).coerceAtLeast(1)

        val min = data.min()
        val max = data.max()
        val range = if (max > min) max - min else 1f

        val normY = data.map { ((it - min) / range) * size.height }

        val strokePath = Path().apply {
            for (i in data.indices) {
                val currentX = spacing.toPx() + i * spacePerPoint
                if (i == 0) {
                    moveTo(currentX, normY[i])
                } else {
                    val prevX = spacing.toPx() + (i - 1) * spacePerPoint
                    val controlX = (prevX + currentX) / 2f
                    cubicTo(
                        controlX, normY[i - 1],
                        controlX, normY[i],
                        currentX, normY[i]
                    )
                }
            }
        }

        drawPath(
            path = strokePath,
            color = graphColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Puntos de datos
        data.indices.forEach { i ->
            val x = spacing.toPx() + i * spacePerPoint
            drawCircle(color = graphColor, radius = 4.dp.toPx(), center = Offset(x, normY[i]))
        }
    }
}

/**
 * Gráfico de pastel que muestra la distribución de cobros por comerciante.
 *
 * @param data Lista de pares (nombre del comerciante, monto total).
 * @param formato Formateador de moneda para la leyenda.
 */
@Composable
fun PieChart(data: List<Pair<String, Double>>, formato: NumberFormat) {
    val total = data.sumOf { it.second }.takeIf { it > 0 } ?: 1.0
    var startAngle = 0f

    val palette = listOf(
        MaterialTheme.colorScheme.primary,
        Color(0xFF2196F3), Color(0xFFFF9800), Color(0xFFE91E63),
        Color(0xFF9C27B0), Color(0xFF00BCD4)
    )

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(160.dp)) {
            data.forEachIndexed { index, (_, monto) ->
                val sweep = (monto / total * 360.0).toFloat()
                drawArc(
                    color = palette[index % palette.size],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true
                )
                startAngle += sweep
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Column(modifier = Modifier.fillMaxWidth()) {
        data.take(5).forEachIndexed { index, (nombre, monto) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(palette[index % palette.size], RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("$nombre: ${formato.format(monto)}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground)
            }
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

/**
 * Devuelve una lista con las fechas (formato yyyy-MM-dd) de los últimos 7 días,
 * incluyendo el día actual, en orden descendente (más reciente primero).
 *
 * @return Lista inmutable de 7 fechas en formato String.
 */
fun getLast7Days(): List<String> {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()
    return (6 downTo 0).map { offset ->
        val c = calendar.clone() as Calendar
        c.add(Calendar.DAY_OF_YEAR, -offset)
        format.format(c.time)
    }
}