package com.example.cobrosmercadoapp.ui.dialogs

import android.Manifest
import android.content.Context
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cobrosmercadoapp.data.entity.Cobro
import com.example.cobrosmercadoapp.data.repository.AppRepository
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Diálogo para registrar un nuevo cobro o editar uno existente.
 *
 * Este componente administra:
 * - Selección de puesto mediante un dropdown con búsqueda.
 * - Captura de monto cobrado y dinero recibido.
 * - Cálculo automático del vuelto.
 * - Obtención opcional de ubicación mediante GPS.
 * - Validación de datos antes de insertar o actualizar un cobro.
 *
 * El diálogo es modal y requiere la acción del usuario para confirmar o cancelar.
 *
 * @param repository Repositorio principal para realizar operaciones sobre cobros y usuarios.
 * @param context Contexto necesario para acceder a recursos del sistema como GPS o SharedPreferences.
 * @param cobroAEditar Cobro existente a editar. Si es `null`, se creará uno nuevo.
 * @param onDismiss Acción ejecutada al cerrar el diálogo sin guardar.
 * @param onSuccess Acción ejecutada cuando el cobro se guarda con éxito.
 */
@Composable
fun CobroDialog(
    repository: AppRepository,
    context: Context,
    cobroAEditar: Cobro? = null,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var puestoId by remember(cobroAEditar) { mutableStateOf(cobroAEditar?.id_puesto) }
    var monto by remember(cobroAEditar) { mutableStateOf(cobroAEditar?.monto_cobrado?.toString() ?: "") }
    var recibido by remember(cobroAEditar) { mutableStateOf(cobroAEditar?.dinero_recibido?.toString() ?: "") }
    var latitud by remember(cobroAEditar) { mutableStateOf(cobroAEditar?.latitud) }
    var longitud by remember(cobroAEditar) { mutableStateOf(cobroAEditar?.longitud) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val puestos by repository.getAllPuestosConComerciante().collectAsStateWithLifecycle(emptyList())

    val fecha = cobroAEditar?.fecha_cobro
        ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // Actualiza el texto del puesto seleccionado al entrar en modo edición
    var selectedPuestoText by remember(cobroAEditar) { mutableStateOf("") }
    LaunchedEffect(cobroAEditar, puestos) {
        cobroAEditar?.let { cobro ->
            puestos.find { it.puesto.id_puesto == cobro.id_puesto }?.let { p ->
                selectedPuestoText =
                    "Puesto #${p.puesto.numero_puesto} - ${p.comerciante?.nombre_comerciante ?: "Sin comerciante"}"
            }
        }
    }

    val montoDouble = monto.toDoubleOrNull() ?: 0.0
    val recibidoDouble = recibido.toDoubleOrNull() ?: 0.0
    val vuelto = recibidoDouble - montoDouble

    val formato = NumberFormat.getCurrencyInstance(Locale("es", "SV")).apply {
        maximumFractionDigits = 0
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    latitud = it.latitude
                    longitud = it.longitude
                }
            }
        } else {
            error = "Permiso de ubicación denegado"
        }
    }

    val pastelGreen = Color(0xFFBEECC4)
    val pastelRed = Color(0xFFF8C1C1)
    val textGreen = Color(0xFF2E7031)
    val textRed = Color(0xFF8A1F1F)
    val dialogBg = Color(0xFFFDFDFE)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogBg,
        title = {
            Text(
                if (cobroAEditar == null) "Nuevo Cobro" else "Editar Cobro",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column {

                SearchableDropdown(
                    options = puestos.map {
                        "Puesto #${it.puesto.numero_puesto} - ${it.comerciante?.nombre_comerciante ?: "Sin comerciante"}" to it.puesto.id_puesto
                    },
                    selectedOption = selectedPuestoText,
                    onOptionSelected = { selectedText, selectedId ->
                        selectedPuestoText = selectedText
                        puestoId = selectedId
                    },
                    label = "Puesto"
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = monto,
                    onValueChange = { monto = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Monto cobrado") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = recibido,
                    onValueChange = { recibido = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Dinero recibido") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors()
                )

                Spacer(Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (vuelto >= 0) pastelGreen else pastelRed
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Monto a devolver:",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            when {
                                vuelto > 0 -> formato.format(vuelto)
                                vuelto == 0.0 -> "$0"
                                else -> "Falta ${formato.format(-vuelto)}"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (vuelto >= 0) textGreen else textRed
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = {
                        if (latitud != null) {
                            latitud = null
                            longitud = null
                        } else {
                            val granted = ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                            if (!granted) {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            } else {
                                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                    location?.let {
                                        latitud = it.latitude
                                        longitud = it.longitude
                                    }
                                }
                            }
                        }
                    }) {
                        Text(if (latitud == null) "Usar GPS" else "Quitar GPS")
                    }

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = if (latitud != null) "GPS: OK" else "Sin GPS",
                        color = if (latitud != null)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                }

                error?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (puestoId == null) {
                    error = "Selecciona un puesto"; return@TextButton
                }
                if (montoDouble <= 0) {
                    error = "Ingresa un monto válido"; return@TextButton
                }
                if (recibidoDouble < montoDouble) {
                    error = "El recibido debe ser ≥ al monto"; return@TextButton
                }

                scope.launch {
                    val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                    val usuarioLogin = sharedPref.getString("usuario_login", "") ?: ""

                    val usuario = repository.getAllUsuarios()
                        .firstOrNull { it.usuario_login == usuarioLogin }

                    if (usuario == null) {
                        error = "Usuario no encontrado"
                        return@launch
                    }

                    val cobroActualizado = Cobro(
                        id_cobro = cobroAEditar?.id_cobro,
                        id_puesto = puestoId!!,
                        monto_cobrado = montoDouble,
                        dinero_recibido = recibidoDouble,
                        fecha_cobro = fecha,
                        latitud = latitud,
                        longitud = longitud,
                        id_usuario = usuario.id_usuario
                    )

                    if (cobroAEditar == null)
                        repository.insertarCobro(cobroActualizado)
                    else
                        repository.actualizarCobro(cobroActualizado)

                    onSuccess()
                    onDismiss()
                }
            }) {
                Text(if (cobroAEditar == null) "Cobrar" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

/**
 * Campo desplegable con búsqueda integrada.
 *
 * Este componente permite:
 * - Escribir para filtrar opciones en tiempo real.
 * - Seleccionar un valor asociado (por ejemplo, un ID).
 * - Mostrar el texto seleccionado en el TextField.
 *
 * Se adapta a listas grandes y mejora la usabilidad en comparación
 * con un `DropdownMenu` tradicional.
 *
 * @param options Lista de pares de texto visible y valor asociado.
 * @param selectedOption Texto mostrado inicialmente en el campo.
 * @param onOptionSelected Callback con el texto mostrado y el valor seleccionado.
 * @param label Etiqueta del campo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SearchableDropdown(
    options: List<Pair<String, T>>,
    selectedOption: String,
    onOptionSelected: (String, T) -> Unit,
    label: String = "Selecciona"
) {
    var expanded by remember { mutableStateOf(false) }
    var searchText by remember(selectedOption) { mutableStateOf(selectedOption) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        val filtered = options.filter {
            it.first.contains(searchText, ignoreCase = true)
        }

        if (filtered.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                filtered.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.first) },
                        onClick = {
                            searchText = option.first
                            onOptionSelected(option.first, option.second)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
