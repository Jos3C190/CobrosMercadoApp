package com.example.cobrosmercadoapp.ui.auth

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.example.cobrosmercadoapp.ui.components.NeumorphicButton
import com.example.cobrosmercadoapp.ui.components.NeumorphicTextField

/**
 * Pantalla de registro de nuevos usuarios.
 *
 * Permite crear una cuenta proporcionando nombre, apellido, usuario y contraseña.
 * Utiliza un diseño neumórfico consistente con el resto del flujo de autenticación,
 * fondo degradado y soporte para scroll vertical en dispositivos de pantalla pequeña.
 *
 * ### Flujo principal:
 * 1. Validación local de campos obligatorios.
 * 2. Delegación del registro al [AuthViewModel] mediante su método [AuthViewModel.registrar].
 * 3. En caso de éxito: persiste la sesión automáticamente y navega a la pantalla principal (`home`).
 * 4. En caso de error: muestra mensaje debajo de los campos.
 *
 * @param navController Controlador de navegación para redirigir al usuario tras el registro
 *   o volver a la pantalla de login.
 * @param viewModel Instancia compartida de [AuthViewModel] que ejecuta la lógica de registro
 *   y gestión de sesión.
 * @param context Contexto requerido para persistir el estado de autenticación mediante SharedPreferences.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel,
    context: Context
) {
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var usuario by remember { mutableStateOf("") }
    var contraseña by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Ajuste responsivo del tamaño del ícono según altura de pantalla
    val iconSize = with(LocalConfiguration.current.screenHeightDp) {
        when {
            this < 650 -> 65.dp
            this < 800 -> 75.dp
            else -> 85.dp
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFB8C6DB), Color(0xFFF5F7FA))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ícono superior (PersonAdd)
            Box(
                modifier = Modifier
                    .padding(top = 40.dp)
                    .size(iconSize)
                    .shadow(20.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.65f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.PersonAdd,
                    contentDescription = null,
                    tint = Color(0xFF6C63FF),
                    modifier = Modifier.size(iconSize * 0.55f)
                )
            }

            // Tarjeta principal del formulario
            Card(
                modifier = Modifier
                    .padding(top = 30.dp, bottom = 40.dp)
                    .fillMaxWidth(0.9f)
                    .shadow(20.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.75f))
            ) {
                Column(
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Cobros Mercado Municipal",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                        color = Color(0xFF6C63FF),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Crear Cuenta",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
                    )

                    NeumorphicTextField(value = nombre, onValueChange = { nombre = it }, label = "Nombre", isError = errorMessage != null)
                    Spacer(Modifier.height(16.dp))
                    NeumorphicTextField(value = apellido, onValueChange = { apellido = it }, label = "Apellido", isError = errorMessage != null)
                    Spacer(Modifier.height(16.dp))
                    NeumorphicTextField(value = usuario, onValueChange = { usuario = it }, label = "Usuario", isError = errorMessage != null)
                    Spacer(Modifier.height(16.dp))
                    NeumorphicTextField(
                        value = contraseña,
                        onValueChange = { contraseña = it },
                        label = "Contraseña",
                        visualTransformation = PasswordVisualTransformation(),
                        isError = errorMessage != null
                    )

                    errorMessage?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(Modifier.height(28.dp))

                    NeumorphicButton(
                        text = "Registrarse",
                        onClick = {
                            if (nombre.isBlank() || apellido.isBlank() || usuario.isBlank() || contraseña.isBlank()) {
                                errorMessage = "Completa todos los campos"
                                return@NeumorphicButton
                            }

                            scope.launch {
                                viewModel.registrar(nombre, apellido, usuario, contraseña) { success, error ->
                                    if (success) {
                                        viewModel.setLoggedIn(context, usuario)
                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    } else {
                                        errorMessage = error ?: "Error al registrarse"
                                    }
                                }
                            }
                        }
                    )

                    Spacer(Modifier.height(16.dp))

                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("¿Ya tienes cuenta? Inicia sesión", color = Color(0xFF6C63FF))
                    }
                }
            }
        }
    }
}