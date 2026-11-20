package com.example.test2

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Универсальная кнопка калькулятора.
 */
@Composable
fun CalculatorButton(
    label: String,
    height: Dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    // Определяется тип кнопки
    val isOperator = label in listOf("+", "-", "*", "/", "=", "√", "^")
    val isClear = label == "C" || label == "⌫"

    // Цвет фона кнопки
    val containerColor = when {
        isOperator -> MaterialTheme.colorScheme.primary
        isClear -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    // Цвет текста на кнопке
    val contentColor = when {
        isOperator -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    // Визуал кнопки
    Button(
        onClick = onClick,
        modifier = modifier.height(height),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text = label, fontSize = 24.sp, fontWeight = FontWeight.Medium)
    }
}
