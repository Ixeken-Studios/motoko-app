package com.ixeken.motoko.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Modifier

enum class ThemeMode { LIGHT, DARK, DARK_OLIVE, SYSTEM }

/** CompositionLocal que provee si las animaciones están habilitadas. */
val LocalAnimationsEnabled = staticCompositionLocalOf { true }

/** CompositionLocal que provee el factor de escala de tipografía activo en toda la app. */
val LocalFontScale = staticCompositionLocalOf { 1.0f }

/** Tipografía base estática sin escala aplicada (la escala se maneja via LocalDensity.fontScale). */
val AppTypography = getScaledTypography(AppFontType.SPACE_MONO, 1.0f)

object MotokoAnimation {
    val ScreenTransitionSpec: FiniteAnimationSpec<Float>
        @Composable
        get() = screenSpec()

    val SheetTransitionSpec: FiniteAnimationSpec<Float>
        @Composable
        get() = sheetSpec()

    val MicroInteractionSpec: FiniteAnimationSpec<Float>
        @Composable
        get() = microSpec()

    @Composable
    fun <T> screenSpec(): FiniteAnimationSpec<T> = if (LocalAnimationsEnabled.current) {
        tween(durationMillis = 200, easing = FastOutSlowInEasing)
    } else {
        snap()
    }

    @Composable
    fun <T> sheetSpec(): FiniteAnimationSpec<T> = if (LocalAnimationsEnabled.current) {
        spring(
            dampingRatio = 0.85f,
            stiffness = Spring.StiffnessMedium
        )
    } else {
        snap()
    }

    @Composable
    fun <T> microSpec(): FiniteAnimationSpec<T> = if (LocalAnimationsEnabled.current) {
        tween(durationMillis = 150, easing = FastOutSlowInEasing)
    } else {
        snap()
    }

    @Composable
    fun <T> pressSpring(): FiniteAnimationSpec<T> = if (LocalAnimationsEnabled.current) {
        spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
    } else {
        snap()
    }
}

/**
 * Modificador optimizado para feedback de toque mediante escala gráfica (sin recomposición de layout).
 */
@Composable
fun Modifier.bounceClick(
    enabled: Boolean = true,
    pressedScale: Float = 0.95f,
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedScale else 1.0f,
        animationSpec = MotokoAnimation.pressSpring(),
        label = "bounceClickScale"
    )

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
}

private val DarkColorScheme = darkColorScheme(
    primary = DarkHeader,
    onPrimary = DarkTextPrimary,
    primaryContainer = DarkSurface,
    onPrimaryContainer = DarkTextPrimary,
    secondary = DarkActiveTab,
    onSecondary = DarkTextPrimary,
    secondaryContainer = DarkHeader,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = DarkTextSecondary,
    onTertiary = DarkBackground,
    tertiaryContainer = DarkLines,
    onTertiaryContainer = DarkTextPrimary,
    error = DarkColorExpense,
    onError = DarkBackground,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkHeader,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkLines,
    outlineVariant = Color(0xFF43474E),
    inverseSurface = SurfaceCard,
    inverseOnSurface = PrimaryDark,
    inversePrimary = PrimaryDark,
    surfaceTint = DarkSurface
)

private val DarkOliveColorScheme = darkColorScheme(
    primary = OliveHeader,
    onPrimary = FloralWhite,
    primaryContainer = OliveSurface,
    onPrimaryContainer = FloralWhite,
    secondary = OliveActiveTab,
    onSecondary = FloralWhite,
    secondaryContainer = OliveHeader,
    onSecondaryContainer = FloralWhite,
    tertiary = OliveTextMuted,
    onTertiary = OliveBackground,
    tertiaryContainer = OliveLines,
    onTertiaryContainer = FloralWhite,
    error = DarkColorExpense,
    onError = OliveBackground,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = OliveBackground,
    onBackground = FloralWhite,
    surface = OliveSurface,
    onSurface = FloralWhite,
    surfaceVariant = OliveHeader,
    onSurfaceVariant = OliveTextMuted,
    outline = OliveLines,
    outlineVariant = Color(0xFF3A4236),
    inverseSurface = SurfaceCard,
    inverseOnSurface = PrimaryDark,
    inversePrimary = PrimaryDark,
    surfaceTint = OliveSurface
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryDark,
    onPrimary = SurfaceCard,
    primaryContainer = SurfaceCard,
    onPrimaryContainer = PrimaryDark,
    secondary = ActiveTab,
    onSecondary = SurfaceCard,
    secondaryContainer = PrimaryLight,
    onSecondaryContainer = PrimaryDark,
    tertiary = TextMuted,
    onTertiary = SurfaceCard,
    tertiaryContainer = ColorLines,
    onTertiaryContainer = PrimaryDark,
    error = ColorExpense,
    onError = SurfaceCard,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = PrimaryLight,
    onBackground = PrimaryDark,
    surface = SurfaceCard,
    onSurface = PrimaryDark,
    surfaceVariant = PrimaryLight,
    onSurfaceVariant = TextMuted,
    outline = ColorLines,
    outlineVariant = Color(0xFFDADCE0),
    inverseSurface = PrimaryDark,
    inverseOnSurface = SurfaceCard,
    inversePrimary = SurfaceCard,
    surfaceTint = SurfaceCard
)

