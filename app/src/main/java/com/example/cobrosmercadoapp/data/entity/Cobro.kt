package com.example.cobrosmercadoapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Representa un cobro realizado a un puesto dentro del sistema.
 *
 * Esta entidad almacena información detallada sobre la transacción,
 * incluyendo montos, ubicación del cobro y el usuario que lo registró.
 *
 * Relaciones:
 * - Cada cobro pertenece a un **puesto** (`id_puesto`), relación obligatoria.
 *   Si el puesto es eliminado, todos sus cobros asociados también se eliminan
 *   (ON DELETE CASCADE).
 *
 * - Cada cobro puede estar asociado a un **usuario** (`id_usuario`) que lo registró.
 *   Si el usuario es eliminado, el valor se establece en null
 *   (ON DELETE SET NULL), preservando el historial del cobro.
 *
 * La propiedad `vuelto` se calcula automáticamente como la diferencia entre
 * el dinero recibido y el monto cobrado.
 *
 * @property id_cobro Identificador único del cobro (autogenerado por Room).
 * @property id_puesto Identificador del puesto al que pertenece el cobro.
 * @property monto_cobrado Monto total que debía pagarse.
 * @property dinero_recibido Monto recibido por el cobrador.
 * @property vuelto Diferencia entre dinero recibido y monto cobrado.
 * @property fecha_cobro Fecha del cobro en formato `yyyy-MM-dd`.
 * @property latitud Coordenada opcional donde se realizó el cobro.
 * @property longitud Coordenada opcional donde se realizó el cobro.
 * @property id_usuario Usuario que registró el cobro (opcional).
 */
@Entity(
    tableName = "Cobro",
    foreignKeys = [
        ForeignKey(
            entity = Puesto::class,
            parentColumns = ["id_puesto"],
            childColumns = ["id_puesto"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id_usuario"],
            childColumns = ["id_usuario"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Cobro(
    @PrimaryKey(autoGenerate = true)
    val id_cobro: Int? = null,

    val id_puesto: Int,

    val monto_cobrado: Double,

    val dinero_recibido: Double,

    // Calculado automáticamente al crear el objeto
    val vuelto: Double = dinero_recibido - monto_cobrado,

    val fecha_cobro: String,  // formato esperado: yyyy-MM-dd

    val latitud: Double?,

    val longitud: Double?,

    val id_usuario: Int?
)
