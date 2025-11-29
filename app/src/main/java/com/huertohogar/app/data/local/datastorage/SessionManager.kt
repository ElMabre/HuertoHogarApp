package com.huertohogar.app.data.local.datastorage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences // Importante para manejar errores
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch // Importante para capturar errores de lectura
import kotlinx.coroutines.flow.map
import java.io.IOException // Importante

// Extensión para crear el DataStore (Singleton)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

class SessionManager(private val context: Context) {

    companion object {
        val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        val PROFILE_IMAGE_KEY = stringPreferencesKey("profile_image_uri")

        // NUEVAS CLAVES PARA LA BASE DE DATOS REMOTA
        val USER_TOKEN_KEY = stringPreferencesKey("user_token")
        val USER_ID_KEY = longPreferencesKey("user_id")
    }

    // --- TOKEN (Para autorización en la API) ---
    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_TOKEN_KEY] = token
        }
    }

    // CORRECCIÓN CRÍTICA AQUÍ:
    val authToken: Flow<String?> = context.dataStore.data
        .catch { exception ->
            // Si ocurre un error leyendo (común al reinstalar), evitamos que la app crashee
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            // Si no hay token, devolvemos "" (vacío) en vez de null.
            // Esto le dice a AppNavigation: "Ya terminé de leer, y NO hay usuario".
            // Si devolvemos null, AppNavigation piensa: "Todavía estoy leyendo, sigue mostrando el círculo".
            preferences[USER_TOKEN_KEY] ?: ""
        }

    // --- USER ID (Para saber a quién actualizar) ---
    suspend fun saveUserId(id: Long) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = id
        }
    }

    val userId: Flow<Long?> = context.dataStore.data
        .map { preferences -> preferences[USER_ID_KEY] }

    // --- EMAIL ---
    suspend fun saveUserEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_EMAIL_KEY] = email
        }
    }

    val userEmail: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[USER_EMAIL_KEY] }

    // --- IMAGEN DE PERFIL (Local) ---
    suspend fun saveProfileImage(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[PROFILE_IMAGE_KEY] = uri
        }
    }

    val profileImage: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[PROFILE_IMAGE_KEY] }

    // --- CERRAR SESIÓN ---
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_EMAIL_KEY)
            preferences.remove(PROFILE_IMAGE_KEY)
            preferences.remove(USER_TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
        }
    }
}