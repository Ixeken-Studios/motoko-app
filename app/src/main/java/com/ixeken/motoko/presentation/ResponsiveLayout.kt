package com.ixeken.motoko.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxWidth

/**
 * Retorna true si la pantalla actual tiene un ancho de 600dp o mayor (Tablets, plegables u orientación horizontal).
 */
@Composable
fun isWideScreen(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp >= 600
}

/**
 * Retorna el ancho actual de la pantalla en Dp.
 */
@Composable
fun currentScreenWidthDp(): Dp {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp.dp
}

/**
 * Modificador para centrar el contenido y limitar el ancho máximo en pantallas grandes/tablets.
 */
@Composable
fun Modifier.responsiveWidth(maxWidth: Dp = 840.dp): Modifier {
    return this
        .fillMaxWidth()
        .widthIn(max = maxWidth)
}
