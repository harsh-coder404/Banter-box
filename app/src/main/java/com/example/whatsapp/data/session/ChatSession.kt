package com.example.whatsapp.data.session

import android.content.Context

object ChatSession {
    private const val PREFS_NAME = "backend_session"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_PHONE = "phone"
    private const val KEY_TOKEN = "token"

    var userId: Long? = null
    var phoneNumber: String? = null
    var token: String? = null

    fun isLoggedIn(): Boolean = userId != null && !token.isNullOrBlank()

    fun load(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedUserId = prefs.getLong(KEY_USER_ID, -1L)
        userId = if (savedUserId == -1L) null else savedUserId
        phoneNumber = prefs.getString(KEY_PHONE, null)
        token = prefs.getString(KEY_TOKEN, null)
    }

    fun setSession(context: Context, userId: Long, phoneNumber: String, token: String) {
        this.userId = userId
        this.phoneNumber = phoneNumber
        this.token = token

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_PHONE, phoneNumber)
            .putString(KEY_TOKEN, token)
            .apply()
    }

    fun clear(context: Context) {
        userId = null
        phoneNumber = null
        token = null

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}

