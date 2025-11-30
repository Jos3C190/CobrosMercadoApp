package com.example.cobrosmercadoapp.data.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Representa un objeto compuesto que combina la información de un cobro
 * junto con los datos del puesto y su comerciante asociado.
 *
 * Esta clase **no es una entidad de Room**, sino un contenedor (POJO)
 * utilizado para mapear relaciones entre tablas mediante consultas con Room.
 *
 * Estructura:
 * - **Cobro** (entidad principal)
 * - **PuestoConComerciante** (relación 1 a 1, obtenida desde `id_puesto`)
 *
 * Es especialmente útil para vistas detalladas, reportes o para cargar
 * información completa sin múltiples consultas manuales.
 *
 * @property cobro Entidad base del cobro.
 * @property puestoConComerciante Relación que incluye el puesto y su comerciante.
 */
data class CobroDetalle(
    @Embedded val cobro: Cobro,

    @Relation(
        entity = Puesto::class,
        parentColumn = "id_puesto",
        entityColumn = "id_puesto"
    )
    val puestoConComerciante: PuestoConComerciante
) {

    /**
     * Acceso directo al puesto asociado al cobro,
     * proveniente de la relación `PuestoConComerciante`.
     */
    val puesto get() = puestoConComerciante.puesto

    /**
     * Acceso directo al comerciante propietario del puesto asociado al cobro.
     */
    val comerciante get() = puestoConComerciante.comerciante
}
