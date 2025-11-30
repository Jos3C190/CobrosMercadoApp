package com.example.cobrosmercadoapp.data.repository

import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.cobrosmercadoapp.data.AppDatabase
import com.example.cobrosmercadoapp.data.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repositorio principal de la aplicación.
 *
 * Esta clase actúa como una capa intermedia entre la capa de datos (Room)
 * y el ViewModel, exponiendo métodos suspend y Flows para consultar
 * y modificar la información almacenada en la base de datos local.
 *
 * El objetivo principal del repositorio es centralizar el acceso a los
 * DAOs y encapsular la lógica relacionada con la persistencia.
 *
 * @property db Instancia de la base de datos Room.
 */
class AppRepository(private val db: AppDatabase) {

    // ============================================================
    // USUARIO
    // ============================================================

    /**
     * Inserta un nuevo usuario en la base de datos.
     *
     * @param usuario Objeto [Usuario] a registrar.
     */
    suspend fun registrarUsuario(usuario: Usuario) {
        db.appDao().insertarUsuario(usuario)
    }

    /**
     * Obtiene un usuario por su nombre de login (sin verificar contraseña).
     *
     * @param login Nombre de usuario.
     * @return Usuario encontrado o null si no existe.
     */
    suspend fun getUserByLogin(login: String): Usuario? {
        return db.appDao().getUserByLogin(login)
    }

    /**
     * Realiza una consulta de inicio de sesión verificando usuario y contraseña.
     *
     * @param login Nombre de usuario escrito por el usuario.
     * @param pass Contraseña correspondiente (en plano, se verifica contra el hash).
     * @return El usuario si las credenciales son válidas, o null si no coincide.
     */
    suspend fun login(login: String, pass: String): Usuario? {
        val user = getUserByLogin(login) ?: return null
        val verified = BCrypt.verifyer().verify(pass.toCharArray(), user.contraseña).verified
        return if (verified) user else null
    }

    /**
     * Obtiene todos los usuarios almacenados.
     *
     * @return Lista completa de usuarios.
     */
    suspend fun getAllUsuarios(): List<Usuario> = db.appDao().getAllUsuarios()

    // ============================================================
    // COMERCIANTE
    // ============================================================

    /**
     * Inserta un nuevo comerciante.
     *
     * @param comerciante Instancia de [Comerciante].
     * @return El ID autogenerado del comerciante insertado.
     */
    suspend fun insertarComerciante(comerciante: Comerciante): Long = db.appDao().insertarComerciante(comerciante)

    /**
     * Actualiza un comerciante existente.
     *
     * @param comerciante Comerciante con datos actualizados.
     */
    suspend fun actualizarComerciante(comerciante: Comerciante) = db.appDao().actualizarComerciante(comerciante)

    /**
     * Elimina un comerciante.
     *
     * @param comerciante Instancia que se desea eliminar.
     */
    suspend fun eliminarComerciante(comerciante: Comerciante) = db.appDao().eliminarComerciante(comerciante)

    /**
     * Obtiene un flujo reactivo con todos los comerciantes.
     *
     * @return [Flow] con la lista de comerciantes.
     */
    fun getAllComerciantes(): Flow<List<Comerciante>> = db.appDao().getAllComerciantes()

    /**
     * Obtiene un comerciante por su ID.
     *
     * @param id Identificador del comerciante.
     * @return El comerciante o null si no existe.
     */
    suspend fun getComercianteById(id: Int): Comerciante? = db.appDao().getComercianteById(id)

    /**
     * Realiza una búsqueda de comerciantes por nombre o ID.
     *
     * @param query Texto usado como filtro.
     * @return [Flow] con los resultados coincidentes.
     */
    fun buscarComerciante(query: String): Flow<List<Comerciante>> = db.appDao().buscarComerciante(query)

    // ============================================================
    // PUESTO
    // ============================================================

    /**
     * Inserta un puesto en la base de datos.
     *
     * @param puesto Objeto [Puesto].
     * @return ID autogenerado del puesto.
     */
    suspend fun insertarPuesto(puesto: Puesto): Long = db.appDao().insertarPuesto(puesto)

    /**
     * Actualiza un puesto existente.
     *
     * @param puesto Instancia actualizada.
     */
    suspend fun actualizarPuesto(puesto: Puesto) = db.appDao().actualizarPuesto(puesto)

    /**
     * Elimina un puesto.
     *
     * @param puesto Puesto a eliminar.
     */
    suspend fun eliminarPuesto(puesto: Puesto) = db.appDao().eliminarPuesto(puesto)

