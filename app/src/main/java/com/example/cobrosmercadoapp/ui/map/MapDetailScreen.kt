package com.example.cobrosmercadoapp.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.cobrosmercadoapp.data.entity.CobroDetalle
import com.example.cobrosmercadoapp.data.repository.AppRepository
import org.mapsforge.core.model.LatLong
import org.mapsforge.map.android.graphics.AndroidGraphicFactory
import org.mapsforge.map.android.util.AndroidUtil
import org.mapsforge.map.android.view.MapView
import org.mapsforge.map.layer.overlay.Marker
import org.mapsforge.map.layer.renderer.TileRendererLayer
import org.mapsforge.map.reader.MapFile
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla que muestra los detalles de un cobro junto con un mapa offline
 * utilizando MapsForge.
 *
 * La pantalla:
 * - Solicita el detalle completo del cobro por ID.
 * - Presenta datos del cobro, comerciante, puesto y cobrador.
 * - Renderiza un mapa offline centrado en la ubicación del cobro (si existe).
 * - Permite regresar mediante el `navController`.
 *
 * @param cobroId Identificador único del cobro cuyos detalles deben consultarse.
 * @param repository Fuente de datos para obtener información relacionada al cobro.
 * @param navController Controlador de navegación para volver a la pantalla anterior.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapDetailScreen(
    cobroId: Int,
    repository: AppRepository,
    navController: NavController
) {
    var cobroDetalle by remember { mutableStateOf<CobroDetalle?>(null) }

    LaunchedEffect(cobroId) {
        cobroDetalle = repository.getCobroDetalleById(cobroId)
    }

    val cobro = cobroDetalle?.cobro
    val puesto = cobroDetalle?.puesto
    val comerciante = cobroDetalle?.comerciante

    val hasGps = cobro?.latitud != null && cobro.longitud != null
    val lat = cobro?.latitud ?: 13.68935
    val lng = cobro?.longitud ?: -89.18718

    val PastelMint = Color(0xFFA8E6CF)
    val ErrorRed = Color(0xFFFF6B6B)
    val TextDark = Color(0xFF2D3436)

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Detalle del Cobro", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color(0xFF6C63FF).copy(alpha = 0.9f)
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (cobro == null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = PastelMint)
                    Spacer(Modifier.height(16.dp))
                    Text("Cargando detalle...", color = TextDark.copy(alpha = 0.7f))
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {

                    // MAPA
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                            .shadow(12.dp, RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    ) {
                        OfflineMapView(
                            lat = lat,
                            lng = lng,
                            label = "Cobro #${cobro.id_cobro}",
                            enabled = hasGps
                        )

                        if (!hasGps) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xCCFFFFFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFFFFF0F0),
                                    border = BorderStroke(2.dp, ErrorRed.copy(alpha = 0.8f)),
                                    modifier = Modifier.padding(32.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(20.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Outlined.LocationOff, null, tint = ErrorRed, modifier = Modifier.size(32.dp))
                                        Spacer(Modifier.width(16.dp))
                                        Text(
                                            text = "Este cobro no tiene datos GPS",
                                            color = TextDark,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // TARJETA DE DETALLES
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .shadow(12.dp, RoundedCornerShape(32.dp))
                            .clip(RoundedCornerShape(32.dp)),
                        color = Color.White
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Icon(Icons.Outlined.Payments, null, tint = PastelMint, modifier = Modifier.size(48.dp))

                            Text(
                                text = "Cobro Realizado",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            Spacer(Modifier.height(16.dp))

                            DetailRow(Icons.Outlined.Tag, "ID Cobro", "#${cobro.id_cobro}")
                            DetailRow(Icons.Outlined.CalendarToday, "Fecha", formatDate(cobro.fecha_cobro))
                            DetailRow(Icons.Outlined.Money, "Monto Cobrado", formatMoney(cobro.monto_cobrado), highlight = true)
                            DetailRow(Icons.Outlined.AttachMoney, "Dinero Recibido", formatMoney(cobro.dinero_recibido))
                            DetailRow(Icons.Outlined.MoneyOff, "Vuelto", formatMoney(cobro.vuelto))
                            DetailRow(Icons.Outlined.LocationOn, "Ubicación", if (hasGps) "GPS Capturada" else "No registrada", highlight = hasGps)

                            Spacer(Modifier.height(12.dp))

                            if (comerciante != null) {
                                Divider(Modifier.padding(vertical = 12.dp))
                                DetailRow(Icons.Outlined.Person, "Comerciante", comerciante.nombre_comerciante)
                            }

                            if (puesto != null) {
                                Divider(Modifier.padding(vertical = 12.dp))
                                DetailRow(Icons.Outlined.Store, "Puesto #", puesto.numero_puesto)
                            }

                            Divider(Modifier.padding(vertical = 12.dp))
                            DetailRow(Icons.Outlined.Person, "Cobrador ID", "#${cobro.id_usuario}")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Componente reutilizable para mostrar un par etiqueta/valor acompañado de un ícono.
 *
 * Se utiliza ampliamente para presentar campos dentro de la ficha del cobro.
 *
 * @param icon Ícono representativo del dato.
 * @param label Etiqueta descriptiva del dato.
 * @param value Valor a mostrar.
 * @param highlight Indica si el valor debe mostrarse con estilo destacado.
 */
@Composable
fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    highlight: Boolean = false
) {
    val TextDark = Color(0xFF2D3436)
    val PastelLavender = Color(0xFF6C63FF)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            null,
            tint = if (highlight) PastelLavender else TextDark.copy(alpha = 0.7f),
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, fontSize = 13.sp, color = TextDark.copy(alpha = 0.65f))
            Text(
                value,
                fontSize = if (highlight) 20.sp else 17.sp,
                fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
                color = if (highlight) PastelLavender else TextDark
            )
        }
    }
}

// --------- FORMATTERS ---------

private val moneyFormat = DecimalFormat("$#,##0.00")
private fun formatMoney(amount: Double) = moneyFormat.format(amount)

private val dateFormat = SimpleDateFormat("dd 'de' MMMM 'del' yyyy", Locale("es", "SV"))

/**
 * Convierte una fecha desde formato ISO (`yyyy-MM-dd`) a un formato largo en español.
 *
 * @param dateStr Fecha en formato ISO.
 * @return Cadena formateada o el valor original si ocurre un error.
 */
private fun formatDate(dateStr: String): String {
    return try {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
        date?.let { dateFormat.format(it) } ?: dateStr
    } catch (e: Exception) {
        dateStr
    }
}

/**
 * Vista de mapa offline basada en MapsForge.
 * Soporta:
 * - Renderizado de mapa con tema XML.
 * - Carga automática del archivo .map desde assets si no existe en `filesDir`.
 * - Marcador personalizado opcional si el mapa está habilitado.
 *
 * @param lat Latitud del punto central.
 * @param lng Longitud del punto central.
 * @param label Texto que se colocará dentro del marcador.
 * @param enabled Si es falso, el mapa se bloquea y muestra una vista general sin controles.
 */
@Composable
fun OfflineMapView(lat: Double, lng: Double, label: String, enabled: Boolean = true) {
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                AndroidGraphicFactory.createInstance(ctx.applicationContext)
                isClickable = enabled
                setBuiltInZoomControls(enabled)
                mapScaleBar.isVisible = enabled

                val mapFile = File(ctx.filesDir, "el-salvador.map")
                if (!mapFile.exists()) {
                    try {
                        ctx.assets.open("maps/el-salvador.map").use { input ->
                            FileOutputStream(mapFile).use { output -> input.copyTo(output) }
                        }
                    } catch (_: Exception) {}
                }

                val mapDataStore = MapFile(mapFile)
                val tileCache = AndroidUtil.createTileCache(
                    ctx,
                    "mapcache",
                    256,
                    1f,
                    1.5
                )

                val tileRendererLayer = TileRendererLayer(
                    tileCache,
                    mapDataStore,
                    model.mapViewPosition,
                    AndroidGraphicFactory.INSTANCE
                ).apply {
                    try {
                        val renderTheme = org.mapsforge.map.android.rendertheme.AssetsRenderTheme(
                            ctx.assets,
                            "maps/",
                            "simple-map.xml"
                        )
                        setXmlRenderTheme(renderTheme)
                    } catch (_: Exception) {}
                }

                layerManager.layers.add(tileRendererLayer)

                if (enabled) {
                    model.mapViewPosition.center = LatLong(lat, lng)
                    model.mapViewPosition.setZoomLevel(17)

                    val bitmap = createMarkerBitmap(ctx, label)
                    val marker = Marker(LatLong(lat, lng), bitmap, 0, -bitmap.height / 2)
                    layerManager.layers.add(marker)
                } else {
                    model.mapViewPosition.center = LatLong(13.68935, -89.18718)
                    model.mapViewPosition.setZoomLevel(6)
                }
            }
        },
        update = { mapView ->
            if (enabled) {
                mapView.model.mapViewPosition.center = LatLong(lat, lng)
            }
        },
        onRelease = { mapView ->
            mapView.layerManager.layers.clear()
            mapView.destroyAll()
        },
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * Crea un bitmap circular para utilizar como marcador dentro del mapa.
 * Incluye texto centrado dividido por líneas si es necesario.
 *
 * @param context Contexto para acceder a recursos.
 * @param text Texto que aparecerá dentro del marcador.
 * @return Bitmap convertido al formato que MapsForge puede renderizar.
 */
private fun createMarkerBitmap(context: Context, text: String): org.mapsforge.core.graphics.Bitmap {
    val size = 200
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFA8E6CF.toInt()
        style = Paint.Style.FILL
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 12f, paint)

    paint.color = android.graphics.Color.WHITE
    paint.textSize = 32f
    paint.textAlign = Paint.Align.CENTER

    val lines = text.split(" ")
    canvas.drawText(lines[0], size / 2f, size / 2f - 10f, paint)
    if (lines.size > 1) canvas.drawText(lines[1], size / 2f, size / 2f + 25f, paint)

    return AndroidGraphicFactory.convertToBitmap(
        android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
    )
}
