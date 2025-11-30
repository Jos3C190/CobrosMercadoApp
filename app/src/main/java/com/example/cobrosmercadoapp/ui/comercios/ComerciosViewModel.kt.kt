package com.example.cobrosmercadoapp.ui.comercios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cobrosmercadoapp.data.entity.Comerciante
import com.example.cobrosmercadoapp.data.entity.Puesto
import com.example.cobrosmercadoapp.data.entity.PuestoConComerciante
import com.example.cobrosmercadoapp.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ViewModel responsable de gestionar el estado y las operaciones relacionadas con
 * comerciantes y puestos dentro de la aplicación. Actúa como puente entre la capa
 * de UI y el repositorio, exponiendo datos reactivos mediante `StateFlow` y realizando
 * operaciones CRUD.
 *
 * @property repository Fuente de datos utilizada para obtener y modificar información
 * de comerciantes y puestos.
 */
class ComerciosViewModel(
    private val repository: AppRepository
) : ViewModel() {

    /**
     * Índice de la pestaña seleccionada en la interfaz.
     * - `0` → Comerciante
     * - `1` → Puestos
     */
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    /**
     * Cambia la pestaña activa.
     * @param index Índice de la pestaña.
     */
    fun setTab(index: Int) {
        _selectedTab.value = index
    }

    /** Cadena actual utilizada para filtrar comerciantes y puestos. */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /**
     * Actualiza el término de búsqueda y recarga los datos filtrados.
     * @param value Texto ingresado por el usuario.
     */
    fun updateSearchQuery(value: String) {
        _searchQuery.value = value
        reloadData()
    }

    /** Lista filtrada de comerciantes mostrada en la UI. */
    private val _comerciantes = MutableStateFlow<List<Comerciante>>(emptyList())
    val comerciantes: StateFlow<List<Comerciante>> = _comerciantes.asStateFlow()

    /** Lista completa de comerciantes, usada en diálogos de creación/edición de puestos. */
    private val _allComerciantes = MutableStateFlow<List<Comerciante>>(emptyList())
    val allComerciantes: StateFlow<List<Comerciante>> = _allComerciantes.asStateFlow()

    /** Lista filtrada de puestos con su comerciante asociado. */
    private val _puestos = MutableStateFlow<List<PuestoConComerciante>>(emptyList())
    val puestos: StateFlow<List<PuestoConComerciante>> = _puestos.asStateFlow()

    /** Estados de visibilidad de diálogos relacionados con comerciantes y puestos. */
    val showCreateComerciante = MutableStateFlow(false)
    val showEditComerciante = MutableStateFlow<Comerciante?>(null)
    val showDeleteComerciante = MutableStateFlow<Comerciante?>(null)

    val showCreatePuesto = MutableStateFlow(false)
    val showEditPuesto = MutableStateFlow<PuestoConComerciante?>(null)
    val showDeletePuesto = MutableStateFlow<Puesto?>(null)

    init {
        // Carga inicial de comerciantes sin filtro.
        viewModelScope.launch {
            repository.getAllComerciantes().collectLatest { list ->
                _allComerciantes.value = list
            }
        }

        // Carga inicial de datos filtrados o completos.
        reloadData()
    }

    /**
     * Recarga comerciantes y puestos, aplicando filtro si existe una búsqueda activa.
     * Los datos se obtienen de manera reactiva desde el repositorio.
     */
    private fun reloadData() {
        val query = _searchQuery.value.trim()

        viewModelScope.launch {
            if (query.isEmpty()) {
                launch {
                    repository.getAllComerciantes().collectLatest { list ->
                        _comerciantes.value = list
                    }
                }
                launch {
                    repository.getAllPuestosConComerciante().collectLatest { list ->
                        _puestos.value = list
                    }
                }
            } else {
                launch {
                    repository.buscarComerciante(query).collectLatest { list ->
                        _comerciantes.value = list
                    }
                }
                launch {
                    repository.buscarPuestos(query).collectLatest { list ->
                        _puestos.value = list
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // CRUD Comerciante
    // -------------------------------------------------------------------------

    /**
     * Crea un nuevo comerciante.
     * @param nombre Nombre del comerciante.
     */
    fun crearComerciante(nombre: String) {
        viewModelScope.launch {
            repository.insertarComerciante(Comerciante(nombre_comerciante = nombre))
            showCreateComerciante.value = false
        }
    }

    /**
     * Actualiza los datos de un comerciante existente.
     * @param id Identificador del comerciante.
     * @param nombre Nuevo nombre.
     */
    fun editarComerciante(id: Int, nombre: String) {
        viewModelScope.launch {
            repository.actualizarComerciante(Comerciante(id_comerciante = id, nombre_comerciante = nombre))
            showEditComerciante.value = null
        }
    }

    /**
     * Elimina un comerciante del sistema.
     * @param comerciante Entidad del comerciante a eliminar.
     */
    fun eliminarComerciante(comerciante: Comerciante) {
        viewModelScope.launch {
            repository.eliminarComerciante(comerciante)
            showDeleteComerciante.value = null
        }
    }

    // -------------------------------------------------------------------------
    // CRUD Puesto
    // -------------------------------------------------------------------------

    /**
     * Crea un nuevo puesto asociado a un comerciante.
     * @param numero Número o código del puesto.
     * @param idComerciante Identificador del comerciante dueño del puesto.
     */
    fun crearPuesto(numero: String, idComerciante: Int) {
        viewModelScope.launch {
            repository.insertarPuesto(Puesto(numero_puesto = numero, id_comerciante = idComerciante))
            showCreatePuesto.value = false
        }
    }

    /**
     * Actualiza los datos de un puesto.
     * @param id Identificador del puesto.
     * @param numero Nuevo número del puesto.
     * @param idComerciante Nuevo comerciante asociado.
     */
    fun editarPuesto(id: Int, numero: String, idComerciante: Int) {
        viewModelScope.launch {
            repository.actualizarPuesto(
                Puesto(id_puesto = id, numero_puesto = numero, id_comerciante = idComerciante)
            )
            showEditPuesto.value = null
        }
    }

    /**
     * Elimina un puesto.
     * @param puesto Entidad del puesto a eliminar.
     */
    fun eliminarPuesto(puesto: Puesto) {
        viewModelScope.launch {
            repository.eliminarPuesto(puesto)
            showDeletePuesto.value = null
        }
    }
}
