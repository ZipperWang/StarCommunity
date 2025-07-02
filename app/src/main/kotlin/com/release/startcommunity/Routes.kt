package com.release.startcommunity

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first


object Routes {
    const val LOGIN    = "login"
    const val REGISTER = "register"
}

private val Context.secureStore by preferencesDataStore("secure_login")
private val KEY_USER = stringPreferencesKey("user")
private val KEY_PWD  = stringPreferencesKey("pwd")

object SecureStore {

    /** 保存（挂起函数） */
    suspend fun save(ctx: Context, user: String, pwd: String) {
        ctx.secureStore.edit { prefs ->
            prefs[KEY_USER] = user
            prefs[KEY_PWD]  = pwd
        }
    }

    /** 加载一次 */
    suspend fun load(ctx: Context): Pair<String, String>? =
        ctx.secureStore.data.first().let {
            val u = it[KEY_USER]; val p = it[KEY_PWD]
            if (u != null && p != null) u to p else null
        }

    suspend fun clear(ctx: Context) {
        ctx.secureStore.edit { it.clear() }
    }
}