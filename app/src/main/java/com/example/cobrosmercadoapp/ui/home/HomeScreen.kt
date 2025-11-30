package com.example.cobrosmercadoapp.ui.home

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.cobrosmercadoapp.data.DatabaseProvider
import com.example.cobrosmercadoapp.data.entity.CobroDetalle
import com.example.cobrosmercadoapp.data.repository.AppRepository
import com.example.cobrosmercadoapp.ui.auth.AuthViewModel
import com.example.cobrosmercadoapp.ui.dialogs.ComercianteDialog
import com.example.cobrosmercadoapp.ui.dialogs.CobroDialog
import com.example.cobrosmercadoapp.ui.dialogs.PuestoDialog
import java.text.NumberFormat
import java.util.*

/**
 * Pantalla principal del módulo de cobros.
 *
 * Esta pantalla actúa como el punto de entrada para los usuarios autenticados
 * y presenta un resumen consolidado de la actividad reciente, accesos directos
 * a funciones clave y opciones de navegación hacia las secciones operativas de
 * la aplicación.
 *
 * ## Responsabilidades
 * - Mostrar el panel general del cobrador, incluyendo estadísticas y accesos rápidos.
 * - Gestionar la interacción entre la interfaz de usuario y el `AuthViewModel`,
 *   el cual expone el estado de autenticación y controla la carga de los cobros
 *   asociados al usuario.
 * - Proveer navegación hacia otros módulos: creación de comerciantes, creación
 *   de puestos, registro de cobros y vista de análisis.
 * - Desplegar el listado de transacciones recientes derivado del repositorio.
 *
 * ## Dependencias
 * - `AuthViewModel`: utilizado para validar estado de sesión y recuperar cobros
 *   asociados al usuario logueado.
 * - `AppRepository`: instancia utilizada para acceder a la capa de datos
 *   (consultas de comerciantes, puestos y cobros).
 * - `DatabaseProvider`: proveedor de la base de datos Room sobre el cual
 *   `AppRepository` opera.
 * - Componentes gráficos reutilizables:
 *   - `CobroDialog`
 *   - `ComercianteDialog`
 *   - `PuestoDialog`
 *   - `GlassActionButton`
 *   - `TransaccionItem`
 *
 * ## Parámetros
 * @param navController Controlador de navegación usado para dirigir al usuario
 * hacia otras pantallas dentro de la aplicación.
 * @param authViewModel ViewModel responsable del estado de autenticación
 * y de exponer los cobros asociados al usuario.
 *
 * ## Comportamiento
 * - Inicializa el repositorio y solicita la carga de cobros del usuario.
 * - Presenta tarjetas informativas, opciones rápidas de creación y un
 *   resumen de actividades recientes.
 * - Despliega diálogos modales según las acciones seleccionadas por el usuario.
 * - Renderiza elementos utilizando Compose con un diseño basado en
 *   Superposición (Z-layers), sombras difusas y secciones organizadas.
 *
 * ## Retorno
 * No devuelve ningún valor. Su salida consiste en la composición declarativa
 * que representa la estructura visual del panel principal del usuario.
 *
 * @see AuthViewModel
 * @see AppRepository
 * @see CobroDialog
 * @see ComercianteDialog
 * @see PuestoDialog
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: AuthViewModel,
    context: Context
) {
    val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    val usuarioLogin = sharedPref.getString("usuario_login", "Usuario") ?: "Usuario"

    val cobros by viewModel.cobros.collectAsStateWithLifecycle(emptyList())
    val ultimosCobros = cobros.take(5)

    var showComercianteDialog by remember { mutableStateOf(false) }
    var showPuestoDialog by remember { mutableStateOf(false) }
    var showCobroDialog by remember { mutableStateOf(false) }

    val repository = remember { AppRepository(DatabaseProvider.getDatabase(context)) }

    rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    LaunchedEffect(Unit) {
        viewModel.cargarCobrosDelUsuario(context)
    }

    // === FONDO ===
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFDDE8F6), Color(0xFFFDFDFD))
                )
            )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(60.dp)
                .alpha(0.12f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, bottom = 0.dp, end = 16.dp, top = 16.dp)
        ) {

            // === BOTÓN SALIR  ===
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, end = 6.dp),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = {
                        with(sharedPref.edit()) {
                            clear()
                            apply()
                        }
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.height(38.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6C63FF)
                    )
                ) {
                    Text("Salir")
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // === HEADER PRINCIPAL + AVATAR FLOTANTE ===
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.TopCenter
            ) {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .align(Alignment.BottomCenter)
                        .padding(top = 20.dp),
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 1f)
                    ),
                    elevation = CardDefaults.cardElevation(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 36.dp, start = 20.dp, end = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Bienvenido, $usuarioLogin",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color(0xFF6C63FF)
                            ),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Gestión de cobros eficiente",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.Black.copy(alpha = 0.6f)
                            )
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .size(78.dp)
                        .offset(y = (-10).dp),
                    shape = RoundedCornerShape(50),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.88f)
                    ),
                    elevation = CardDefaults.cardElevation(20.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF6C63FF),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // === ACCIONES RÁPIDAS ===
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 1f)),
                elevation = CardDefaults.cardElevation(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Acciones rápidas",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF4A4A4A)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GlassActionButton(
                            modifier = Modifier.weight(1f).height(55.dp),
                            text = "Agregar Comerciante",
                            icon = Icons.Default.Person,
                            onClick = { showComercianteDialog = true }
                        )

                        GlassActionButton(
                            modifier = Modifier.weight(1f).height(55.dp),
                            text = "Agregar Puesto",
                            icon = Icons.Default.ShoppingCart,
                            onClick = { showPuestoDialog = true }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showCobroDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(55.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Agregar Cobro", color = Color.White)
                        }

                        OutlinedButton(
                            onClick = { navController.navigate("analytics") },
                            modifier = Modifier
                                .weight(1f)
                                .height(55.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Default.Analytics, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ver Análisis")
                        }
                    }

                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // === ACTIVIDAD RECIENTE ===
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 6.dp)
            ) {
                Icon(
                    Icons.Default.Analytics,
                    contentDescription = null,
                    tint = Color(0xFF6C63FF)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Actividad Reciente",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (ultimosCobros.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay transacciones recientes",
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(ultimosCobros) { detalle ->
                        TransaccionItem(detalle)
                    }
                }
            }
        }

        // === DIÁLOGOS ===
        if (showComercianteDialog) {
            ComercianteDialog(
                repository = repository,
                onDismiss = { showComercianteDialog = false },
                onSuccess = { viewModel.cargarCobrosDelUsuario(context) }
            )
        }

        if (showPuestoDialog) {
            PuestoDialog(
                repository = repository,
                onDismiss = { showPuestoDialog = false },
                onSuccess = { }
            )
        }

        if (showCobroDialog) {
            CobroDialog(
                repository = repository,
                context = context,
                onDismiss = { showCobroDialog = false },
                onSuccess = { viewModel.cargarCobrosDelUsuario(context) }
            )
        }
    }
}

@Composable
fun GlassActionButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = 1f),
            contentColor = Color(0xFF6C63FF)
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        Icon(icon, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}

@Composable
fun TransaccionItem(detalle: CobroDetalle) {
    val cobro = detalle.cobro
    val puesto = detalle.puesto
    val comerciante = detalle.comerciante

    val formato = NumberFormat.getCurrencyInstance(Locale("es", "SV"))
    formato.maximumFractionDigits = 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Color(0xFF6C63FF).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = Color(0xFF6C63FF),
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Puesto #${puesto.numero_puesto}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFF333333)
                    )
                )

                Text(
                    text = comerciante?.nombre_comerciante ?: "Comerciante no asignado",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF777777)
                    )
                )

                Text(
                    text = cobro.fecha_cobro,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF9A9A9A)
                    )
                )
            }

            Box(
                modifier = Modifier
                    .background(
                        color = Color(0xFF6C63FF).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formato.format(cobro.monto_cobrado),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFF6C63FF)
                    )
                )
            }
        }
    }
}
