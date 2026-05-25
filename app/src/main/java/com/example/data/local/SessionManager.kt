package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "yumaste_prefs"
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_NAME = "user_name"
    }

    fun saveSession(token: String, email: String, name: String? = null) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_EMAIL, email)
            name?.let { putString(KEY_NAME, it) }
            apply()
        }
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun getEmail(): String? {
        return prefs.getString(KEY_EMAIL, null)
    }

    fun getName(): String? {
        return prefs.getString(KEY_NAME, null)
    }

    fun clearSession() {
        prefs.edit().apply {
            remove(KEY_TOKEN)
            remove(KEY_EMAIL)
            remove(KEY_NAME)
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }
}
