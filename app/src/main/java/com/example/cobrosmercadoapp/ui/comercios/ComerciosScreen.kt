package com.example.cobrosmercadoapp.ui.comercios

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cobrosmercadoapp.data.entity.Comerciante
import com.example.cobrosmercadoapp.data.entity.PuestoConComerciante

private val PrimaryPurple = Color(0xFF6C63FF)
private val SecondaryPurple = Color(0xFF9C92FF)

/**
 * Pantalla principal de gestión de comercios.
 *
 * Esta pantalla permite administrar tanto **Comerciantes** como **Puestos**.
 * El usuario puede:
 *
 * - Buscar comerciantes o puestos mediante un campo de búsqueda.
 * - Alternar entre pestañas para visualizar comerciantes o puestos.
 * - Crear, editar o eliminar ambas entidades.
 *
 * La pantalla observa múltiples estados provenientes del [ComerciosViewModel],
 * incluyendo listas filtradas, listas completas (para dropdowns),
 * y banderas que controlan la visibilidad de los diálogos modales.
 *
 * Además, gestiona la navegación general mediante [NavController] (aunque
 * en esta pantalla no se navega directamente).
 *
 * @param navController Controlador de navegación general de la aplicación.
 * @param viewModel ViewModel responsable de exponer el estado, lógica de negocio
 * y operaciones CRUD de comerciantes y puestos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComerciosScreen(
    navController: NavController,
    viewModel: ComerciosViewModel
) {
    val comerciantes by viewModel.comerciantes.collectAsState()
    val allComerciantes by viewModel.allComerciantes.collectAsState()
    val puestos by viewModel.puestos.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val showCreateCom = viewModel.showCreateComerciante.collectAsState()
    val showEditCom = viewModel.showEditComerciante.collectAsState()
    val showDeleteCom = viewModel.showDeleteComerciante.collectAsState()

    val showCreatePuesto = viewModel.showCreatePuesto.collectAsState()
    val showEditPuesto = viewModel.showEditPuesto.collectAsState()
    val showDeletePuesto = viewModel.showDeletePuesto.collectAsState()

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                color = Color.Transparent
            ) {
                HeaderSection()
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (viewModel.selectedTab.value == 0) {
                        viewModel.showCreateComerciante.value = true
                    } else {
                        viewModel.showCreatePuesto.value = true
                    }
                },
                containerColor = PrimaryPurple
            ) {
                Text("+", color = Color.White, style = MaterialTheme.typography.titleLarge)
            }
        }
    ) { innerPadding ->
        MainContent(
            innerPadding = innerPadding,
            viewModel = viewModel,
            comerciantes = comerciantes,
            puestos = puestos,
            searchQuery = searchQuery
        )
    }

    // Modales Comerciante
    if (showCreateCom.value) {
        ComercianteDialogCreate(
            onDismiss = { viewModel.showCreateComerciante.value = false },
            onConfirm = { nombre -> viewModel.crearComerciante(nombre) }
        )
    }

    showEditCom.value?.let { editing ->
        ComercianteDialogEdit(
            comerciante = editing,
            onDismiss = { viewModel.showEditComerciante.value = null },
            onConfirm = { id, nombre -> viewModel.editarComerciante(id, nombre) }
        )
    }

    showDeleteCom.value?.let { c ->
        ConfirmDeleteComerciante(
            comerciante = c,
            onDismiss = { viewModel.showDeleteComerciante.value = null },
            onConfirm = { viewModel.eliminarComerciante(c) }
        )
    }

    // Modales Puesto
    if (showCreatePuesto.value) {
        PuestoDialogCreate(
            comerciantes = allComerciantes,
            puestosExistentes = puestos,
            onDismiss = { viewModel.showCreatePuesto.value = false },
            onConfirm = { numero, idCom -> viewModel.crearPuesto(numero, idCom) }
        )
    }

    showEditPuesto.value?.let { p ->
        PuestoDialogEdit(
            item = p,
            comerciantes = allComerciantes,
            puestosExistentes = puestos,
            onDismiss = { viewModel.showEditPuesto.value = null },
            onConfirm = { id, numero, idCom -> viewModel.editarPuesto(id, numero, idCom) }
        )
    }

    showDeletePuesto.value?.let { p ->
        ConfirmDeletePuesto(
            puesto = p,
            onDismiss = { viewModel.showDeletePuesto.value = null },
            onConfirm = { viewModel.eliminarPuesto(p) }
        )
    }
}

/**
 * Encabezado superior que muestra el título y subtítulo de la pantalla,
 * utilizando un degradado morado como fondo.
 */
