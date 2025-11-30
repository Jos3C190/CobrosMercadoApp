package com.example.cobrosmercadoapp.ui.payments

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.cobrosmercadoapp.data.entity.CobroDetalle
import com.example.cobrosmercadoapp.data.repository.AppRepository
import com.example.cobrosmercadoapp.ui.dialogs.CobroDialog
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

private val PrimaryPurple = Color(0xFF6C63FF)

/**
 * Pantalla principal encargada de listar, filtrar y gestionar cobros.
 *
 * Esta pantalla:
 * - Muestra los cobros del día o filtrados por rango de fechas.
 * - Permite búsqueda por texto.
 * - Proporciona acciones para crear, editar y eliminar cobros.
 * - Navega hacia la vista de mapa para visualizar la ubicación del cobro.
 *
 * Los filtros y búsquedas se almacenan en el [PaymentsViewModel] para persistencia durante recomposiciones.
 *
 * @param viewModel ViewModel que administra el estado de cobros y filtros.
 * @param repository Repositorio para operaciones CRUD de cobros.
 * @param context Contexto de la aplicación, utilizado para SharedPreferences y diálogos.
 * @param navController Controlador de navegación para ir a la vista de mapa.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(
    viewModel: PaymentsViewModel,
    repository: AppRepository,
    context: Context,
    navController: NavHostController
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val fechaInicio by viewModel.fechaInicio.collectAsState()
    val fechaFin by viewModel.fechaFin.collectAsState()
    val filtroActivo by viewModel.filtroActivo.collectAsState()
    val cobros by viewModel.cobros.collectAsState()

    val scope = rememberCoroutineScope()
    val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    val usuarioLogin = sharedPref.getString("usuario_login", "") ?: ""

    var showInicioPicker by remember { mutableStateOf(false) }
    var showFinPicker by remember { mutableStateOf(false) }
    var filtrosExpandido by remember { mutableStateOf(false) }

    var cobroAEditar by remember { mutableStateOf<CobroDetalle?>(null) }
    var showCobroDialog by remember { mutableStateOf(false) }
    var cobroAEliminar by remember { mutableStateOf<CobroDetalle?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val formatoFechaLarga = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es", "SV"))
    val hoy = formatoFecha.format(Date())
    val hoyLargo = formatoFechaLarga.format(Date())
    val formatoMoneda = NumberFormat.getCurrencyInstance(Locale("es", "SV")).apply {
        maximumFractionDigits = 0
    }

    LaunchedEffect(usuarioLogin) {
        if (viewModel.idUsuario.value == null) {
            viewModel.loadIdUsuario(usuarioLogin)
        }
    }

    val totalDelDia = cobros.sumOf { it.cobro.monto_cobrado }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    cobroAEditar = null
                    showCobroDialog = true
                },
                containerColor = PrimaryPurple
            ) {
                Text("+", color = Color.White, style = MaterialTheme.typography.titleLarge)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFDDE8F6), Color(0xFFFDFDFD))
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Cabecera con fecha
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = MaterialTheme.shapes.large,
                            ambientColor = Color(0x33000000),
                            spotColor = Color(0x22000000)
                        ),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF6C63FF), Color(0xFF8F85FF))
                                )
                            )
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                "Cobros del día",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                hoyLargo,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                // Total del día
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .shadow(4.dp, MaterialTheme.shapes.medium),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Payments, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Total: ${formatoMoneda.format(totalDelDia)}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Filtros y búsqueda
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .shadow(4.dp, MaterialTheme.shapes.medium),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { filtrosExpandido = !filtrosExpandido }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Filtros",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                imageVector = if (filtrosExpandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (filtrosExpandido) {
                            Column(modifier = Modifier.padding(16.dp)) {

                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.updateSearchQuery(it) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(57.dp),
                                    leadingIcon = { Icon(Icons.Default.Search, null) },
                                    label = { Text("Buscar", fontSize = 13.sp) },
                                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                                )

                                Spacer(Modifier.height(12.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = fechaInicio,
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(56.dp),
                                        label = { Text("Desde", fontSize = 13.sp) },
                                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                                        trailingIcon = {
                                            IconButton({ showInicioPicker = true }) {
                                                Icon(Icons.Default.CalendarToday, null)
                                            }
                                        }
                                    )
                                    OutlinedTextField(
                                        value = fechaFin,
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(56.dp),
                                        label = { Text("Hasta", fontSize = 13.sp) },
                                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                                        trailingIcon = {
                                            IconButton({ showFinPicker = true }) {
                                                Icon(Icons.Default.CalendarToday, null)
                                            }
                                        }
                                    )
                                }

                                Spacer(Modifier.height(12.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Button(
                                        onClick = { viewModel.setFiltroActivo(true) },
                                        modifier = Modifier.weight(1f),
                                        enabled = fechaInicio.isNotBlank() && fechaFin.isNotBlank()
                                    ) {
                                        Text("Aplicar")
                                    }

                                    OutlinedButton(
                                        onClick = { viewModel.resetFiltros() },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Hoy")
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Lista de cobros
                if (cobros.isEmpty()) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("No hay cobros", color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(cobros) { detalle ->
                            CobroItem(
                                detalle = detalle,
                                onEdit = {
                                    cobroAEditar = detalle
                                    showCobroDialog = true
                                },
                                onDelete = {
                                    cobroAEliminar = detalle
                                    showDeleteDialog = true
                                },
                                onViewMap = {
                                    navController.navigate("map/${detalle.cobro.id_cobro}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCobroDialog) {
        CobroDialog(
            repository = repository,
            context = context,
            cobroAEditar = cobroAEditar?.cobro,
            onDismiss = { showCobroDialog = false },
            onSuccess = { showCobroDialog = false }
        )
    }

    if (showDeleteDialog && cobroAEliminar != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "Eliminar Cobro",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = { Text("¿Seguro que deseas eliminar este cobro?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch { repository.eliminarCobro(cobroAEliminar!!.cobro) }
                        showDeleteDialog = false
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton({ showDeleteDialog = false }) { Text("Cancelar") }
            },
            containerColor = Color.White,
            tonalElevation = 0.dp
        )
    }

    if (showInicioPicker) {
        CustomDatePickerDialog(
            onDateSelected = { viewModel.updateFechaInicio(it) },
            onDismiss = { showInicioPicker = false }
        )
    }
    if (showFinPicker) {
        CustomDatePickerDialog(
            onDateSelected = { viewModel.updateFechaFin(it) },
            onDismiss = { showFinPicker = false }
        )
    }
}

/**
 * Elemento visual que representa un cobro individual en la lista.
 *
 * Muestra la información principal del cobro, incluyendo puesto,
 * comerciante, fecha, monto cobrado y dinero recibido. Ofrece acciones
 * para editar, eliminar y visualizar en mapa.
 *
 * @param detalle Información combinada del cobro, puesto y comerciante.
 * @param onEdit Acción ejecutada cuando el usuario desea editar el cobro.
 * @param onDelete Acción ejecutada cuando el usuario confirma la eliminación.
 * @param onViewMap Acción para navegar hacia la vista del mapa.
 */
