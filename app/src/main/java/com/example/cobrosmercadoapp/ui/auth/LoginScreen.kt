package com.example.cobrosmercadoapp.ui.auth

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cobrosmercadoapp.ui.components.NeumorphicButton
import com.example.cobrosmercadoapp.ui.components.NeumorphicTextField
import kotlinx.coroutines.launch

/**
 * Pantalla de inicio de sesión de la aplicación "Cobros Mercado Municipal".
 *
 * Permite al usuario autenticarse mediante nombre de usuario y contraseña.
 * Utiliza un diseño neumórfico con fondo degradado y componentes personalizados
 * ([NeumorphicTextField], [NeumorphicButton]) para una experiencia visual moderna.
 *
 * ### Flujo principal:
 * 1. Validación local de campos vacíos.
 * 2. Delegación de autenticación al [AuthViewModel].
 * 3. En caso de éxito: persiste la sesión y navega a la pantalla principal (`home`).
 * 4. En caso de error: muestra mensaje debajo de los campos.
 *
 * @param navController Controlador de navegación para realizar transiciones entre pantallas.
 * @param viewModel Instancia de [AuthViewModel] que gestiona la lógica de autenticación y estado de sesión.
 * @param context Contexto requerido para persistir el estado de login mediante SharedPreferences.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel,
    context: Context
) {
    var usuario by remember { mutableStateOf("") }
    var contraseña by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFB8C6DB), Color(0xFFF5F7FA))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo decorativo
            Box(
                modifier = Modifier
                    .size(85.dp)
                    .shadow(18.dp, RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.55f), RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Storefront,
                    contentDescription = "Logo Mercado",
                    tint = Color(0xFF6C63FF),
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
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
                        "Iniciar Sesión",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
                    )

                    NeumorphicTextField(
                        value = usuario,
                        onValueChange = { usuario = it },
                        label = "Usuario",
                        isError = errorMessage != null
                    )

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
                        onClick = {
                            if (usuario.isBlank() || contraseña.isBlank()) {
                                errorMessage = "Completa todos los campos"
                                return@NeumorphicButton
                            }

                            scope.launch {
                                viewModel.login(usuario, contraseña) { success, error ->
                                    if (success) {
                                        viewModel.setLoggedIn(context, usuario)
                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    } else {
                                        errorMessage = error ?: "Error desconocido"
                                    }
                                }
                            }
                        },
                        text = "Entrar"
                    )

                    Spacer(Modifier.height(16.dp))

                    TextButton(onClick = { navController.navigate("register") }) {
                        Text("Crear cuenta nueva", color = Color(0xFF6C63FF))
                    }
                }
            }
        }
    }
}