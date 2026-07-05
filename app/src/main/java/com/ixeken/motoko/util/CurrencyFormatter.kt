package com.ixeken.motoko.util

import java.util.Locale

/**
 * Utilidad centralizada para el formateo de montos con soporte para símbolos de moneda dinámicos
 * y respeto del modo privacidad de la aplicación.
 */
object CurrencyFormatter {
    fun format(value: Double, symbol: String, isPrivacyEnabled: Boolean, abbreviate: Boolean = false): String {
        if (isPrivacyEnabled) {
            return "$symbol • • •"
        }
        val absValue = kotlin.math.abs(value)
        val sign = if (value < 0) "-" else ""

        if (abbreviate) {
            return when {
                absValue >= 1_000_000_000.0 -> {
                    val valInB = absValue / 1_000_000_000.0
                    val formatted = if (kotlin.math.abs(valInB - kotlin.math.round(valInB)) < 0.05) {
                        String.format(Locale.US, "%.0fB", valInB)
                    } else {
                        String.format(Locale.US, "%.1fB", valInB)
                    }
                    "$sign$symbol $formatted"
                }
                absValue >= 1_000_000.0 -> {
                    val valInM = absValue / 1_000_000.0
                    val formatted = if (kotlin.math.abs(valInM - kotlin.math.round(valInM)) < 0.05) {
                        String.format(Locale.US, "%.0fM", valInM)
                    } else {
                        String.format(Locale.US, "%.1fM", valInM)
                    }
                    "$sign$symbol $formatted"
                }
                else -> {
                    val formatted = String.format(Locale.US, "%.2f", absValue)
                    "$sign$symbol $formatted"
                }
            }
        } else {
            val formatted = String.format(Locale.US, "%.2f", absValue)
            return "$sign$symbol $formatted"
        }
    }
}
