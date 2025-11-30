package com.example.cobrosmercadoapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa a un usuario del sistema.
 *
 * Esta tabla almacena la información básica necesaria para identificar y autenticar
 * a los usuarios que tienen acceso a la aplicación.
 *
 * Nota de seguridad: La propiedad contraseña se almacena como un hash seguro (usando BCrypt).
 * Nunca se guarda la contraseña original en texto plano.
 *
 * @property id_usuario Identificador único del usuario (autogenerado).
 * @property nombre Nombre del usuario.
 * @property apellido Apellido del usuario.
 * @property usuario_login Nombre de usuario utilizado para iniciar sesión.
 * @property contraseña Hash de la contraseña del usuario.
 */
@Entity(tableName = "Usuario")
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id_usuario: Int = 0,
    val nombre: String,
    val apellido: String,
    val usuario_login: String,
    val contraseña: String
)