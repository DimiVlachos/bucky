package app.buckykeys.keyboard.settings.home.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.buckykeys.keyboard.settings.core.presentation.theme.BuckyTheme

@Composable
internal fun HomeScreen(setup: @Composable () -> Unit, settings: @Composable () -> Unit) {
    Scaffold { innerPadding ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(BuckyTheme.spacing.large),
            verticalArrangement = Arrangement.spacedBy(BuckyTheme.spacing.large),
        ) {
            setup()
            HorizontalDivider()
            settings()
        }
    }
}
