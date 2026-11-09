package com.example.test2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.ui.theme.Test2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Test2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CalculatorScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {
    // Текущее введённое число (строка)
    var display by remember { mutableStateOf("0") }
    // Первый операнд (сохранённое число)
    var firstOperand by remember { mutableStateOf<Double?>(null) }
    // Выбранная операция
    var operation by remember { mutableStateOf<String?>(null) }
    // Флаг: ждём ввода нового числа (после нажатия оператора)
    var waitingForNewNumber by remember { mutableStateOf(false) }

    // Обработчик нажатий на кнопки
    fun onButtonClick(label: String) {
        when (label) {
            "C" -> {
                display = "0"
                firstOperand = null
                operation = null
                waitingForNewNumber = false
            }
            "+", "-", "*", "/" -> {
                firstOperand = display.toDoubleOrNull() ?: 0.0
                operation = label
                waitingForNewNumber = true
            }
            "=" -> {
                val second = display.toDoubleOrNull() ?: 0.0
                val first = firstOperand
                if (first != null && operation != null) {
                    val result = when (operation) {
                        "+" -> first + second
                        "-" -> first - second
                        "*" -> first * second
                        "/" -> if (second == 0.0) null else first / second
                        else -> null
                    }
                    display = when {
                        result == null -> "Ошибка"
                        result % 1.0 == 0.0 -> result.toLong().toString()
                        else -> result.toString()
                    }
                    firstOperand = null
                    operation = null
                    waitingForNewNumber = false
                }
            }
            "," -> {
                if (!display.contains(",")) {
                    display += ","
                    waitingForNewNumber = false
                }
            }
            else -> { // цифры 0-9
                if (waitingForNewNumber) {
                    display = label
                    waitingForNewNumber = false
                } else {
                    display = if (display == "0") label else display + label
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Дисплей
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.BottomEnd
        ) {
            Text(
                text = display,
                fontSize = 56.sp,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Кнопки калькулятора
        val buttons = listOf(
            listOf("C", "/", "*", "-"),
            listOf("7", "8", "9", "+"),
            listOf("4", "5", "6", "="),
            listOf("1", "2", "3"),
            listOf("0", ",")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { label ->
                    CalculatorButton(
                        label = label,
                        modifier = Modifier.weight(1f),
                        onClick = { onButtonClick(label) }
                    )
                }
                // Добиваем пустыми местами, если кнопок в ряду меньше 4
                repeat(4 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isOperator = label in listOf("+", "-", "*", "/", "=")
    val isClear = label == "C"

    val containerColor = when {
        isOperator -> MaterialTheme.colorScheme.primary
        isClear -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when {
        isOperator -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    Button(
        onClick = onClick,
        modifier = modifier.height(72.dp),
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CalculatorPreview() {
    Test2Theme {
        CalculatorScreen()
    }
}