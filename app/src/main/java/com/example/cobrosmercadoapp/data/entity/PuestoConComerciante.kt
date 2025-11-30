package com.example.cobrosmercadoapp.data.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Modelo compuesto que representa un puesto junto con su comerciante asociado.
 *
 * Esta clase **no es una entidad de Room**, sino un POJO utilizado para cargar
 * relaciones entre tablas mediante consultas que utilizan `@Relation`.
 *
 * Relación representada:
 * - Un puesto pertenece a un comerciante.
 * - La relación se realiza a través de la columna `id_comerciante`.
 *
 * Caso especial:
 * - El comerciante puede ser `null` cuando la referencia se ha eliminado y la
 *   entidad `Puesto` quedó huérfana (situación poco común pero posible si en el
 *   futuro se cambia la política de borrado).
 *
 * Este objeto es frecuentemente utilizado al obtener listas o detalles de puestos
 * donde se requiere mostrar el nombre del comerciante sin ejecutar múltiples queries.
 *
 * @property puesto Entidad base del puesto.
 * @property comerciante Datos del comerciante propietario; puede ser nulo.
 */
data class PuestoConComerciante(
    @Embedded val puesto: Puesto,

    @Relation(
        parentColumn = "id_comerciante",
        entityColumn = "id_comerciante"
    )
    val comerciante: Comerciante?
)
