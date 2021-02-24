package kr.co.petdoc.petdoc.repository.local.preference

import android.content.SharedPreferences

open class BasePreferences(
    private val prefs: SharedPreferences
) : PersistentPrefs {

    override fun getValue(name: String, defaultValue: String): String {
        return prefs.getString(name, defaultValue)?:""
    }

    override fun setValue(name: String, value: String) {
        with(prefs.edit()) {
            putString(name, value)
            apply()
        }
    }

    override fun setValueSync(name: String, value: String): Boolean {
        val sp = prefs.edit()
        sp.putString(name, value)
        return sp.commit()
    }

    override fun getValue(name: String, defaultValue: Int): Int {
        return prefs.getInt(name, defaultValue)
    }

    override fun setValue(name: String, value: Int) {
        with(prefs.edit()) {
            putInt(name, value)
            apply()
        }
    }

    override fun getValue(name: String, defaultValue: Long): Long {
        return prefs.getLong(name, defaultValue)
    }

    override fun setValue(name: String, value: Long) {
        with(prefs.edit()) {
            putLong(name, value)
            apply()
        }
    }

    override fun getValue(name: String, defaultValue: Boolean): Boolean {
        return prefs.getBoolean(name, defaultValue)
    }

    override fun setValue(name: String, value: Boolean) {
        with(prefs.edit()) {
            putBoolean(name, value)
            apply()
        }
    }

    override fun has(name: String): Boolean {
        return prefs.contains(name)
    }

    override fun clear() {
        with(prefs.edit()) {
            clear()
            apply()
        }
    }

}