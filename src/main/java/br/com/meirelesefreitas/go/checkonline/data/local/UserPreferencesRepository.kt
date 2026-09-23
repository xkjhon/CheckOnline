package br.com.meirelesefreitas.go.checkonline.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "checkonline_preferences")

data class UserPreferences(
    val savedMatricula: String,
    val savedSenha: String,
    val savePasswordEnabled: Boolean,
    val activeMatricula: String,
    val lastSyncTimeMillis: Long
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val SAVED_MATRICULA = stringPreferencesKey("saved_matricula")
        val SAVED_SENHA = stringPreferencesKey("saved_senha")
        val SAVE_PASSWORD_ENABLED = booleanPreferencesKey("save_password_enabled")
        val ACTIVE_MATRICULA = stringPreferencesKey("active_matricula")
        val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        val savedMatricula = preferences[PreferencesKeys.SAVED_MATRICULA] ?: ""
        val savedSenha = preferences[PreferencesKeys.SAVED_SENHA] ?: ""
        val savePasswordEnabled = preferences[PreferencesKeys.SAVE_PASSWORD_ENABLED] ?: false
        val activeMatricula = preferences[PreferencesKeys.ACTIVE_MATRICULA] ?: ""
        val lastSyncTime = preferences[PreferencesKeys.LAST_SYNC_TIME] ?: System.currentTimeMillis()

        UserPreferences(
            savedMatricula = savedMatricula,
            savedSenha = savedSenha,
            savePasswordEnabled = savePasswordEnabled,
            activeMatricula = activeMatricula,
            lastSyncTimeMillis = lastSyncTime
        )
    }

    suspend fun saveLoginCredentials(matricula: String, senha: String, savePassword: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVE_MATRICULA] = matricula
            preferences[PreferencesKeys.SAVE_PASSWORD_ENABLED] = savePassword
            if (savePassword) {
                preferences[PreferencesKeys.SAVED_MATRICULA] = matricula
                preferences[PreferencesKeys.SAVED_SENHA] = senha
            } else {
                preferences.remove(PreferencesKeys.SAVED_MATRICULA)
                preferences.remove(PreferencesKeys.SAVED_SENHA)
            }
        }
    }

    suspend fun updateLastSyncTime(timeMillis: Long = System.currentTimeMillis()) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SYNC_TIME] = timeMillis
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.ACTIVE_MATRICULA)
            val saveEnabled = preferences[PreferencesKeys.SAVE_PASSWORD_ENABLED] ?: false
            if (!saveEnabled) {
                preferences.remove(PreferencesKeys.SAVED_MATRICULA)
                preferences.remove(PreferencesKeys.SAVED_SENHA)
            }
        }
    }
}
