package com.example.butterflydetector.utils

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.butterflydetector.R

object ColorModeManager {
    private const val PREFS_NAME = "ColorModePrefs"
    private const val KEY_COLORBLIND_MODE = "colorblind_mode"

    fun isColorblindMode(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_COLORBLIND_MODE, false)
    }

    fun setColorblindMode(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_COLORBLIND_MODE, enabled).apply()
    }

    fun toggleColorblindMode(context: Context): Boolean {
        val newMode = !isColorblindMode(context)
        setColorblindMode(context, newMode)
        return newMode
    }

    // Get colors based on current mode
    fun getLogoGreen(context: Context): Int {
        return if (isColorblindMode(context)) {
            ContextCompat.getColor(context, R.color.logo_green_colorblind)
        } else {
            ContextCompat.getColor(context, R.color.logo_green)
        }
    }

    fun getLogoDarkGreen(context: Context): Int {
        return if (isColorblindMode(context)) {
            ContextCompat.getColor(context, R.color.logo_dark_green_colorblind)
        } else {
            ContextCompat.getColor(context, R.color.logo_dark_green)
        }
    }

    fun getBookPages(context: Context): Int {
        return if (isColorblindMode(context)) {
            ContextCompat.getColor(context, R.color.book_pages_colorblind)
        } else {
            ContextCompat.getColor(context, R.color.book_pages)
        }
    }

    fun getPurple700(context: Context): Int {
        return if (isColorblindMode(context)) {
            ContextCompat.getColor(context, R.color.purple_700_colorblind)
        } else {
            ContextCompat.getColor(context, R.color.purple_700)
        }
    }
}