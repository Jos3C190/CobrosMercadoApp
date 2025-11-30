package com.example.cobrosmercadoapp.ui.splash

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

/**
 * Pantalla de presentación (Splash Screen) mostrada al iniciar la aplicación.
 *
 * Esta pantalla cumple dos propósitos principales:
 * 1. Evitar el parpadeo inmediato hacia la pantalla de Login cuando la app se abre,
 *    simulando una breve carga inicial.
 * 2. Verificar si el usuario ya tiene una sesión iniciada y navegar automáticamente
 *    a la pantalla correspondiente.
 *
 * Flujo de navegación:
 * - Si **isLoggedIn == true**, la app navega a la pantalla **Home**.
 * - Si **isLoggedIn == false**, se redirige al usuario a la pantalla **Login**.
 *
 * @param navController Controlador de navegación para redirigir al usuario.
 * @param isLoggedIn Indica si el usuario ya tiene una sesión iniciada.
 */
@Composable
fun SplashScreen(
    navController: NavController,
    isLoggedIn: Boolean
) {
    /**
     * Efecto lanzado una sola vez.
     *
     * Se agrega un delay mínimo (700ms) para garantizar que el Splash
     * aparezca lo suficiente y evitar el salto visual brusco.
     */
    LaunchedEffect(Unit) {
        delay(700)
        if (isLoggedIn) {
            // Navega al Home si el usuario ya inició sesión
            navController.navigate("home") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            // Navega al Login si no hay sesión activa
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    /**
     * Interfaz del Splash:
     * Centra un título y un indicador de progreso en la pantalla.
     */
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Cobros Mercado Municipal",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            CircularProgressIndicator()
        }
    }
}
