package app.buckykeys.keyboard.settings.keyboard.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.buckykeys.keyboard.settings.R
import app.buckykeys.keyboard.settings.core.presentation.theme.BuckyTheme

@Composable
internal fun ExtraRowSaveBar(visible: Boolean, enabled: Boolean, onSave: () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .shadow(8.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(BuckyTheme.spacing.medium),
        ) {
            Button(onClick = onSave, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.settings_save))
            }
        }
    }
}
