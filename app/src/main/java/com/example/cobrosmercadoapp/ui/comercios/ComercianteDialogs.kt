package com.example.cobrosmercadoapp.ui.comercios

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cobrosmercadoapp.data.entity.Comerciante

/**
 * Diálogo para crear un nuevo comerciante.
 *
 * Presenta un formulario mínimo compuesto por un campo de texto y acciones de
 * confirmación y cancelación. Su propósito es capturar el nombre del comerciante
 * a registrar y devolverlo al llamador mediante [onConfirm].
 *
 * Este componente es autónomo y no persiste datos; únicamente comunica el valor
 * ingresado. La lógica de validación y almacenamiento debe manejarse externamente.
 *
 * @param onDismiss Acción ejecutada al cerrar el diálogo sin crear un registro.
 * @param onConfirm Callback llamado con el nombre ingresado cuando el usuario confirma.
 */
@Composable
fun ComercianteDialogCreate(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Nuevo Comerciante",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        containerColor = Color.White,
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del comerciante") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombre.isNotBlank()) onConfirm(nombre.trim())
                }
            ) { Text("Crear") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

/**
 * Diálogo para editar un comerciante existente.
 *
 * Carga los datos actuales del [comerciante] en el formulario, permite modificarlos
 * y entrega el resultado a través del callback [onConfirm] junto al identificador.
 * No modifica directamente el estado de la base de datos ni realiza validaciones,
 * delegando dichas tareas al llamador.
 *
 * @param comerciante Entidad actualmente seleccionada cuyo nombre será editado.
 * @param onDismiss Acción ejecutada al cerrar el diálogo sin actualizar valores.
 * @param onConfirm Callback que recibe el ID del comerciante y el nombre actualizado.
 */
@Composable
fun ComercianteDialogEdit(
    comerciante: Comerciante,
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    var nombre by remember { mutableStateOf(comerciante.nombre_comerciante) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Editar Comerciante",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        containerColor = Color.White,
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del comerciante") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombre.isNotBlank()) {
                        onConfirm(comerciante.id_comerciante, nombre.trim())
                    }
                }
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

/**
 * Diálogo de confirmación para eliminar un comerciante.
 *
 * Informa al usuario que la acción implica eliminar también los puestos asociados
 * al comerciante. La eliminación efectiva debe implementarse fuera del componente;
 * este diálogo solo recoge la intención del usuario.
 *
 * @param comerciante Comerciante que se solicita eliminar.
 * @param onDismiss Acción ejecutada al cerrar el diálogo sin confirmar.
 * @param onConfirm Acción ejecutada cuando el usuario confirma la eliminación.
 */
@Composable
fun ConfirmDeleteComerciante(
    comerciante: Comerciante,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Eliminar Comerciante",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        containerColor = Color.White,
        text = {
            Text(
                "¿Eliminar al comerciante \"${comerciante.nombre_comerciante}\"? " +
                        "Esta acción eliminará también sus puestos."
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Eliminar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
