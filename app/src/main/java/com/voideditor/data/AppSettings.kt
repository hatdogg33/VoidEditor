package com.voideditor.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateMapOf

object AppSettings {

    private lateinit var prefs: SharedPreferences
    private val cache = mutableStateMapOf<String, Any>()

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.applicationContext
            .getSharedPreferences("voideditor_settings", Context.MODE_PRIVATE)
    }

    private fun ready(): Boolean = ::prefs.isInitialized

    fun bool(key: String, default: Boolean): Boolean {
        if (!ready()) return default
        val cached = cache[key]
        if (cached is Boolean) return cached
        val stored = prefs.getBoolean(key, default)
        cache[key] = stored
        return stored
    }

    fun putBool(key: String, value: Boolean) {
        if (!ready()) return
        cache[key] = value
        prefs.edit().putBoolean(key, value).apply()
    }

    fun int(key: String, default: Int): Int {
        if (!ready()) return default
        val cached = cache[key]
        if (cached is Int) return cached
        val stored = prefs.getInt(key, default)
        cache[key] = stored
        return stored
    }

    fun putInt(key: String, value: Int) {
        if (!ready()) return
        cache[key] = value
        prefs.edit().putInt(key, value).apply()
    }

    fun float(key: String, default: Float): Float {
        if (!ready()) return default
        val cached = cache[key]
        if (cached is Float) return cached
        val stored = prefs.getFloat(key, default)
        cache[key] = stored
        return stored
    }

    fun putFloat(key: String, value: Float) {
        if (!ready()) return
        cache[key] = value
        prefs.edit().putFloat(key, value).apply()
    }

    fun string(key: String, default: String): String {
        if (!ready()) return default
        val cached = cache[key]
        if (cached is String) return cached
        val stored = prefs.getString(key, default) ?: default
        cache[key] = stored
        return stored
    }

    fun putString(key: String, value: String) {
        if (!ready()) return
        cache[key] = value
        prefs.edit().putString(key, value).apply()
    }

    fun resetAll(specs: List<SettingSpec>) {
        for (spec in specs) {
            when (spec) {
                is BoolSpec -> putBool(spec.key, spec.default)
                is IntSpec -> putInt(spec.key, spec.default)
                is FloatSpec -> putFloat(spec.key, spec.default)
                is ChoiceSpec -> putInt(spec.key, spec.default)
                is HeaderSpec -> Unit
            }
        }
    }
}