@Composable
private fun HeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(listOf(PrimaryPurple, SecondaryPurple))
            )
            .padding(16.dp)
    ) {
        Column {
            Text(
                "Gestión de Comercios",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Crear, editar y eliminar Comerciantes y Puestos",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

/**
 * Contenido principal que incluye:
 * - Buscador
 * - Pestañas para cambiar entre comerciantes y puestos
 * - Listado dinámico según pestaña activa
 *
 * La separación en esta función mejora la legibilidad del `Scaffold`.
 */
@Composable
private fun MainContent(
    innerPadding: PaddingValues,
    viewModel: ComerciosViewModel,
    comerciantes: List<Comerciante>,
    puestos: List<PuestoConComerciante>,
    searchQuery: String
) {
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFDDE8F6), Color(0xFFFDFDFD))
                )
            )
    ) {
        // --- Buscador + Tabs ---
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Buscar por nombre, id o número de puesto") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            val selected = viewModel.selectedTab.collectAsState().value
            TabRow(selectedTabIndex = selected) {
                Tab(
                    selected = selected == 0,
                    onClick = { viewModel.setTab(0) },
                    text = { Text("Comerciantes") }
                )
                Tab(
                    selected = selected == 1,
                    onClick = { viewModel.setTab(1) },
                    text = { Text("Puestos") }
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // --- Lista ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            val selectedTab = viewModel.selectedTab.collectAsState().value
            if (selectedTab == 0) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    items(comerciantes, key = { it.id_comerciante }) { c ->
                        ComerciantesRow(
                            comerciante = c,
                            onEdit = { viewModel.showEditComerciante.value = c },
                            onDelete = { viewModel.showDeleteComerciante.value = c }
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(12.dp)
                ) {
                    items(puestos, key = { it.puesto.id_puesto }) { p ->
                        PuestosRow(
                            item = p,
                            onEdit = { viewModel.showEditPuesto.value = p },
                            onDelete = { viewModel.showDeletePuesto.value = p.puesto }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Fila individual que representa un [Comerciante].
 *
 * Muestra el nombre y su ID, junto con acciones de edición y eliminación.
 *
 * @param comerciante Instancia a mostrar.
 * @param onEdit Callback ejecutado al presionar "Editar".
 * @param onDelete Callback ejecutado al presionar "Eliminar".
 */
@Composable
private fun ComerciantesRow(
    comerciante: Comerciante,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = comerciante.nombre_comerciante,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "ID: ${comerciante.id_comerciante}",
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.wrapContentWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onEdit) { Text("Editar") }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onDelete) { Text("Eliminar", color = Color.Red) }
            }
        }
    }
}

/**
 * Fila individual que representa un Puesto junto con su comerciante asociado.
 *
 * @param item Objeto combinado que contiene información del puesto y su comerciante.
 * @param onEdit Acción para modificar el puesto.
 * @param onDelete Acción para eliminar el puesto.
 */
@Composable
private fun PuestosRow(
    item: PuestoConComerciante,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Puesto: ${item.puesto.numero_puesto}",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.comerciante?.nombre_comerciante ?: "N/A",
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.wrapContentWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onEdit) { Text("Editar") }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onDelete) { Text("Eliminar", color = Color.Red) }
            }
        }
    }
}
