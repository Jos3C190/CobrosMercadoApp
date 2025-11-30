package com.example.cobrosmercadoapp.data.dao

import androidx.room.*
import com.example.cobrosmercadoapp.data.entity.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) principal del sistema.
 *
 * Define todas las operaciones de acceso a datos relacionadas con usuarios,
 * comerciantes, puestos y cobros.
 *
 * Se utilizan corrutinas (suspend) para operaciones de escritura/lectura
 * asincrónicas y Flow para consultas reactivas que reflejan cambios en la base
 * de datos en tiempo real.
 */
@Dao
interface AppDao {

    // =====================================================================
    // USUARIO
    // =====================================================================

    /**
     * Inserta un nuevo usuario o reemplaza el existente si coincide la clave única.
     *
     * @param usuario Usuario a registrar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuario(usuario: Usuario)

    /**
     * Obtiene un usuario por su nombre de login (sin verificar contraseña, ya que se hace en el repositorio).
     *
     * @param login Nombre de usuario.
     * @return Usuario encontrado o null si no existe.
     */
    @Query("SELECT * FROM Usuario WHERE usuario_login = :login LIMIT 1")
    suspend fun getUserByLogin(login: String): Usuario?

    /**
     * Obtiene la lista completa de usuarios registrados.
     */
    @Query("SELECT * FROM Usuario")
    suspend fun getAllUsuarios(): List<Usuario>

    // =====================================================================
    // COMERCIANTE CRUD
    // =====================================================================
    /**
     * Inserta un nuevo comerciante.
     *
     * @return ID generado para el comerciante.
     */
    @Insert suspend fun insertarComerciante(comerciante: Comerciante): Long

    /**
     * Actualiza los datos de un comerciante existente.
     */
    @Update suspend fun actualizarComerciante(comerciante: Comerciante)

    /**
     * Elimina un comerciante.
     *
     * **Nota:** Si el comerciante tiene puestos asociados,
     * estos también serán eliminados por la política CASCADE.
     */
    @Delete suspend fun eliminarComerciante(comerciante: Comerciante)

    /**
     * Obtiene en tiempo real todos los comerciantes ordenados alfabéticamente.
     */
    @Query("SELECT * FROM Comerciante ORDER BY nombre_comerciante ASC")
    fun getAllComerciantes(): Flow<List<Comerciante>>

    /**
     * Obtiene un comerciante por su identificador.
     */
    @Query("SELECT * FROM Comerciante WHERE id_comerciante = :id LIMIT 1")
    suspend fun getComercianteById(id: Int): Comerciante?

    /**
     * Realiza una búsqueda de comerciantes por nombre o por ID parcial.
     */
    @Query("""
        SELECT * FROM Comerciante 
        WHERE nombre_comerciante LIKE '%' || :query || '%' 
        OR id_comerciante LIKE '%' || :query || '%' 
        ORDER BY nombre_comerciante ASC
    """)
    fun buscarComerciante(query: String): Flow<List<Comerciante>>

    // =====================================================================
    // PUESTO CRUD
    // =====================================================================

    /**
     * Inserta un nuevo puesto.
     *
     * @return ID generado para el puesto.
     */
    @Insert suspend fun insertarPuesto(puesto: Puesto): Long

    /**
     * Actualiza la información de un puesto existente.
     */
    @Update suspend fun actualizarPuesto(puesto: Puesto)

    /**
     * Elimina un puesto del sistema.
     */
    @Delete suspend fun eliminarPuesto(puesto: Puesto)

    /**
     * Obtiene todos los puestos en tiempo real.
     */
    @Query("SELECT * FROM Puesto")
    fun getAllPuestos(): Flow<List<Puesto>>

    /**
     * Obtiene todos los puestos junto con su comerciante asociado.
     *
     * Esta consulta produce resultados utilizando el POJO PuestoConComerciante.
     */
    @Transaction
    @Query("SELECT * FROM Puesto")
    fun getAllPuestosConComerciante(): Flow<List<PuestoConComerciante>>

    /**
     * Obtiene un puesto por su identificador.
     */
    @Query("SELECT * FROM Puesto WHERE id_puesto = :id LIMIT 1")
    suspend fun getPuestoById(id: Int): Puesto?

