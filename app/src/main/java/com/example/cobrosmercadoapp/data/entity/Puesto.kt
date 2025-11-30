package com.example.cobrosmercadoapp.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad que representa un puesto dentro del mercado.
 *
 * Cada puesto está asociado a un comerciante, formando una relación
 * uno-a-uno o uno-a-muchos según la lógica de negocio.
 * Si un comerciante es eliminado del sistema, todos sus puestos asociados
 * también se eliminan automáticamente (ON DELETE CASCADE), preservando
 * la coherencia de los datos.
 *
 * @property id_puesto Identificador único del puesto (autogenerado).
 * @property numero_puesto Código o número asignado al puesto dentro del mercado.
 * @property id_comerciante Identificador del comerciante propietario del puesto.
 */
@Entity(
    tableName = "Puesto",
    foreignKeys = [
        ForeignKey(
            entity = Comerciante::class,
            parentColumns = ["id_comerciante"],
            childColumns = ["id_comerciante"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["numero_puesto"], unique = true)]
)
data class Puesto(
    @PrimaryKey(autoGenerate = true)
    val id_puesto: Int = 0,

    val numero_puesto: String,

    val id_comerciante: Int
)