@Composable
fun MotokoTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    appFont: String = "space_mono",
    textSizeIndex: Int = 2,
    animationsEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        themeMode == ThemeMode.DARK_OLIVE -> DarkOliveColorScheme
        themeMode == ThemeMode.DARK || (themeMode == ThemeMode.SYSTEM && isSystemInDarkTheme()) -> DarkColorScheme
        else -> LightColorScheme
    }

    val isOlive = themeMode == ThemeMode.DARK_OLIVE
    val isDark = themeMode == ThemeMode.DARK || (themeMode == ThemeMode.SYSTEM && isSystemInDarkTheme())

    val motokoColors = remember(colorScheme, isOlive, isDark) {
        if (isOlive) {
            MotokoColors(
                primaryDark = OliveBackground,
                primaryLight = OliveHeader,
                surfaceCard = OliveSurface,
                activeTab = OliveActiveTab,
                textMuted = OliveTextMuted,
                colorLines = OliveLines,
                colorIncome = DarkColorIncome,
                colorExpense = DarkColorExpense,
                textPrimary = FloralWhite,
                textOnDark = FloralWhite,
                iconOnDark = FloralWhite,
                iconOnLight = FloralWhite
            )
        } else if (isDark) {
            MotokoColors(
                primaryDark = DarkBackground,
                primaryLight = DarkHeader,
                surfaceCard = DarkSurface,
                activeTab = DarkActiveTab,
                textMuted = DarkTextMuted,
                colorLines = DarkLines,
                colorIncome = DarkColorIncome,
                colorExpense = DarkColorExpense,
                textPrimary = DarkTextPrimary,
                textOnDark = DarkTextPrimary,
                iconOnDark = DarkTextPrimary,
                iconOnLight = DarkTextPrimary
            )
        } else {
            MotokoColors(
                primaryDark = PrimaryDark,
                primaryLight = PrimaryLight,
                surfaceCard = SurfaceCard,
                activeTab = ActiveTab,
                textMuted = TextMuted,
                colorLines = ColorLines,
                colorIncome = ColorIncome,
                colorExpense = ColorExpense,
                textPrimary = PrimaryDark,
                textOnDark = SurfaceCard,
                iconOnDark = SurfaceCard,
                iconOnLight = PrimaryDark
            )
        }
    }

    val scale = remember(textSizeIndex) {
        when (textSizeIndex) {
            0 -> 0.85f
            1 -> 0.92f
            2 -> 1.0f
            3 -> 1.12f
            4 -> 1.25f
            else -> 1.0f
        }
    }

    val fontType = remember(appFont) {
        try { AppFontType.valueOf(appFont.uppercase()) } catch (e: Exception) { AppFontType.SPACE_MONO }
    }

    val typography = remember(fontType) {
        getScaledTypography(fontType, 1.0f)
    }

    val currentDensity = LocalDensity.current
    val customDensity = remember(currentDensity, scale) {
        object : Density {
            override val density: Float = currentDensity.density
            override val fontScale: Float = currentDensity.fontScale * scale
        }
    }

    CompositionLocalProvider(
        LocalMotokoColors provides motokoColors,
        LocalFontScale provides scale,
        LocalAnimationsEnabled provides animationsEnabled,
        LocalDensity provides customDensity
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}
