package com.example.cobrosmercadoapp.ui.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cobrosmercadoapp.data.entity.CobroDetalle
import com.example.cobrosmercadoapp.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel responsable de gestionar la lógica relacionada con los cobros:
 * búsqueda, filtrado por fechas y carga asociada al usuario autenticado.
 *
 * Esta clase actúa como intermediaria entre la capa UI y el [AppRepository].
 * Expone estados inmutables mediante [StateFlow] para permitir recomposición
 * automática en Jetpack Compose.
 *
 * Funcionalidades principales:
 * - Manejo de texto de búsqueda.
 * - Filtros por fecha de inicio y fin.
 * - Activación/desactivación del filtro avanzado.
 * - Carga reactiva de cobros según usuario y filtros activos.
 *
 * @property repository Fuente de datos que provee operaciones relacionadas
 * con consultas de cobros y usuarios.
 */
class PaymentsViewModel(
    private val repository: AppRepository
) : ViewModel() {

    // -------------------------------------------------------------------------
    // Estados de filtros y búsqueda
    // -------------------------------------------------------------------------

    /** Texto utilizado para filtrar cobros por coincidencia en campos relevantes. */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /** Fecha inicial del rango de filtrado (formato yyyy-MM-dd). */
    private val _fechaInicio = MutableStateFlow("")
    val fechaInicio: StateFlow<String> = _fechaInicio.asStateFlow()

    /** Fecha final del rango de filtrado (formato yyyy-MM-dd). */
    private val _fechaFin = MutableStateFlow("")
    val fechaFin: StateFlow<String> = _fechaFin.asStateFlow()

    /** Indica si el filtro por rango de fechas está activo. */
    private val _filtroActivo = MutableStateFlow(false)
    val filtroActivo: StateFlow<Boolean> = _filtroActivo.asStateFlow()

    // -------------------------------------------------------------------------
    // Estado de cobros cargados
    // -------------------------------------------------------------------------

    /** Lista de cobros resultante de aplicar filtros y búsquedas. */
    private val _cobros = MutableStateFlow<List<CobroDetalle>>(emptyList())
    val cobros: StateFlow<List<CobroDetalle>> = _cobros.asStateFlow()

    // -------------------------------------------------------------------------
    // Estado del usuario autenticado
    // -------------------------------------------------------------------------

    /**
     * Identificador único del usuario actual.
     *
     * Se carga únicamente una vez mediante [loadIdUsuario], y desencadena la
     * carga reactiva de cobros.
     */
    private val _idUsuario = MutableStateFlow<Int?>(null)
    val idUsuario: StateFlow<Int?> = _idUsuario.asStateFlow()

    // -------------------------------------------------------------------------
    // Fecha actual
    // -------------------------------------------------------------------------

    /** Valor por defecto para filtros cuando no se han especificado fechas. */
    private val hoy: String by lazy {
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        formato.format(Date())
    }

    // -------------------------------------------------------------------------
    // Actualización de estados
    // -------------------------------------------------------------------------

    /** Actualiza el texto de búsqueda. */
    fun updateSearchQuery(value: String) {
        _searchQuery.value = value
    }

    /** Define la fecha de inicio del rango a filtrar. */
    fun updateFechaInicio(value: String) {
        _fechaInicio.value = value
    }

    /** Define la fecha de finalización del rango a filtrar. */
    fun updateFechaFin(value: String) {
        _fechaFin.value = value
    }

    /**
     * Activa o desactiva el filtrado por fechas.
     *
     * @param active `true` para activar el filtro; `false` para ignorarlo.
     */
    fun setFiltroActivo(active: Boolean) {
        _filtroActivo.value = active
    }

    /**
     * Restablece todos los filtros de fecha.
     */
    fun resetFiltros() {
        _fechaInicio.value = ""
        _fechaFin.value = ""
        _filtroActivo.value = false
    }

    // -------------------------------------------------------------------------
    // Carga de usuario
    // -------------------------------------------------------------------------

    /**
     * Obtiene la información del usuario a partir de su nombre de login y
     * almacena su identificador.
     *
     * @param usuarioLogin Nombre de usuario utilizado al iniciar sesión.
     */
    fun loadIdUsuario(usuarioLogin: String) {
        viewModelScope.launch {
            val usuario = repository.getAllUsuarios()
                .find { it.usuario_login == usuarioLogin }
            _idUsuario.value = usuario?.id_usuario
        }
    }

    // -------------------------------------------------------------------------
    // Carga reactiva de cobros
    // -------------------------------------------------------------------------

    init {
        viewModelScope.launch {
            idUsuario.collectLatest { id ->
                if (id != null) {
                    combineFlowsAndLoadCobros(id)
                }
            }
        }
    }

    /**
     * Observa los estados relacionados con búsqueda y filtrado.
     * Cada cambio provoca un recálculo de la lista de cobros.
     *
     * @param id Identificador del usuario cuyo historial de cobros se carga.
     */
    private fun combineFlowsAndLoadCobros(id: Int) {
        viewModelScope.launch { searchQuery.collectLatest { loadCobros(id) } }
        viewModelScope.launch { fechaInicio.collectLatest { loadCobros(id) } }
        viewModelScope.launch { fechaFin.collectLatest { loadCobros(id) } }
        viewModelScope.launch { filtroActivo.collectLatest { loadCobros(id) } }
    }

    /**
     * Carga los cobros del usuario aplicando filtros y texto de búsqueda.
     * Si el filtro de fecha no está activo, se utiliza la fecha actual
     * como rango predeterminado.
     *
     * @param id Identificador del usuario dueño de los cobros.
     */
    private suspend fun loadCobros(id: Int) {
        val start = if (_filtroActivo.value)
            _fechaInicio.value.takeIf { it.isNotBlank() } ?: hoy
        else hoy

        val end = if (_filtroActivo.value)
            _fechaFin.value.takeIf { it.isNotBlank() } ?: hoy
        else hoy

        repository.buscarCobrosDetalle(id, _searchQuery.value, start, end)
            .collectLatest { _cobros.value = it }
    }
}
