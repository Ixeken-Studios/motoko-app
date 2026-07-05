package com.ixeken.motoko.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R as LucideR
import com.ixeken.motoko.R
import com.ixeken.motoko.presentation.main.MainViewModel
import com.ixeken.motoko.ui.theme.LocalMotokoColors

// Space Mono Bold font loading read dynamically from the theme
private val SpaceMonoBoldFamily: FontFamily?
    @Composable
    get() = MaterialTheme.typography.bodyLarge.fontFamily

/**
 * ThemesHeaderContent component inside ThemesScreen.kt.
 */
@Composable
fun ThemesHeaderContent(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = LocalMotokoColors.current.primaryDark,
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .shadow(8.dp, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(LocalMotokoColors.current.textOnDark, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = LucideR.drawable.lucide_ic_arrow_left),
                    contentDescription = stringResource(id = R.string.desc_back),
                    tint = LocalMotokoColors.current.primaryDark,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = stringResource(id = R.string.themes_title),
                fontFamily = SpaceMonoBoldFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * Screen displaying the themes list options: Theme mode, Toggle colored elements, Expressive color set.
 */
@Composable
fun ThemesScreen(
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var showThemeSelectionDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LocalMotokoColors.current.primaryLight)
    ) {
        ThemesHeaderContent(onBackClick = onBackClick)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ThemeOptionNavigationCard(
                    iconResId = LucideR.drawable.lucide_ic_paintbrush,
                    title = stringResource(id = R.string.theme_mode_title),
                    subtext = stringResource(id = R.string.theme_mode_desc),
                    onClick = { showThemeSelectionDialog = true }
                )
            }

            item {
                Text(
                    text = stringResource(id = R.string.settings_sec_appearance),
                    fontFamily = SpaceMonoBoldFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp)
                )
            }

            item {
                val activeColor = if (state.coloredElementsEnabled) LocalMotokoColors.current.colorIncome else MaterialTheme.colorScheme.onSurface
                val inactiveColor = if (state.coloredElementsEnabled) LocalMotokoColors.current.colorExpense else Color(0xFFD1D5DB)
                ThemeOptionSwitchCard(
                    iconResId = LucideR.drawable.lucide_ic_sparkles,
                    title = stringResource(id = R.string.toggle_colored_elements_title),
                    subtext = stringResource(id = R.string.toggle_colored_elements_desc),
                    checked = state.coloredElementsEnabled,
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    onCheckedChange = { viewModel.setColoredElements(it) }
                )
            }


            item {
                val activeColor = if (state.coloredElementsEnabled) LocalMotokoColors.current.colorIncome else MaterialTheme.colorScheme.onSurface
                val inactiveColor = if (state.coloredElementsEnabled) LocalMotokoColors.current.colorExpense else Color(0xFFD1D5DB)
                ThemeOptionSwitchCard(
                    iconResId = LucideR.drawable.lucide_ic_sparkles,
                    title = stringResource(id = R.string.settings_animations_title),
                    subtext = stringResource(id = R.string.settings_animations_desc),
                    checked = state.animationsEnabled,
                    activeColor = activeColor,
                    inactiveColor = inactiveColor,
                    onCheckedChange = { viewModel.setAnimationsEnabled(it) }
                )
            }
        }
    }

    if (showThemeSelectionDialog) {
        ThemeSelectionDialog(
            currentMode = state.themeMode,
            onModeSelected = { viewModel.setThemeMode(it) },
            onDismiss = { showThemeSelectionDialog = false }
        )
    }
}

@Composable
private fun ThemeOptionNavigationCard(
    iconResId: Int,
    title: String,
    subtext: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LocalMotokoColors.current.surfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(LocalMotokoColors.current.primaryDark, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = SpaceMonoBoldFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtext,
                    fontFamily = SpaceMonoBoldFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    color = LocalMotokoColors.current.textMuted
                )
            }
            Icon(
                painter = painterResource(id = LucideR.drawable.lucide_ic_chevron_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ThemeOptionSwitchCard(
    iconResId: Int,
    title: String,
    subtext: String,
    checked: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    var isChecked by remember(checked) { mutableStateOf(checked) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LocalMotokoColors.current.surfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(LocalMotokoColors.current.primaryDark, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = SpaceMonoBoldFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtext,
                    fontFamily = SpaceMonoBoldFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    color = LocalMotokoColors.current.textMuted
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            ThemesCustomSwitch(
                checked = isChecked,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                modifier = Modifier.clickable {
                    val next = !isChecked
                    isChecked = next
                    onCheckedChange(next)
                }
            )
        }
    }
}

@Composable
private fun ThemesCustomSwitch(
    checked: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    modifier: Modifier = Modifier
) {
    val trackWidth = 50.dp
    val trackHeight = 28.dp
    val thumbSize = 22.dp
    val padding = 3.dp

    val alignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    val trackColor = if (checked) activeColor else inactiveColor

    Box(
        modifier = modifier
            .width(trackWidth)
            .height(trackHeight)
            .background(trackColor, RoundedCornerShape(14.dp))
            .padding(padding),
        contentAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .size(thumbSize)
                .background(Color.White, RoundedCornerShape(11.dp))
        )
    }
}
