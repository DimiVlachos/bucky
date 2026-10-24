package app.buckykeys.keyboard.settings.home.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.stringResource
import app.buckykeys.keyboard.settings.R
import app.buckykeys.keyboard.settings.core.presentation.components.KeepFocusedVisibleState
import app.buckykeys.keyboard.settings.core.presentation.components.ProvideKeepFocusedVisible
import app.buckykeys.keyboard.settings.core.presentation.components.scrollFocusedIntoView
import app.buckykeys.keyboard.settings.core.presentation.theme.BuckyTheme
import app.buckykeys.keyboard.settings.home.presentation.components.BuckyTopBar

@Composable
internal fun HomeScreen(
    setup: @Composable () -> Unit,
    settings: @Composable () -> Unit,
    footer: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
                    ),
        ) {
            BuckyTopBar(title = stringResource(R.string.settings_title))
            val scrollState = rememberScrollState()
            val keepFocusedVisible = remember { KeepFocusedVisibleState() }
            Column(
                modifier =
                    Modifier.fillMaxWidth()
                        .weight(1f)
                        .clipToBounds()
                        .scrollFocusedIntoView(
                            keepFocusedVisible,
                            scrollState,
                            BuckyTheme.spacing.medium,
                        )
                        .verticalScroll(scrollState)
                        .padding(BuckyTheme.spacing.large),
                verticalArrangement = Arrangement.spacedBy(BuckyTheme.spacing.large),
            ) {
                ProvideKeepFocusedVisible(keepFocusedVisible) {
                    setup()
                    HorizontalDivider()
                    settings()
                    HorizontalDivider()
                    footer()
                }
            }
            Box(modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding()) {
                bottomBar()
            }
        }
    }
}
