package com.huertohogar.app.data.local.datastorage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences // Para manejar errores cuando no se pueden leer preferencias
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch // Para capturar errores en lectura del DataStore
import kotlinx.coroutines.flow.map
import java.io.IOException // Error común al leer DataStore cuando se reinstala la app

// Extensión que crea un DataStore asociado al contexto.
// Es un Singleton automático gracias a "preferencesDataStore".
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

class SessionManager(private val context: Context) {

    companion object {
        // Llaves para guardar valores dentro del DataStore
        val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        val PROFILE_IMAGE_KEY = stringPreferencesKey("profile_image_uri")

        // Claves nuevas para datos obtenidos desde la API remota
        val USER_TOKEN_KEY = stringPreferencesKey("user_token")
        val USER_ID_KEY = longPreferencesKey("user_id")
    }

    // --- TOKEN (Se usa para autorizaciones en la API REST) ---
    suspend fun saveAuthToken(token: String) {
        // Guardamos el token dentro del DataStore
        context.dataStore.edit { preferences ->
            preferences[USER_TOKEN_KEY] = token
        }
    }

    // Flow que expone el token actual
    val authToken: Flow<String?> = context.dataStore.data
        .catch { exception ->
            // Manejo de errores: si ocurre un IOException (muy común después de reinstalar)
            // se devuelve un conjunto de preferencias vacío para evitar que la app crashee.
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            // Si no existe un token guardado, devolvemos un String vacío.
            // Esto ayuda a la navegación de la app para saber que NO hay usuario logeado,
            // evitando estados intermedios con "null".
            preferences[USER_TOKEN_KEY] ?: ""
        }

    // --- USER ID (Identifica al usuario en la base de datos remota) ---
    suspend fun saveUserId(id: Long) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = id
        }
    }

    // Flow que devuelve el userId almacenado (o null si no existe)
    val userId: Flow<Long?> = context.dataStore.data
        .map { preferences -> preferences[USER_ID_KEY] }

    // --- EMAIL DEL USUARIO ---
    suspend fun saveUserEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_EMAIL_KEY] = email
        }
    }

    // Flujo que expone el email guardado
    val userEmail: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[USER_EMAIL_KEY] }

    // --- IMAGEN DE PERFIL (Solo la URI local) ---
    suspend fun saveProfileImage(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[PROFILE_IMAGE_KEY] = uri
        }
    }

    // Flujo que entrega la imagen de perfil guardada
    val profileImage: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[PROFILE_IMAGE_KEY] }

    // --- CERRAR SESIÓN ---
    suspend fun clearSession() {
        // Eliminamos todos los datos relacionados al usuario
        context.dataStore.edit { preferences ->
            preferences.remove(USER_EMAIL_KEY)
            preferences.remove(PROFILE_IMAGE_KEY)
            preferences.remove(USER_TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
        }
    }
}
