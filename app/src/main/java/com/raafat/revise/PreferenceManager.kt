package com.raafat.revise

import android.content.Context
import android.content.SharedPreferences

/**
 * Handles saving and loading user preferences
 */
class PreferenceManager(private val context: Context) {

    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
    }

    fun saveSelectedSura(position: Int) {
        preferences.edit().putInt(KEY_SELECTED_SURA, position).apply()
    }

    fun getSelectedSura(): Int {
        return preferences.getInt(KEY_SELECTED_SURA, 0)
    }

    fun saveSelectedAya(value: Float) {
        preferences.edit().putFloat(KEY_SELECTED_AYA, value).apply()
    }

    fun getSelectedAya(): Float {
        return preferences.getFloat(KEY_SELECTED_AYA, 1f)
    }

    companion object {
        private const val KEY_SELECTED_SURA = "spinner"
        private const val KEY_SELECTED_AYA = "slider"
    }
}