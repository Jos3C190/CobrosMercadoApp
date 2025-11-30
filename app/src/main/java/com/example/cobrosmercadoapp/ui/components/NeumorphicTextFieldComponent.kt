package com.example.cobrosmercadoapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Campo de texto con estilo *neumórfico* diseñado para integrarse con interfaces
 * de estética suave y elevada. Envuelve un [OutlinedTextField] para conservar
 * la funcionalidad estándar del componente, agregando sombras, bordes suaves y
 * un contenedor semitransparente.
 *
 * Ideal para formularios o diálogos donde se busca una apariencia moderna basada
 * en luces y sombras, sin alterar el comportamiento nativo del campo.
 *
 * @param value Texto actual del campo.
 * @param onValueChange Acción ejecutada cuando el usuario modifica el contenido.
 * @param label Texto mostrado como etiqueta del campo.
 * @param visualTransformation Transformación visual aplicada al texto (por ejemplo, contraseñas).
 * @param isError Indica si el campo debe mostrarse en estado de error.
 */
@Composable
fun NeumorphicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.12f),
                spotColor = Color.Black.copy(alpha = 0.20f)
            )
            .background(
                color = Color.White.copy(alpha = 0.78f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 6.dp, vertical = 10.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(
                    text = label,
                    color = Color.Black.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
            },
            visualTransformation = visualTransformation,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                focusedBorderColor = Color(0xFF6C63FF),
                unfocusedBorderColor = Color.Transparent,
                errorBorderColor = Color.Red,
                cursorColor = Color(0xFF6C63FF),
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black.copy(alpha = 0.8f),
            ),
            isError = isError
        )
    }
}

/**
 * Botón con estética *neumórfica* que simula profundidad mediante sombras
 * dinámicas. Responde al estado de presión del usuario mediante animaciones
 * en la elevación, el desplazamiento vertical y el color, ofreciendo una
 * interacción visual agradable y moderna.
 *
 * Este componente reemplaza el estilo de botón estándar cuando se busca una
 * apariencia más orgánica, ideal para dashboards, pantallas principales
 * y formularios con diseño suave.
 *
 * @param onClick Acción ejecutada al presionar el botón.
 * @param text Texto visible del botón.
 * @param modifier Modificador opcional para personalizar el componente.
 */
@Composable
fun NeumorphicButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 4.dp else 14.dp,
        label = "elevation"
    )
    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 8.dp else 0.dp,
        label = "offset"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isPressed)
            Color.White.copy(alpha = 0.70f)
        else
            Color.White.copy(alpha = 0.92f),
        label = "bgColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (isPressed) Color(0xFF5A52E0) else Color(0xFF6C63FF),
        label = "textColor"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
            .offset(y = offsetY)
            .background(bgColor, RoundedCornerShape(28.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
