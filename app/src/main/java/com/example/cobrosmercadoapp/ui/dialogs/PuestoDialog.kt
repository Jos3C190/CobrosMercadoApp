package com.example.cobrosmercadoapp.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.cobrosmercadoapp.data.entity.Puesto
import com.example.cobrosmercadoapp.data.repository.AppRepository
import kotlinx.coroutines.launch

/**
 * Muestra un cuadro de diálogo para registrar un nuevo [Puesto] en la base de datos.
 *
 * Este componente gestiona:
 * - La entrada del número de puesto.
 * - La selección de un comerciante mediante un menú desplegable con búsqueda.
 * - Validaciones locales antes de la inserción.
 * - Persistencia asíncrona en el repositorio.
 *
 * El diálogo se cierra automáticamente tras una inserción exitosa.
 *
 * @param repository Fuente de datos utilizada para obtener comerciantes y registrar puestos.
 * @param onDismiss Se ejecuta cuando el usuario cierra el diálogo sin guardar.
 * @param onSuccess Se ejecuta tras guardar correctamente un puesto.
 */
@Composable
fun PuestoDialog(
    repository: AppRepository,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var numeroPuesto by remember { mutableStateOf("") }
    var comercianteId by remember { mutableStateOf<Int?>(null) }
    var selectedComercianteText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // Flujo reactivo: lista de comerciantes y puestos usados para validar duplicados
    val comerciantes by repository.getAllComerciantes().collectAsStateWithLifecycle(emptyList())
    val puestosExistentes by repository.getAllPuestosConComerciante().collectAsStateWithLifecycle(emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text(
                "Agregar Puesto",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = numeroPuesto,
                    onValueChange = {
                        numeroPuesto = it
                        error = null
                    },
                    label = { Text("Número del puesto") },
                    isError = error != null,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                SearchableDropdown(
                    options = comerciantes.map { it.nombre_comerciante to it.id_comerciante },
                    selectedOption = selectedComercianteText,
                    onOptionSelected = { text, id ->
                        selectedComercianteText = text
                        comercianteId = id
                        error = null
                    },
                    label = "Comerciante"
                )

                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val num = numeroPuesto.trim()

                    when {
                        num.isBlank() ->
                            error = "Ingresa el número del puesto"

                        comercianteId == null ->
                            error = "Selecciona un comerciante"

                        // Evita duplicados por número
                        puestosExistentes.any { it.puesto.numero_puesto.equals(num, ignoreCase = true) } ->
                            error = "El número de puesto '$num' ya está registrado"

                        else -> {
                            scope.launch {
                                try {
                                    repository.insertarPuesto(
                                        Puesto(
                                            numero_puesto = num,
                                            id_comerciante = comercianteId!!
                                        )
                                    )
                                    onSuccess()
                                    onDismiss()
                                } catch (e: Exception) {
                                    error = "Error al guardar. Intenta de nuevo."
                                }
                            }
                        }
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

/**
 * Campo desplegable con capacidad de búsqueda usado para seleccionar un elemento
 * a partir de una lista de opciones textuales.
 *
 * Se utiliza principalmente en diálogos donde se necesita asignar relaciones
 * (por ejemplo, seleccionar un comerciante para un puesto).
 *
 * @param options Lista de pares donde el primer valor es el texto visible y el segundo es el ID asociado.
 * @param selectedOption Texto actualmente seleccionado. Se utiliza como valor inicial del campo.
 * @param onOptionSelected Callback que retorna el texto elegido y su ID asociado.
 * @param label Etiqueta descriptiva del campo de entrada.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchableDropdown(
    options: List<Pair<String, Int>>,
    selectedOption: String,
    onOptionSelected: (String, Int) -> Unit,
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
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            options
                .filter { it.first.contains(searchText, ignoreCase = true) }
                .forEach { option ->
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