    /**
     * Obtiene todos los puestos pertenecientes a un comerciante específico.
     */
    @Query("SELECT * FROM Puesto WHERE id_comerciante = :idComerciante")
    fun getPuestosPorComerciante(idComerciante: Int): Flow<List<Puesto>>

    /**
     * Realiza una búsqueda de puestos considerando:
     * - número de puesto
     * - ID del puesto
     * - nombre del comerciante
     *
     * Retorna una lista compuesta (PuestoConComerciante).
     */
    @Query("""
        SELECT Puesto.*, Comerciante.* 
        FROM Puesto 
        LEFT JOIN Comerciante ON Puesto.id_comerciante = Comerciante.id_comerciante 
        WHERE Puesto.numero_puesto LIKE '%' || :query || '%' 
        OR Puesto.id_puesto LIKE '%' || :query || '%' 
        OR Comerciante.nombre_comerciante LIKE '%' || :query || '%'
    """)
    fun buscarPuestos(query: String): Flow<List<PuestoConComerciante>>

    // =====================================================================
    // COBROS
    // =====================================================================

    /**
     * Registra un nuevo cobro.
     *
     * @return ID generado para el cobro.
     */
    @Insert suspend fun insertarCobro(cobro: Cobro): Long

    /**
     * Obtiene todos los cobros realizados por un usuario,
     * ordenados del más reciente al más antiguo.
     */
    @Query("SELECT * FROM Cobro WHERE id_usuario = :idUsuario ORDER BY fecha_cobro DESC")
    fun getCobrosPorUsuario(idUsuario: Int): Flow<List<Cobro>>

    /**
     * Actualiza los datos de un cobro existente.
     */
    @Update suspend fun actualizarCobro(cobro: Cobro)

    /**
     * Elimina un cobro.
     */
    @Delete suspend fun eliminarCobro(cobro: Cobro)

    /**
     * Obtiene todos los cobros con información detallada del puesto y comerciante (global, sin filtrar por usuario).
     */
    @Transaction
    @Query("SELECT * FROM Cobro ORDER BY fecha_cobro DESC")
    fun getAllCobrosDetalle(): Flow<List<CobroDetalle>>

    /**
     * Obtiene todos los cobros con información detallada del puesto y comerciante para un usuario específico.
     */
    @Transaction
    @Query("SELECT * FROM Cobro WHERE id_usuario = :idUsuario ORDER BY fecha_cobro DESC")
    fun getCobrosDetallePorUsuario(idUsuario: Int): Flow<List<CobroDetalle>>

    /**
     * Obtiene un cobro por su identificador.
     */
    @Query("SELECT * FROM Cobro WHERE id_cobro = :id")
    suspend fun getCobroById(id: Int): Cobro?

    /**
     * Obtiene el detalle completo de un cobro específico.
     */
    @Transaction
    @Query("SELECT * FROM Cobro WHERE id_cobro = :id LIMIT 1")
    suspend fun getCobroDetalleById(id: Int): CobroDetalle?

    /**
     * Realiza una búsqueda avanzada de cobros con detalles incluidos,
     * permitiendo filtrar por:
     * - usuario
     * - rango de fechas opcional
     * - coincidencias en número de puesto o nombre del comerciante
     *
     * Retorna una lista reactiva de CobroDetalle.
     */
    @Transaction
    @Query("""
        SELECT * FROM Cobro 
        WHERE id_usuario = :idUsuario 
        AND (:fechaInicio IS NULL OR fecha_cobro >= :fechaInicio) 
        AND (:fechaFin IS NULL OR fecha_cobro <= :fechaFin) 
        AND id_puesto IN ( 
            SELECT id_puesto FROM Puesto 
            LEFT JOIN Comerciante ON Puesto.id_comerciante = Comerciante.id_comerciante 
            WHERE Puesto.numero_puesto LIKE '%' || :query || '%' 
            OR Comerciante.nombre_comerciante LIKE '%' || :query || '%' 
        ) 
        ORDER BY fecha_cobro DESC
    """)
    fun buscarCobrosDetalle(
        idUsuario: Int,
        query: String,
        fechaInicio: String?,
        fechaFin: String?
    ): Flow<List<CobroDetalle>>
}