package com.example.cobrosmercadoapp.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.cobrosmercadoapp.data.entity.CobroDetalle
import com.example.cobrosmercadoapp.data.entity.Usuario
import com.example.cobrosmercadoapp.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel encargado de toda la lógica de autenticación y gestión de sesión de la aplicación.
 *
 * Coordina el registro, inicio de sesión, cierre de sesión y carga de cobros asociados al usuario
 * autenticado. Persiste el estado de autenticación mediante [SharedPreferences] y expone los
 * estados observables a través de [StateFlow] para que la UI reaccione de forma reactiva.
 *
 * @property repository Instancia del repositorio que proporciona acceso a datos locales
 *   (usuarios, cobros, etc.).
 */
class AuthViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _cobros = MutableStateFlow<List<CobroDetalle>>(emptyList())
    /**
     * Lista de cobros del usuario autenticado con información detallada del puesto y comerciante.
     *
     * Se actualiza mediante [cargarCobrosDelUsuario]. Valor inicial: lista vacía.
     */
    val cobros: StateFlow<List<CobroDetalle>> = _cobros.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    /**
     * Indica si existe una sesión activa en la aplicación.
     *
     * Su valor se sincroniza con SharedPreferences mediante [loadLoginState], [setLoggedIn] y [logout].
     */
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    /**
     * Carga el estado de autenticación almacenado en SharedPreferences.
     *
     * Debe llamarse al inicio de la aplicación (por ejemplo, en `SplashScreen` o `MainActivity`).
     * La operación es **síncrona** para evitar problemas de timing en el arranque.
     *
     * @param context Contexto necesario para acceder a SharedPreferences.
     */
    fun loadLoginState(context: Context) {
        val sp = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        _isLoggedIn.value = sp.getBoolean("isLoggedIn", false)
    }

    /**
     * Marca la sesión como activa y persiste el nombre de usuario autenticado.
     *
     * @param context Contexto para acceder a SharedPreferences.
     * @param usuarioLogin Nombre de usuario (login) que inició sesión.
     */
    fun setLoggedIn(context: Context, usuarioLogin: String) {
        viewModelScope.launch {
            val sp = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
            sp.edit()
                .putBoolean("isLoggedIn", true)
                .putString("usuario_login", usuarioLogin)
                .apply()

            _isLoggedIn.value = true
        }
    }

    /**
     * Cierra la sesión actual.
     *
     * Elimina todos los datos de autenticación de SharedPreferences y restablece los estados
     * observables ([isLoggedIn] → false, [cobros] → lista vacía).
     *
     * @param context Contexto necesario para limpiar SharedPreferences.
     */
    fun logout(context: Context) {
        viewModelScope.launch {
            context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()

            _cobros.value = emptyList()
            _isLoggedIn.value = false
        }
    }

    /**
     * Registra un nuevo usuario en la base de datos local.
     *
     * La contraseña se hashea con BCrypt (cost factor 12) antes de almacenarse.
     * Si el nombre de usuario ya existe, la operación falla.
     *
     * @param nombre Nombre completo del usuario.
     * @param apellido Apellido del usuario.
     * @param usuarioLogin Login único (clave de autenticación).
     * @param contraseña Contraseña en texto plano.
     * @param onResult Callback con el resultado:
     *   - `(true, null)` → registro exitoso.
     *   - `(false, mensaje)` → fallo con motivo.
     */
    fun registrar(
        nombre: String,
        apellido: String,
        usuarioLogin: String,
        contraseña: String,
        onResult: (success: Boolean, errorMessage: String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val existe = repository.getUserByLogin(usuarioLogin) != null
                if (existe) {
                    onResult(false, "El usuario ya existe")
                    return@launch
                }

                val hashedPassword = BCrypt.withDefaults().hashToString(12, contraseña.toCharArray())
                val nuevoUsuario = Usuario(
                    nombre = nombre,
                    apellido = apellido,
                    usuario_login = usuarioLogin,
                    contraseña = hashedPassword
                )

                repository.registrarUsuario(nuevoUsuario)
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, "Error al registrar: ${e.message}")
            }
        }
    }

    /**
     * Intenta autenticar al usuario con las credenciales proporcionadas.
     *
     * La verificación de contraseña se realiza comparando el hash almacenado mediante BCrypt.
     *
     * @param usuarioLogin Login del usuario.
     * @param contraseña Contraseña en texto plano.
     * @param onResult Callback con el resultado:
     *   - `(true, null)` → autenticación exitosa.
     *   - `(false, mensaje)` → credenciales incorrectas o error.
     */
    fun login(
        usuarioLogin: String,
        contraseña: String,
        onResult: (success: Boolean, errorMessage: String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val usuario = repository.login(usuarioLogin, contraseña)
                if (usuario != null) {
                    onResult(true, null)
                } else {
                    onResult(false, "Usuario o contraseña incorrectos")
                }
            } catch (e: Exception) {
                onResult(false, "Error de conexión: ${e.message}")
            }
        }
    }

    /**
     * Carga todos los cobros asociados al usuario actualmente autenticado.
     *
     * Lee el `usuario_login` desde SharedPreferences, busca el ID correspondiente y
     * obtiene los [CobroDetalle] mediante el repositorio.
     *
     * El resultado se publica en el flujo [cobros]. En caso de error o ausencia de sesión,
     * se emite una lista vacía.
     *
     * @param context Contexto para acceder al usuario autenticado en SharedPreferences.
     */
    fun cargarCobrosDelUsuario(context: Context) {
        viewModelScope.launch {
            try {
                val sp = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                val usuarioLogin = sp.getString("usuario_login", "") ?: return@launch

                val usuario = repository.getAllUsuarios()
                    .find { it.usuario_login == usuarioLogin } ?: return@launch

                val cobrosDetalle = repository.getCobrosDetallePorUsuario(usuario.id_usuario).first()
                _cobros.value = cobrosDetalle
            } catch (e: Exception) {
                _cobros.value = emptyList()
            }
        }
    }
}