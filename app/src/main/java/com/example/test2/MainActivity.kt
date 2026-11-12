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

@Stable
class CalculatorState {
    // 1. Предыдущее выражение (маленький серый)
    var expression by mutableStateOf("")
        private set

    // 2. Текущая операция
    var currentOperationSymbol by mutableStateOf("")
        private set

    // 3. Текущее число (крупный шрифт)
    var display by mutableStateOf("0")
        private set

    private var total: Double = 0.0           // накопленная сумма слагаемых
    private var term: Double = 0.0            // текущее слагаемое (* и / работают здесь)
    private var pendingAdditive: String? = null // отложенный + или -
    private var lastOp: String? = null
    private var waitingForOperand = false
    private var justEvaluated = false
    private var currentInput = "0"

    fun onDigit(digit: String) {
        if (justEvaluated) clearAll()

        if (waitingForOperand) {
            currentInput = digit
            waitingForOperand = false
        } else {
            currentInput = if (currentInput == "0") digit else currentInput + digit
        }
        display = currentInput
        updateExpression()
    }

    fun onDecimal() {
        if (justEvaluated) clearAll()

        if (waitingForOperand) {
            currentInput = "0,"
            waitingForOperand = false
        } else if (!currentInput.contains(',')) {
            currentInput += ","
        }
        display = currentInput
        updateExpression()
    }

    fun onBackspace() {
        if (justEvaluated) {
            clearAll()
            return
        }

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

        if (currentInput.length <= 1) {
            currentInput = "0"
        } else {
            currentInput = currentInput.dropLast(1)
        }
        display = currentInput
        updateExpression()
    }

    fun onClear() = clearAll()

    fun onOperator(op: String) {
        if (justEvaluated) {
            justEvaluated = false
            term = parse(currentInput)
            total = term
            pendingAdditive = null
            lastOp = null
        }

        val value = parse(currentInput)
        val isMultiplicative = op == "*" || op == "/"

        // Два оператора подряд — просто меняем
        if (waitingForOperand && lastOp != null) {
            lastOp = op
            currentOperationSymbol = symbolOf(op)
            updateExpression()
            return
        }

        // Применяем предыдущую операцию
        apply(value)

        lastOp = op
        currentOperationSymbol = symbolOf(op)

        if (isMultiplicative) {
            // * и / работают только внутри term
            // pendingAdditive не трогаем
        } else {
            // + или - : фиксируем текущий term в total
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

    fun onEquals() {
        if (lastOp == null && pendingAdditive == null) return

        val value = parse(currentInput)

        // Применяем последнюю операцию
        apply(value)

        // Финальное свёртывание отложенного +/–
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

        // Готовим к продолжению
        total = result
        term = result
        pendingAdditive = null
        lastOp = null
        waitingForOperand = true
        justEvaluated = true
    }

    // -------------------- внутренние методы --------------------

    private fun apply(value: Double) {
        when (lastOp) {
            "*", "/" -> {
                term = applyOp(term, value, lastOp!!) ?: Double.NaN
            }
            "+", "-" -> {
                // При +/– value становится новым term
                // (старый term уже был учтён при нажатии оператора)
                term = value
            }
            null -> {
                term = value
                total = value
            }
        }
    }

    private fun updateExpression() {
        expression = when {
            lastOp == null && pendingAdditive == null -> ""
            waitingForOperand -> {
                val left = if (pendingAdditive != null) formatNumber(total) else formatNumber(term)
                val op = lastOp ?: pendingAdditive
                "$left ${symbolOf(op!!)}"
            }
            else -> {
                val left = if (pendingAdditive != null) formatNumber(total) else formatNumber(term)
                val op = lastOp ?: pendingAdditive
                "$left ${symbolOf(op!!)}"
            }
        }
    }

    private fun buildFullExpression(lastValue: Double): String {
        val left = if (pendingAdditive != null) formatNumber(total) else formatNumber(term)
        val op = lastOp ?: pendingAdditive
        return if (op != null) {
            "$left ${symbolOf(op)} ${formatNumber(lastValue)}"
        } else {
            formatNumber(lastValue)
        }
    }

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

    private fun parse(str: String): Double =
        str.replace(',', '.').toDoubleOrNull() ?: 0.0

    private fun symbolOf(op: String) = when (op) {
        "*" -> "×"
        "/" -> "÷"
        else -> op
    }

    private fun applyOp(a: Double, b: Double, op: String): Double? = when (op) {
        "+" -> a + b
        "-" -> a - b
        "*" -> a * b
        "/" -> if (b == 0.0) null else a / b
        else -> null
    }

    private fun formatNumber(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Ошибка"
        return if (value % 1.0 == 0.0) value.toLong().toString()
        else value.toString().replace('.', ',')
    }

    private fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Ошибка"
        val rounded = kotlin.math.round(value * 1e10) / 1e10
        return formatNumber(rounded)
    }
}

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {
    val state = remember { CalculatorState() }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight
        val horizontalPadding = if (isLandscape) 24.dp else 16.dp
        val verticalPadding = if (isLandscape) 8.dp else 16.dp
        val buttonHeight = if (isLandscape) 48.dp else 64.dp
        val displayFontSize = if (isLandscape) 40.sp else 56.sp

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
        } else {
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
        // 1. Предыдущее выражение / число — маленький серый
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

        // 2. Текущая операция
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

        // 3. Текущее число — крупный шрифт
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
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isOperator = label in listOf("+", "-", "*", "/", "=")
    val isClear = label == "C" || label == "⌫"

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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CalculatorPreview() {
    Test2Theme {
        CalculatorScreen()
    }
}