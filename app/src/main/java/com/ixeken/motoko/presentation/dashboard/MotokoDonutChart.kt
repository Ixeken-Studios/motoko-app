package com.ixeken.motoko.presentation.dashboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import com.ixeken.motoko.ui.theme.MotokoAnimation
import androidx.compose.ui.unit.dp
import com.ixeken.motoko.ui.theme.LocalMotokoColors

/**
 * Gráfica de dona interactiva y nativa para mostrar distribución de gastos.
 *
 * @param categoryExpenses Mapa con los gastos por categoría (nombre de categoría a monto de gasto).
 * @param modifier Modificador de Compose para personalizar diseño y tamaño.
 * @param colors Lista de colores para cada sección de la dona.
 */
@Composable
fun MotokoDonutChart(
    categoryExpenses: Map<String, Double>,
    modifier: Modifier = Modifier.size(140.dp),
    colors: List<Color> = listOf(
        LocalMotokoColors.current.primaryDark,
        LocalMotokoColors.current.activeTab,
        LocalMotokoColors.current.textMuted,
        LocalMotokoColors.current.textMuted,
        Color(0xFFADB5BD),
        Color(0xFFDEE2E6)
    )
) {
    val totalExpenses = categoryExpenses.values.sum()
    
    // Animación de escala/progreso suave al cambiar de datos
    val progress = remember { Animatable(0f) }
    val animSpec = MotokoAnimation.sheetSpec<Float>()
    LaunchedEffect(categoryExpenses) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = animSpec
        )
    }

    val strokeWidthPx = with(LocalDensity.current) { 36.dp.toPx() }
    val emptySliceColor = LocalMotokoColors.current.colorLines

    Canvas(
        modifier = modifier
    ) {
        val inset = strokeWidthPx / 2
        val arcSize = androidx.compose.ui.geometry.Size(size.width - strokeWidthPx, size.height - strokeWidthPx)
        if (totalExpenses > 0f) {
            var currentStartAngle = -90f
            categoryExpenses.entries.take(6).forEachIndexed { index, entry ->
                val ratio = (entry.value / totalExpenses).toFloat()
                val sweepAngle = ratio * 360f * progress.value
                
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = currentStartAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(
                        width = strokeWidthPx,
                        cap = StrokeCap.Round
                    )
                )
                currentStartAngle += sweepAngle
            }
        } else {
            drawArc(
                color = emptySliceColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(
                    width = strokeWidthPx,
                    cap = StrokeCap.Round
                )
            )
        }
    }
}
