package com.example.cobrosmercadoapp.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cobrosmercadoapp.data.entity.Comerciante
import com.example.cobrosmercadoapp.data.repository.AppRepository
import kotlinx.coroutines.launch

/**
 * Muestra un cuadro de diálogo para registrar un nuevo [Comerciante].
 *
 * Este diálogo permite ingresar el nombre del comerciante y guardar el registro
 * en la base de datos mediante el [AppRepository]. Su uso está orientado a
 * pantallas de administración de puestos o comerciantes.
 *
 * ### Comportamiento
 * - Valida que el nombre no esté vacío.
 * - Inserta el comerciante en la base de datos dentro de una corrutina.
 * - Cierra el diálogo al confirmar o cancelar.
 * - Notifica al llamador a través de [onSuccess] cuando el registro se ha guardado.
 *
 * @param repository Fuente de datos que permite insertar el nuevo comerciante.
 * @param onDismiss Acción ejecutada cuando el usuario cierra el diálogo sin confirmar.
 * @param onSuccess Acción ejecutada cuando el registro se guarda correctamente.
 */
@Composable
fun ComercianteDialog(
    repository: AppRepository,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        titleContentColor = MaterialTheme.colorScheme.primary,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        title = {
            Text(
                "Agregar Comerciante",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del comerciante") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFDDDDDD),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = Color(0xFF777777),
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

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
            TextButton(
                onClick = {
                    if (nombre.isBlank()) {
                        error = "El nombre es obligatorio"
                        return@TextButton
                    }
                    scope.launch {
                        repository.insertarComerciante(
                            Comerciante(nombre_comerciante = nombre)
                        )
                        onSuccess()
                        onDismiss()
                    }
                }
            ) {
                Text("Guardar", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.primary)
            }
        }
    )
}
