package com.ixeken.motoko.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ixeken.motoko.R

/**
 * Define las opciones de fuentes disponibles en la aplicación para facilitar extensiones futuras.
 */
enum class AppFontType {
    DEFAULT,
    SPACE_MONO,
    SPACE_GROTESK,
    DM_SANS,
    INTER,
    DARUMA_DROP_ONE,
    DELA_GOTHIC_ONE,
    DOTGOTHIC16
}

/**
 * Proveedor de familias de fuentes basado en la selección del usuario.
 */
object AppFontProvider {
    fun getFontFamily(fontType: AppFontType): FontFamily {
        return when (fontType) {
            AppFontType.DEFAULT -> FontFamily.Default
            AppFontType.SPACE_MONO -> FontFamily(
                Font(resId = R.font.space_mono_bold, weight = FontWeight.Bold),
                Font(resId = R.font.space_mono_bold, weight = FontWeight.Normal)
            )
            AppFontType.SPACE_GROTESK -> FontFamily(
                Font(resId = R.font.space_grotesk_bold, weight = FontWeight.Bold),
                Font(resId = R.font.space_grotesk_bold, weight = FontWeight.Normal)
            )
            AppFontType.DM_SANS -> FontFamily(
                Font(resId = R.font.dm_sans_bold, weight = FontWeight.Bold),
                Font(resId = R.font.dm_sans_bold, weight = FontWeight.Normal)
            )
            AppFontType.INTER -> FontFamily(
                Font(resId = R.font.inter_bold, weight = FontWeight.Bold),
                Font(resId = R.font.inter_bold, weight = FontWeight.Normal)
            )
            AppFontType.DARUMA_DROP_ONE -> FontFamily(
                Font(resId = R.font.darumadrop_one, weight = FontWeight.Bold),
                Font(resId = R.font.darumadrop_one, weight = FontWeight.Normal)
            )
            AppFontType.DELA_GOTHIC_ONE -> FontFamily(
                Font(resId = R.font.dela_gothic_one, weight = FontWeight.Bold),
                Font(resId = R.font.dela_gothic_one, weight = FontWeight.Normal)
            )
            AppFontType.DOTGOTHIC16 -> FontFamily(
                Font(resId = R.font.dotgothic16, weight = FontWeight.Bold),
                Font(resId = R.font.dotgothic16, weight = FontWeight.Normal)
            )
        }
    }
}

/**
 * Genera la configuración de tipografía Material3 adaptando todas las variantes a la fuente seleccionada.
 * Aplica un factor de escala global a fontSize y lineHeight para accesibilidad adaptativa.
 */
fun getScaledTypography(fontType: AppFontType = AppFontType.SPACE_MONO, scale: Float = 1.0f): Typography {
    val fontFamily = AppFontProvider.getFontFamily(fontType)
    return Typography(
        displayLarge = TextStyle(fontFamily = fontFamily, fontSize = (57.sp * scale), lineHeight = (64.sp * scale)),
        displayMedium = TextStyle(fontFamily = fontFamily, fontSize = (45.sp * scale), lineHeight = (52.sp * scale)),
        displaySmall = TextStyle(fontFamily = fontFamily, fontSize = (36.sp * scale), lineHeight = (44.sp * scale)),
        headlineLarge = TextStyle(fontFamily = fontFamily, fontSize = (32.sp * scale), lineHeight = (40.sp * scale)),
        headlineMedium = TextStyle(fontFamily = fontFamily, fontSize = (28.sp * scale), lineHeight = (36.sp * scale)),
        headlineSmall = TextStyle(fontFamily = fontFamily, fontSize = (24.sp * scale), lineHeight = (32.sp * scale)),
        titleLarge = TextStyle(fontFamily = fontFamily, fontSize = (22.sp * scale), lineHeight = (28.sp * scale)),
        titleMedium = TextStyle(fontFamily = fontFamily, fontSize = (16.sp * scale), lineHeight = (24.sp * scale)),
        titleSmall = TextStyle(fontFamily = fontFamily, fontSize = (14.sp * scale), lineHeight = (20.sp * scale)),
        bodyLarge = TextStyle(fontFamily = fontFamily, fontSize = (16.sp * scale), lineHeight = (24.sp * scale)),
        bodyMedium = TextStyle(fontFamily = fontFamily, fontSize = (14.sp * scale), lineHeight = (20.sp * scale)),
        bodySmall = TextStyle(fontFamily = fontFamily, fontSize = (12.sp * scale), lineHeight = (16.sp * scale)),
        labelLarge = TextStyle(fontFamily = fontFamily, fontSize = (14.sp * scale), lineHeight = (20.sp * scale)),
        labelMedium = TextStyle(fontFamily = fontFamily, fontSize = (12.sp * scale), lineHeight = (16.sp * scale)),
        labelSmall = TextStyle(fontFamily = fontFamily, fontSize = (11.sp * scale), lineHeight = (16.sp * scale))
    )
}