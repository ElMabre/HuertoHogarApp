package com.huertohogar.app.data.local.datastorage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensión para crear el DataStore.
// Al ponerlo aquí fuera de la clase, se asegura que sea único (Singleton) para el Contexto.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

class SessionManager(private val context: Context) {

    companion object {
        // Claves para guardar los datos
        val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        val PROFILE_IMAGE_KEY = stringPreferencesKey("profile_image_uri")
    }

    // --- EMAIL ---

    /**
     * Guarda el email del usuario.
     */
    suspend fun saveUserEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_EMAIL_KEY] = email
        }
    }

    /**
     * Flujo (Stream) que devuelve el email guardado.
     * Si no hay nada, devuelve null.
     */
    val userEmail: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_EMAIL_KEY]
        }

    // --- IMAGEN DE PERFIL ---

    /**
     * Guarda la URI de la foto de perfil como String.
     */
    suspend fun saveProfileImage(uri: String) {
        context.dataStore.edit { preferences ->
            preferences[PROFILE_IMAGE_KEY] = uri
        }
    }

    /**
     * Flujo que devuelve la URI de la foto.
     */
    val profileImage: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PROFILE_IMAGE_KEY]
        }

    // --- CERRAR SESIÓN ---

    /**
     * Borra todos los datos de la sesión (logout).
     */
    suspend fun clearUserEmail() {
        context.dataStore.edit { preferences ->
            // remove() es el método correcto de MutablePreferences
            preferences.remove(USER_EMAIL_KEY)
            preferences.remove(PROFILE_IMAGE_KEY)
        }
    }

    // Alias para limpiar todo (por compatibilidad si lo llamaste clearSession en otro lado)
    suspend fun clearSession() {
        clearUserEmail()
    }
}