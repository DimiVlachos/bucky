package app.buckykeys.keyboard.settings.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import app.buckykeys.keyboard.settings.core.presentation.theme.BuckyTheme

@Composable
internal fun BuckyTopBar(title: String) {
    Box(
        modifier =
            Modifier.fillMaxWidth()
                .zIndex(1f)
                .shadow(8.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .statusBarsPadding()
                .padding(
                    horizontal = BuckyTheme.spacing.large,
                    vertical = BuckyTheme.spacing.medium,
                ),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
