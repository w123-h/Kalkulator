package com.example.test2

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * История последнего вычисления, текущая операция и текущее число.
 */
@Composable
fun DisplayArea(
    state: CalculatorState,
    displayFontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    // Отдельные ScrollState для истории и для числа
    val expressionScroll = rememberScrollState()
    val displayScroll = rememberScrollState()

    // Автоскролл истории вправо при изменении
    LaunchedEffect(state.expression) {
        expressionScroll.scrollTo(expressionScroll.maxValue)
    }
    // Автоскролл числа вправо при изменении
    LaunchedEffect(state.display) {
        displayScroll.scrollTo(displayScroll.maxValue)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ) {
        // Предыдущее выражение / число — маленький серый.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(expressionScroll),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = state.expression,
                fontSize = 16.sp,
                color = Color.Gray,
                maxLines = 1,
                softWrap = false
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Текущая операция — знак операции, если введена.
        Text(
            text = state.currentOperationSymbol,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Текущее число — крупный шрифт (со скроллом и автоскроллом вправо).
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(displayScroll),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = state.display,
                fontSize = displayFontSize,
                fontWeight = FontWeight.Light,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
