package com.ixeken.motoko.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class MotokoColors(
    val primaryDark: Color,
    val primaryLight: Color,
    val surfaceCard: Color,
    val activeTab: Color,
    val textMuted: Color,
    val colorLines: Color,
    val colorIncome: Color,
    val colorExpense: Color,
    val textPrimary: Color,
    val textOnDark: Color,
    val iconOnDark: Color,
    val iconOnLight: Color
) {
    companion object {
        fun fromColorScheme(scheme: ColorScheme): MotokoColors = MotokoColors(
            primaryDark = scheme.onBackground,
            primaryLight = scheme.background,
            surfaceCard = scheme.surface,
            activeTab = if (scheme.background == scheme.onBackground) scheme.outline else scheme.onSurfaceVariant,
            textMuted = scheme.onSurfaceVariant,
            colorLines = scheme.outlineVariant,
            colorIncome = ColorIncome,
            colorExpense = ColorExpense,
            textPrimary = scheme.onBackground,
            textOnDark = Color(0xFFF8F9FA),
            iconOnDark = Color(0xFFF8F9FA),
            iconOnLight = scheme.onBackground
        )
    }
}

val LocalMotokoColors = staticCompositionLocalOf {
    MotokoColors(
        primaryDark = Color(0xFF212529),
        primaryLight = Color(0xFFE9ECEF),
        surfaceCard = Color(0xFFF8F9FA),
        activeTab = Color(0xFF343A40),
        textMuted = Color(0xFF495057),
        colorLines = Color(0xFFCED4DA),
        colorIncome = Color(0xFFA7C957),
        colorExpense = Color(0xFFBC4749),
        textPrimary = Color(0xFF212529),
        textOnDark = Color(0xFFF8F9FA),
        iconOnDark = Color(0xFFF8F9FA),
        iconOnLight = Color(0xFF212529)
    )
}
