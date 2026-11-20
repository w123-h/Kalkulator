package com.example.test2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Раскладка кнопок для обычного и инженерного режима.
 */
@Composable
fun KeyboardArea(
    state: CalculatorState,
    buttonHeight: Dp,
    scientific: Boolean,
    modifier: Modifier = Modifier
) {
    val buttons = if (scientific) {
        listOf(
            listOf("C", "⌫", "√", "^"),
            listOf("7", "8", "9", "/"),
            listOf("4", "5", "6", "*"),
            listOf("1", "2", "3", "-"),
            listOf("0", ",", "=", "+")
        )
    } else {
        listOf(
            listOf("C", "⌫", "/", "*"),
            listOf("7", "8", "9", "-"),
            listOf("4", "5", "6", "+"),
            listOf("1", "2", "3", "="),
            listOf("0", ",")
        )
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { label ->
                    CalculatorButton(
                        label = label,
                        height = buttonHeight,
                        modifier = Modifier.weight(1f),
                        onClick = { state.onButtonClick(label) }
                    )
                }
                // Добиваем ряд пустыми местами, чтобы кнопки не растягивались.
                repeat(4 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
