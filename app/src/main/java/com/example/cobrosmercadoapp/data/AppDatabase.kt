package com.example.cobrosmercadoapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.cobrosmercadoapp.data.dao.AppDao
import com.example.cobrosmercadoapp.data.entity.*

/**
 * Base de datos principal de la aplicación utilizando Room.
 *
 * Esta clase define la configuración de la base de datos local, incluyendo
 * las entidades registradas y el acceso al DAO principal. Room genera
 * automáticamente el código necesario para la gestión de la base de datos.
 *
 * ## Entidades incluidas:
 * - [Usuario]
 * - [Comerciante]
 * - [Puesto]
 * - [Cobro]
 *
 * ## Versión de la base de datos:
 * - Se utiliza `version = 3`, lo cual indica que la estructura ha sido
 *   modificada desde una versión previa. Si se realizan cambios en futuras
 *   actualizaciones (nuevas tablas, columnas, relaciones), es necesario
 *   incrementar esta versión y proporcionar una migración apropiada.
 *
 * ## Exportación de esquema:
 * - `exportSchema = false` desactiva la exportación del esquema en archivos
 *   JSON. Puede activarse en proyectos donde se requiera mantener historial
 *   de estructuras.
 *
 * Esta clase se implementa como `abstract` ya que Room se encargará de
 * generar la implementación concreta durante la compilación.
 */
@Database(
    entities = [Usuario::class, Comerciante::class, Puesto::class, Cobro::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Provee el DAO principal utilizado para el acceso a datos.
     *
     * @return Instancia de [AppDao] que contiene todas las operaciones CRUD.
     */
    abstract fun appDao(): AppDao
}
