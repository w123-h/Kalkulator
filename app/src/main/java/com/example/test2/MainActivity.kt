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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test2.ui.theme.Test2Theme
import kotlin.math.round

// Подключает экран
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Разрешаем контенту рисоваться под статус-баром и навигационной панелью.
        enableEdgeToEdge()
        setContent {
            Test2Theme {
                // Scaffold даёт корректные внутренние отступы (innerPadding)
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CalculatorScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// Основная логика калькулятора
@Stable
class CalculatorState {

    // 1-я строка Display: история выражения, маленький серый шрифт.
    var expression by mutableStateOf("")
        private set

    // 2-я строка Display: знак текущей операции, если она выбрана.
    var currentOperationSymbol by mutableStateOf("")
        private set

    // 3-я строка Display: текущее число/результат, крупный шрифт.
    var display by mutableStateOf("0")
        private set

    // ---------- Внутреннее состояние вычислений ----------

    private var total: Double = 0.0 // накопленная сумма слагаемых
    private var term: Double = 0.0 // текущее слагаемое
    private var pendingAdditive: String? = null // акой +/− ждёт завершения × ÷?
    private var lastOp: String? = null // Какая операция сейчас активна?
    private var waitingForOperand = false // ждём ввода нового числа
    private var justEvaluated = false // true сразу после "="
    private var currentInput = "0" // текущее вводимое число

    // Обрабатывает нажатие цифровой кнопки (0–9).
    fun onDigit(digit: String) {
        if (justEvaluated) clearAll()

        // проверяется начался набор нового числа или нет
        if (waitingForOperand) {
            currentInput = digit // начинаем новое число с этой цифры
            waitingForOperand = false // теперь число есть — больше не ждём
        } else {
            currentInput = if (currentInput == "0") digit else currentInput + digit // продолжаем текущее
        }
        display = currentInput
        updateExpression()
    }

    fun onDecimal() {
        if (justEvaluated) clearAll()

        if (waitingForOperand) {
            currentInput = "0,"
            waitingForOperand = false
        } else if (!currentInput.contains(',')) { // Защита от второй запятой в одном числе
            currentInput += ","
        }
        display = currentInput
        updateExpression()
    }

    // Обрабатывает нажатие "⌫" (backspace).
    fun onBackspace() {
        // очищаем весь калькулятор после равно
        if (justEvaluated) {
            clearAll()
            return
        }

        // Удаление оператора
        if (waitingForOperand) {
            if (lastOp != null) {
                lastOp = null
                currentOperationSymbol = ""
                waitingForOperand = false
                currentInput = formatNumber(term)
                display = currentInput
                updateExpression()
            }
            return
        }

        // удаление последней цифры
        if (currentInput.length <= 1) {
            currentInput = "0"
        } else {
            currentInput = currentInput.dropLast(1)
        }
        display = currentInput
        updateExpression()
    }

    // Полный сброс калькулятора
    fun onClear() = clearAll()

    fun onOperator(op: String) {
        // Продолжаем вычисления от результата.
        if (justEvaluated) {
            justEvaluated = false // больше не "только что был ="
            term = parse(currentInput) // результат становится текущим слагаемым
            total = term // и накопленной суммой
            pendingAdditive = null // очищаем отложенный +/-
            lastOp = null // очищаем последнюю операцию
        }

        // проверка оператора
        val value = parse(currentInput)
        val isMultiplicative = op == "*" || op == "/"

        // защита от «двойного оператора»
        if (waitingForOperand && lastOp != null) {
            lastOp = op
            currentOperationSymbol = symbolOf(op)
            updateExpression()
            return
        }

        // Применяем предыдущую операцию к введённому числу.
        apply(value)

        lastOp = op
        currentOperationSymbol = symbolOf(op)

        if (isMultiplicative) {
            // × и ÷ работают внутри term; pendingAdditive не трогаем.
        } else {
            // + или − : сворачиваем текущий term в total.
            if (pendingAdditive != null) {
                total = applyOp(total, term, pendingAdditive!!) ?: Double.NaN
            } else {
                total = term
            }
            term = total          // начинаем новое слагаемое
            pendingAdditive = op
        }

        waitingForOperand = true
        updateExpression()
    }

    // Обрабатывает нажатие "="
    fun onEquals() {
        if (lastOp == null && pendingAdditive == null) return

        val value = parse(currentInput)

        // Применяем последнюю операцию.
        apply(value)

        // Финальное свёртывание отложенного +/–.
        val result = if (pendingAdditive != null) {
            applyOp(total, term, pendingAdditive!!) ?: Double.NaN
        } else {
            term
        }

        currentInput = formatResult(result)
        display = currentInput

        // История
        expression = buildFullExpression(value)
        currentOperationSymbol = ""

        // Готовим к продолжению: результат становится текущим значением
        total = result
        term = result
        pendingAdditive = null
        lastOp = null
        waitingForOperand = true
        justEvaluated = true
    }

    // Объявляем куда положить текущее число
    private fun apply(value: Double) {
        when (lastOp) {
            "*", "/" -> {
                term = applyOp(term, value, lastOp!!) ?: Double.NaN
            }
            "+", "-" -> {
                term = value
            }
            null -> {
                term = value
                total = value
            }
        }
    }

    // обновляем отображаемое значение
    private fun updateExpression() {
        expression = when {
            lastOp == null && pendingAdditive == null -> ""
            else -> {
                val left = if (pendingAdditive != null) formatNumber(total) else formatNumber(term)
                val op = lastOp ?: pendingAdditive
                if (op != null) "$left ${symbolOf(op)}" else ""
            }
        }
    }

    // собирает полное выражение для истории
    private fun buildFullExpression(lastValue: Double): String {
        val left = if (pendingAdditive != null) formatNumber(total) else formatNumber(term)
        val op = lastOp ?: pendingAdditive
        return if (op != null) {
            "$left ${symbolOf(op)} ${formatNumber(lastValue)}"
        } else {
            formatNumber(lastValue)
        }
    }

    // Полный сброс всех полей к исходным значениям
    private fun clearAll() {
        expression = ""
        currentOperationSymbol = ""
        display = "0"
        currentInput = "0"
        total = 0.0
        term = 0.0
        pendingAdditive = null
        lastOp = null
        waitingForOperand = false
        justEvaluated = false
    }

    // Преобразует внутренний код операции в отображаемый символ (×, ÷)
    private fun symbolOf(op: String) = when (op) {
        "*" -> "×"
        "/" -> "÷"
        else -> op
    }

    // Чистая арифметика
    private fun applyOp(a: Double, b: Double, op: String): Double? = when (op) {
        "+" -> a + b
        "-" -> a - b
        "*" -> a * b
        "/" -> if (b == 0.0) null else a / b
        else -> null
    }

    // Форматирует число для отображения: целые без ".0", дробные — с запятой.
    private fun formatNumber(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Ошибка"
        return if (value % 1.0 == 0.0) value.toLong().toString()
        else value.toString().replace('.', ',')
    }

    // Форматирует результат вычисления: округляет до 10 знаков
    private fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Ошибка"
        val rounded = round(value * 1e10) / 1e10
        return formatNumber(rounded)
    }
}

// Настройка габаритов экрана
@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {
    val state = remember { CalculatorState() }

    // адаптация под ориентацию экрана
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
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// История последнего вычесления
@Composable
fun DisplayArea(
    state: CalculatorState,
    displayFontSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ) {
        // Предыдущее выражение / число — маленький серый.
        Text(
            text = state.expression,
            fontSize = 16.sp,
            color = Color.Gray,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
        )

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

        // Текущее число — крупный шрифт.
        Text(
            text = state.display,
            fontSize = displayFontSize,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun parse(str: String): Double = str.replace(',', '.').toDoubleOrNull() ?: 0.0

// Что будет на кнопке и где она будет
@Composable
fun KeyboardArea(
    state: CalculatorState,
    buttonHeight: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val buttons = listOf(
        listOf("C", "⌫", "/", "*"),
        listOf("7", "8", "9", "-"),
        listOf("4", "5", "6", "+"),
        listOf("1", "2", "3", "="),
        listOf("0", ",")
    )

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
                        onClick = {
                            when (label) {
                                "C" -> state.onClear()
                                "⌫" -> state.onBackspace()
                                "+", "-", "*", "/" -> state.onOperator(label)
                                "=" -> state.onEquals()
                                "," -> state.onDecimal()
                                else -> state.onDigit(label)
                            }
                        }
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

// Универсальная кнопка калькулятора.
@Composable
fun CalculatorButton(label: String, height: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier, onClick: () -> Unit)
{
    // Определяется тип кнопки
    val isOperator = label in listOf("+", "-", "*", "/", "=")
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

// Отображение на панельке справа
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CalculatorPreview() {
    Test2Theme {
        CalculatorScreen()
    }
}