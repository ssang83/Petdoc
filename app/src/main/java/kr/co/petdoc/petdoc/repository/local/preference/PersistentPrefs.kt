package kr.co.petdoc.petdoc.repository.local.preference

const val PREF_NAME = "petdocpreference"

interface PersistentPrefs {

    fun getValue(name: String, defaultValue: String): String

    fun setValue(name: String, value: String)

    fun setValueSync(name: String, value: String): Boolean

    fun getValue(name: String, defaultValue: Int): Int

    fun setValue(name: String, value: Int)

    fun getValue(name: String, defaultValue: Long): Long

    fun setValue(name: String, value: Long)

    fun getValue(name: String, defaultValue: Boolean): Boolean

    fun setValue(name: String, value: Boolean)

    fun has(name: String): Boolean

    fun clear()

}