@Composable
fun CobroItem(
    detalle: CobroDetalle,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewMap: () -> Unit
) {
    val cobro = detalle.cobro
    val puesto = detalle.puesto
    val comerciante = detalle.comerciante

    val formato = NumberFormat.getCurrencyInstance(Locale("es", "SV")).apply {
        maximumFractionDigits = 0
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, shape = MaterialTheme.shapes.medium)
            .padding(top = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Puesto: #${puesto.numero_puesto}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        comerciante?.nombre_comerciante ?: "No asignado",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        cobro.fecha_cobro,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        formato.format(cobro.monto_cobrado),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Recibido: ${formato.format(cobro.dinero_recibido)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(4.dp))

                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                        }
                        IconButton(onClick = onViewMap) {
                            Icon(
                                Icons.Default.RemoveRedEye,
                                contentDescription = "Ver en mapa",
                                tint = Color(0xFF2196F3)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Diálogo de selección de fecha basado en [DatePickerDialog].
 *
 * Devuelve la fecha seleccionada en formato `yyyy-MM-dd`,
 * aplicando zona horaria UTC para mantener consistencia
 * con los valores almacenados en el backend.
 *
 * @param onDateSelected Callback que recibe la fecha formateada seleccionada.
 * @param onDismiss Acción ejecutada al cerrar el diálogo sin seleccionar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        colors = DatePickerDefaults.colors(containerColor = Color.White),
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    onDateSelected(formatter.format(Date(millis)))
                }
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    ) {
        DatePicker(state = datePickerState)
    }
}
