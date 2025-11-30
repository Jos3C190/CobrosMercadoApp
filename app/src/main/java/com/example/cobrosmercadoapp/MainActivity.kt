package com.example.cobrosmercadoapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.cobrosmercadoapp.data.DatabaseProvider
import com.example.cobrosmercadoapp.data.repository.AppRepository
import com.example.cobrosmercadoapp.ui.analytics.AnalyticsScreen
import com.example.cobrosmercadoapp.ui.auth.AuthViewModel
import com.example.cobrosmercadoapp.ui.auth.LoginScreen
import com.example.cobrosmercadoapp.ui.auth.RegisterScreen
import com.example.cobrosmercadoapp.ui.comercios.ComerciosScreen
import com.example.cobrosmercadoapp.ui.comercios.ComerciosViewModel
import com.example.cobrosmercadoapp.ui.home.HomeScreen
import com.example.cobrosmercadoapp.ui.payments.PaymentsScreen
import com.example.cobrosmercadoapp.ui.payments.PaymentsViewModel
import com.example.cobrosmercadoapp.ui.map.MapDetailScreen
import com.example.cobrosmercadoapp.ui.splash.SplashScreen
import com.example.cobrosmercadoapp.ui.theme.CobrosMercadoAppTheme

/**
 * Actividad principal de la aplicación.
 *
 * Esta actividad inicializa la base de datos, el repositorio y los ViewModels
 * principales, y define el contenido Compose a través de [CobrosMercadoApp].
 *
 * ## Responsabilidades:
 * - Crear e inyectar dependencias de capa de datos (Room + Repository).
 * - Instanciar ViewModels que no se gestionan con Hilt (patrón manual).
 * - Renderizar la UI principal mediante composición.
 */
class MainActivity : ComponentActivity() {

    private lateinit var comerciosVM: ComerciosViewModel
    private lateinit var paymentsVM: PaymentsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = DatabaseProvider.getDatabase(this)
        val repository = AppRepository(database)

        val authViewModel = AuthViewModel(repository)
        comerciosVM = ComerciosViewModel(repository)
        paymentsVM = PaymentsViewModel(repository)

        // Cargar estado ANTES de setContent
        authViewModel.loadLoginState(this)

        setContent {
            CobrosMercadoApp(
                authViewModel = authViewModel,
                comerciosVM = comerciosVM,
                paymentsVM = paymentsVM,
                repository = repository,
                context = this
            )
        }
    }

}

/**
 * Composable raíz que define el árbol de navegación, el estado global
 * y el layout principal de la aplicación.
 *
 * ## Funciones clave:
 * - Gestiona el estado de autenticación y la restauración automática de sesión.
 * - Define navegación con rutas estructuradas mediante [NavHost].
 * - Muestra u oculta la barra inferior según la pantalla actual.
 *
 * @param authViewModel ViewModel de autenticación del usuario.
 * @param comerciosVM ViewModel encargado de comerciantes y puestos.
 * @param paymentsVM ViewModel encargado de cobros y pagos.
 * @param repository Repositorio general para las operaciones de datos.
 * @param context Contexto necesario para algunas operaciones de sistema.
 */
@Composable
fun CobrosMercadoApp(
    authViewModel: AuthViewModel,
    comerciosVM: ComerciosViewModel,
    paymentsVM: PaymentsViewModel,
    repository: AppRepository,
    context: Context
) {
    val navController = rememberNavController()

    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Oculta la barra inferior en pantallas especiales
    val hideBottomBar = currentRoute in listOf("login", "register", "splash")

    CobrosMercadoAppTheme {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (!hideBottomBar) {
                    NavigationBar(
                        modifier = Modifier
                            .padding(12.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                            )
                            .shadow(
                                elevation = 12.dp,
                                shape = RoundedCornerShape(22.dp),
                                ambientColor = Color(0xFFB0BEC5).copy(alpha = 0.25f),
                                spotColor = Color(0xFFFFFFFF).copy(alpha = 0.35f)
                            ),
                        containerColor = Color.Transparent
                    ) {
                        val items = listOf(
                            Screen.Home,
                            Screen.Stores,
                            Screen.Payments,
                            Screen.Analytics
                        )

                        items.forEach { screen ->
                            val selected = currentRoute == screen.route

                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.label,
                                        tint = if (selected)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier
                                            .padding(6.dp)
                                            .size(if (selected) 28.dp else 24.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = screen.label,
                                        color = if (selected)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->

            /**
             * Árbol de navegación de la aplicación.
             */
            NavHost(
                navController = navController,
                startDestination = "splash",
                modifier = Modifier.padding(innerPadding)
            ) {

                composable("splash") {
                    SplashScreen(
                        navController = navController,
                        isLoggedIn = isLoggedIn
                    )
                }

                composable("login") {
                    LoginScreen(navController, authViewModel, context)
                }

                composable("register") {
                    RegisterScreen(navController, authViewModel, context)
                }

                composable(Screen.Home.route) {
                    HomeScreen(navController, authViewModel, context)
                }

                composable(Screen.Payments.route) {
                    PaymentsScreen(
                        viewModel = paymentsVM,
                        repository = repository,
                        context = context,
                        navController = navController
                    )
                }

                composable(Screen.Analytics.route) {
                    AnalyticsScreen(
                        navController = navController,
                        context = context,
                        repository = repository
                    )
                }

                composable(
                    "map/{cobroId}",
                    arguments = listOf(navArgument("cobroId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val cobroId = backStackEntry.arguments?.getInt("cobroId")!!
                    MapDetailScreen(
                        cobroId = cobroId,
                        repository = repository,
                        navController = navController
                    )
                }

                composable(Screen.Stores.route) {
                    ComerciosScreen(
                        navController = navController,
                        viewModel = comerciosVM
                    )
                }
            }
        }
    }
}

/**
 * Representa una pantalla dentro del sistema de navegación de la aplicación.
 *
 * Cada objeto define:
 * - la ruta de navegación [route],
 * - la etiqueta visual [label],
 * - el icono utilizado en la barra inferior [icon].
 */
sealed class Screen(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Home : Screen("home", "Inicio", Icons.Default.Home)
    object Stores : Screen("comercios", "Comercios", Icons.Default.Storefront)
    object Payments : Screen("payments", "Pagos", Icons.Default.Payment)
    object Analytics : Screen("analytics", "Análisis", Icons.Default.Analytics)
}