    /**
     * Obtiene todos los puestos sin relaciones.
     *
     * @return [Flow] con lista de puestos.
     */
    fun getAllPuestos(): Flow<List<Puesto>> = db.appDao().getAllPuestos()

    /**
     * Obtiene los puestos junto con su comerciante relacionado.
     *
     * @return [Flow] con una lista de [PuestoConComerciante].
     */
    fun getAllPuestosConComerciante(): Flow<List<PuestoConComerciante>> = db.appDao().getAllPuestosConComerciante()

    /**
     * Obtiene un puesto por su ID.
     *
     * @param id Identificador único de puesto.
     * @return El puesto solicitado o null.
     */
    suspend fun getPuestoById(id: Int): Puesto? = db.appDao().getPuestoById(id)

    /**
     * Obtiene los puestos correspondientes a un comerciante específico.
     *
     * @param id ID del comerciante.
     * @return [Flow] de la lista de puestos asociados.
     */
    fun getPuestosPorComerciante(id: Int): Flow<List<Puesto>> = db.appDao().getPuestosPorComerciante(id)

    /**
     * Realiza una búsqueda de puestos filtrando por número de puesto,
     * ID o nombre del comerciante.
     *
     * @param query Texto de búsqueda.
     * @return [Flow] con resultados tipo [PuestoConComerciante].
     */
    fun buscarPuestos(query: String): Flow<List<PuestoConComerciante>> = db.appDao().buscarPuestos(query)

    // ============================================================
    // COBRO
    // ============================================================

    /**
     * Inserta un cobro.
     *
     * @param cobro [Cobro] a insertar.
     * @return ID autogenerado del cobro.
     */
    suspend fun insertarCobro(cobro: Cobro): Long = db.appDao().insertarCobro(cobro)

    /**
     * Obtiene los cobros registrados por un usuario.
     *
     * @param idUsuario ID del usuario que registró los cobros.
     * @return [Flow] con lista de cobros.
     */
    fun getCobrosPorUsuario(idUsuario: Int): Flow<List<Cobro>> = db.appDao().getCobrosPorUsuario(idUsuario)

    /**
     * Obtiene un cobro específico por su ID.
     *
     * @param id ID del cobro.
     * @return El cobro o null si no existe.
     */
    suspend fun getCobroById(id: Int): Cobro? = withContext(Dispatchers.IO) { db.appDao().getCobroById(id) }

    /**
     * Actualiza la información de un cobro.
     *
     * @param cobro Instancia actualizada.
     */
    suspend fun actualizarCobro(cobro: Cobro) {
        db.appDao().actualizarCobro(cobro)
    }

    /**
     * Elimina un cobro.
     *
     * @param cobro Instancia a eliminar.
     */
    suspend fun eliminarCobro(cobro: Cobro) {
        db.appDao().eliminarCobro(cobro)
    }

    /**
     * Obtiene todos los cobros junto con información del puesto y comerciante (global).
     *
     * @return [Flow] con lista de detalles.
     */
    fun getAllCobrosDetalle(): Flow<List<CobroDetalle>> = db.appDao().getAllCobrosDetalle()

    /**
     * Obtiene los cobros junto con información del puesto y comerciante para un usuario.
     *
     * @param idUsuario ID del usuario que realizó los cobros.
     * @return [Flow] con lista de detalles.
     */
    fun getCobrosDetallePorUsuario(idUsuario: Int): Flow<List<CobroDetalle>> = db.appDao().getCobrosDetallePorUsuario(idUsuario)

    /**
     * Obtiene un detalle de cobro por ID, incluyendo datos relacionados.
     *
     * @param id ID del cobro.
     * @return [CobroDetalle] o null.
     */
    suspend fun getCobroDetalleById(id: Int): CobroDetalle? = db.appDao().getCobroDetalleById(id)

    /**
     * Busca cobros con detalles filtrando por puesto, comerciante
     * y un rango de fechas opcional.
     *
     * @param idUsuario Usuario propietario de los cobros.
     * @param query Texto de búsqueda (puesto o comerciante).
     * @param fechaInicio Fecha mínima (puede ser null).
     * @param fechaFin Fecha máxima (puede ser null).
     * @return [Flow] con lista filtrada de detalles.
     */
    fun buscarCobrosDetalle(
        idUsuario: Int,
        query: String,
        fechaInicio: String?,
        fechaFin: String?
    ): Flow<List<CobroDetalle>> = db.appDao().buscarCobrosDetalle(idUsuario, query, fechaInicio, fechaFin)
}