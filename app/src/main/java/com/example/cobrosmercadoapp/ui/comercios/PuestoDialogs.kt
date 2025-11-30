package com.example.cobrosmercadoapp.ui.comercios

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cobrosmercadoapp.data.entity.Comerciante
import com.example.cobrosmercadoapp.data.entity.Puesto
import com.example.cobrosmercadoapp.data.entity.PuestoConComerciante

/**
 * Dialog para crear un nuevo puesto en el mercado.
 *
 * Muestra un formulario con el número de puesto y un selector buscable de comerciantes.
 * Realiza validaciones en tiempo real y evita la creación de números de puesto duplicados.
 *
 * @param comerciantes Lista completa de comerciantes disponibles para asignar al puesto.
 * @param puestosExistentes Lista completa de puestos existentes. Se usa para comprobar duplicidad
 *   del número de puesto (ignorando mayúsculas/minúsculas).
 * @param onDismiss Acción ejecutada cuando el usuario cierra el diálogo sin confirmar.
 * @param onConfirm Callback invocado cuando los datos son válidos.
 *   Recibe el **número de puesto** (String, ya trimmed) y el **ID del comerciante** seleccionado.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuestoDialogCreate(
    comerciantes: List<Comerciante>,
    puestosExistentes: List<PuestoConComerciante>,
    onDismiss: () -> Unit,
    onConfirm: (numeroPuesto: String, idComerciante: Int) -> Unit
) {
    var numero by remember { mutableStateOf("") }
    var selectedComercianteId by remember { mutableStateOf<Int?>(null) }
    var selectedComercianteText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Nuevo Puesto", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        },
        containerColor = Color.White,
        text = {
            Column {
                OutlinedTextField(
                    value = numero,
                    onValueChange = {
                        numero = it
                        error = null
                    },
                    label = { Text("Número de puesto") },
                    isError = error != null,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                SearchableDropdown(
                    options = comerciantes.map { it.nombre_comerciante to it.id_comerciante },
                    selectedOption = selectedComercianteText,
                    onOptionSelected = { text, id ->
                        selectedComercianteText = text
                        selectedComercianteId = id
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
            Button(
                onClick = {
                    val num = numero.trim()

                    when {
                        num.isBlank() -> error = "Ingresa un número de puesto"
                        selectedComercianteId == null -> error = "Selecciona un comerciante"
                        puestosExistentes.any { it.puesto.numero_puesto.equals(num, ignoreCase = true) } ->
                            error = "El número de puesto '$num' ya está registrado"
                        else -> onConfirm(num, selectedComercianteId!!)
                    }
                },
                enabled = numero.isNotBlank() && selectedComercianteId != null
            ) { Text("Crear") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

/**
 * Dialog para editar un puesto existente.
 *
 * Funciona de forma similar a [PuestoDialogCreate] pero inicializa los campos con los datos
 * actuales del puesto y permite conservar el mismo número si no ha cambiado.
 *
 * @param item Puesto con su comerciante asociado que se desea modificar.
 * @param comerciantes Lista completa de comerciantes disponibles.
 * @param puestosExistentes Lista completa de puestos existentes (para validar unicidad del número).
 * @param onDismiss Acción ejecutada al cancelar la edición.
 * @param onConfirm Callback invocado al guardar cambios válidos.
 *   Proporciona: **ID del puesto**, **nuevo número de puesto** y **nuevo ID de comerciante**.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuestoDialogEdit(
    item: PuestoConComerciante,
    comerciantes: List<Comerciante>,
    puestosExistentes: List<PuestoConComerciante>,
    onDismiss: () -> Unit,
    onConfirm: (idPuesto: Int, numeroPuesto: String, idComerciante: Int) -> Unit
) {
    var numero by remember { mutableStateOf(item.puesto.numero_puesto) }
    var selectedComercianteId by remember { mutableStateOf(item.puesto.id_comerciante) }
    var selectedComercianteText by remember {
        mutableStateOf(
            comerciantes.find { it.id_comerciante == item.puesto.id_comerciante }?.nombre_comerciante ?: ""
        )
    }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Editar Puesto", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        },
        containerColor = Color.White,
        text = {
            Column {
                OutlinedTextField(
                    value = numero,
                    onValueChange = { numero = it; error = null },
                    label = { Text("Número de puesto") },
                    isError = error != null,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                SearchableDropdown(
                    options = comerciantes.map { it.nombre_comerciante to it.id_comerciante },
                    selectedOption = selectedComercianteText,
                    onOptionSelected = { text, id ->
                        selectedComercianteText = text
                        selectedComercianteId = id
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
            Button(
                onClick = {
                    val num = numero.trim()

                    when {
                        num.isBlank() -> error = "Ingresa un número de puesto"
                        selectedComercianteId == null -> error = "Selecciona un comerciante"
                        puestosExistentes.any {
                            it.puesto.numero_puesto.equals(num, ignoreCase = true) &&
                                    it.puesto.id_puesto != item.puesto.id_puesto
                        } -> error = "El número de puesto '$num' ya está en uso por otro puesto"
                        else -> onConfirm(item.puesto.id_puesto, num, selectedComercianteId!!)
                    }
                },
                enabled = numero.isNotBlank() && selectedComercianteId != null
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

/**
 * Dialog de confirmación para eliminar un puesto.
 *
 * @param puesto El puesto que se solicita eliminar.
 * @param onDismiss Callback ejecutado al cancelar la eliminación.
 * @param onConfirm Callback ejecutado al confirmar la eliminación.
 */
@Composable
fun ConfirmDeletePuesto(
    puesto: Puesto,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar Puesto", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
        containerColor = Color.White,
        text = { Text("¿Eliminar el puesto #${puesto.numero_puesto}?") },
        confirmButton = { Button(onClick = onConfirm) { Text("Eliminar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

/**
 * Componente dropdown con capacidad de búsqueda para seleccionar un elemento de una lista.
 *
 * Filtra las opciones en tiempo real según el texto introducido y permite selección mediante click.
 * Se utiliza principalmente para la selección de comerciantes.
 *
 * @param options Lista de pares **(texto visible, ID asociado)**.
 * @param selectedOption Texto actualmente mostrado en el campo (puede ser vacío al inicio).
 * @param onOptionSelected Callback invocado al seleccionar una opción.
 *   Recibe el **texto** y el **ID** correspondiente.
 * @param label Etiqueta del campo (por defecto "Seleccionar comerciante").
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchableDropdown(
    options: List<Pair<String, Int>>,
    selectedOption: String,
    onOptionSelected: (String, Int) -> Unit,
    label: String = "Seleccionar comerciante"
) {
    var expanded by remember { mutableStateOf(false) }
    var searchText by remember(selectedOption) { mutableStateOf(selectedOption) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            readOnly = false,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .exposedDropdownSize()
                .background(Color.White)
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
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