package com.example.cobrosmercadoapp.data

import android.content.Context
import androidx.room.Room

/**
 * Proveedor singleton de la base de datos Room.
 *
 * Esta clase encapsula la creación y gestión de la instancia única de
 * [AppDatabase], asegurando que toda la aplicación utilice la misma conexión
 * a la base de datos.
 *
 * ### Características clave:
 *
 * - **Patrón Singleton:**
 *   Mantiene una única instancia en memoria mediante la propiedad
 *   [instance], evitando múltiples aperturas de la base de datos.
 *
 * - **Bloque sincronizado:**
 *   `synchronized(this)` garantiza que, aunque varios hilos soliciten la
 *   base de datos al mismo tiempo, solo uno podrá crearla, evitando
 *   condiciones de carrera.
 *
 * - **Uso del contexto de aplicación:**
 *   `context.applicationContext` evita fugas de memoria en actividades.
 *
 * - **Creación de la base de datos:**
 *   Se utiliza `Room.databaseBuilder` apuntando a la clase [AppDatabase].
 *
 * @object DatabaseProvider Punto centralizado para obtener la base de datos.
 */
object DatabaseProvider {

    /** Instancia única en memoria de la base de datos. */
    private var instance: AppDatabase? = null

    /**
     * Devuelve la instancia única de [AppDatabase], creándola si aún no existe.
     *
     * @param context Contexto utilizado para obtener el `applicationContext`.
     * @return Instancia de la base de datos lista para utilizarse.
     */
    fun getDatabase(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "cobros_mercado.db"
            ).build().also { instance = it }
        }
    }
}
