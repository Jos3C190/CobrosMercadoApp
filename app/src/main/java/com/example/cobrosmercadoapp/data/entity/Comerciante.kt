package com.example.cobrosmercadoapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa a un comerciante dentro del sistema.
 *
 * Esta tabla almacena únicamente información básica del comerciante,
 * funcionando como un catálogo simple que se relaciona con otros elementos,
 * especialmente con los puestos del mercado.
 *
 * La clave primaria se genera automáticamente mediante Room.
 *
 * @property id_comerciante Identificador único del comerciante (autogenerado).
 * @property nombre_comerciante Nombre completo o nombre comercial del comerciante.
 */
@Entity(tableName = "Comerciante")
data class Comerciante(
    @PrimaryKey(autoGenerate = true)
    val id_comerciante: Int = 0,

    val nombre_comerciante: String
)
