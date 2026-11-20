package com.example.test2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Настройка габаритов экрана и адаптация под ориентацию.
 */
@Composable
fun CalculatorScreen(
    modifier: Modifier = Modifier,
    scientific: Boolean = false
) {
    val state = remember { CalculatorState() }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight
        val horizontalPadding = if (isLandscape) 24.dp else 16.dp
        val verticalPadding = if (isLandscape) 8.dp else 16.dp
        val buttonHeight = if (isLandscape) 48.dp else 64.dp
        val displayFontSize = if (isLandscape) 40.sp else 56.sp

        // отображение приложения если горизонтально
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontalPadding, verticalPadding),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DisplayArea(
                    state = state,
                    displayFontSize = displayFontSize,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                KeyboardArea(
                    state = state,
                    buttonHeight = buttonHeight,
                    scientific = scientific,
                    modifier = Modifier
                        .weight(1.4f)
                        .fillMaxHeight()
                )
            }
        }
        // отображение приложения если вертикально
        else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontalPadding, verticalPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DisplayArea(
                    state = state,
                    displayFontSize = displayFontSize,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                KeyboardArea(
                    state = state,
                    buttonHeight = buttonHeight,
                    scientific = scientific,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
