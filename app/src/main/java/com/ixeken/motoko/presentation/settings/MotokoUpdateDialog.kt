package com.ixeken.motoko.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.R as LucideR
import com.ixeken.motoko.R
import com.ixeken.motoko.presentation.main.MainViewModel
import com.ixeken.motoko.presentation.main.UpdateResult
import com.ixeken.motoko.ui.theme.LocalMotokoColors

private val SpaceMonoBoldFamily: FontFamily?
    @Composable
    get() = MaterialTheme.typography.bodyLarge.fontFamily

/**
 * Diálogo de confirmación de privacidad previa a conectarse a la red para buscar actualizaciones.
 */
@Composable
fun InternetConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.settings_dialog_internet_confirm_title),
                fontFamily = SpaceMonoBoldFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = LocalMotokoColors.current.primaryDark
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.settings_dialog_internet_confirm_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalMotokoColors.current.textMuted
            )
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón Cancelar
                Box(
                    modifier = Modifier
                        .background(Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.settings_dialog_btn_cancel),
                        fontFamily = SpaceMonoBoldFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalMotokoColors.current.primaryDark
                    )
                }

                // Botón Continuar
                Box(
                    modifier = Modifier
                        .background(LocalMotokoColors.current.primaryDark, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onConfirm)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.settings_dialog_internet_btn_proceed),
                        fontFamily = SpaceMonoBoldFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalMotokoColors.current.textOnDark
                    )
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = LocalMotokoColors.current.surfaceCard
    )
}

/**
 * Diálogo de actualización multi-estado (Carga, Actualizado, Nueva Versión con Changelog y Descarga, Error).
 */
@Composable
fun MotokoUpdateDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val result = viewModel.updateResultState
    val isChecking = viewModel.isCheckingUpdates

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when {
                    isChecking -> {
                        CircularProgressIndicator(
                            color = LocalMotokoColors.current.primaryDark,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = stringResource(id = R.string.update_status_checking),
                            fontFamily = SpaceMonoBoldFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalMotokoColors.current.primaryDark
                        )
                    }
                    result is UpdateResult.UpToDate -> {
                        Icon(
                            painter = painterResource(id = LucideR.drawable.lucide_ic_sparkles),
                            contentDescription = null,
                            tint = LocalMotokoColors.current.colorIncome,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = stringResource(id = R.string.update_status_up_to_date_title),
                            fontFamily = SpaceMonoBoldFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalMotokoColors.current.primaryDark
                        )
                    }
                    result is UpdateResult.FutureVersion -> {
                        Icon(
                            painter = painterResource(id = LucideR.drawable.lucide_ic_sparkles),
                            contentDescription = null,
                            tint = LocalMotokoColors.current.colorIncome,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = stringResource(id = R.string.update_status_future_version_title),
                            fontFamily = SpaceMonoBoldFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalMotokoColors.current.primaryDark
                        )
                    }
                    result is UpdateResult.NewVersion -> {
                        Icon(
                            painter = painterResource(id = LucideR.drawable.lucide_ic_sparkles),
                            contentDescription = null,
                            tint = LocalMotokoColors.current.colorIncome,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = stringResource(id = R.string.update_status_new_version_title),
                            fontFamily = SpaceMonoBoldFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalMotokoColors.current.primaryDark
                        )
                    }
                    result is UpdateResult.Error -> {
                        Icon(
                            painter = painterResource(id = LucideR.drawable.lucide_ic_refresh_cw),
                            contentDescription = null,
                            tint = LocalMotokoColors.current.colorExpense,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = stringResource(id = R.string.update_status_error_title),
                            fontFamily = SpaceMonoBoldFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalMotokoColors.current.primaryDark
                        )
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                when {
                    isChecking -> {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    result is UpdateResult.UpToDate -> {
                        Text(
                            text = stringResource(id = R.string.update_status_up_to_date_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = LocalMotokoColors.current.textMuted
                        )
                    }
                    result is UpdateResult.FutureVersion -> {
                        Text(
                            text = stringResource(id = R.string.update_status_future_version_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = LocalMotokoColors.current.textMuted
                        )
                    }
                    result is UpdateResult.NewVersion -> {
                        Text(
                            text = "Motoko ${result.version}",
                            fontFamily = SpaceMonoBoldFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalMotokoColors.current.primaryDark
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        if (!result.changelog.isNullOrBlank()) {
                            Text(
                                text = stringResource(id = R.string.update_changelog_header),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = LocalMotokoColors.current.textMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                                    .background(LocalMotokoColors.current.primaryLight, RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                LazyColumn {
                                    item {
                                        Text(
                                            text = result.changelog,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = LocalMotokoColors.current.primaryDark
                                        )
                                    }
                                }
                            }
                        }
                    }
                    result is UpdateResult.Error -> {
                        Text(
                            text = stringResource(id = R.string.update_status_error_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = LocalMotokoColors.current.textMuted
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (result is UpdateResult.Error && !isChecking) {
                    Box(
                        modifier = Modifier
                            .background(LocalMotokoColors.current.primaryDark, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.checkUpdatesManual(context) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.update_btn_retry),
                            fontFamily = SpaceMonoBoldFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalMotokoColors.current.textOnDark
                        )
                    }
                }

                if (result is UpdateResult.NewVersion && !isChecking) {
                    Box(
                        modifier = Modifier
                            .background(LocalMotokoColors.current.colorIncome, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                uriHandler.openUri(result.downloadUrl)
                                onDismiss()
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.update_btn_download_apk),
                            fontFamily = SpaceMonoBoldFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalMotokoColors.current.primaryDark
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onDismiss)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.dialog_close),
                        fontFamily = SpaceMonoBoldFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalMotokoColors.current.primaryDark
                    )
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = LocalMotokoColors.current.surfaceCard
    )
}
