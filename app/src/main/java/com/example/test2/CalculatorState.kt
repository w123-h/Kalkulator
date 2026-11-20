package com.example.test2

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Состояние калькулятора: иерархия операций, ввод, история и отображение.
 * Вся арифметика вынесена в [CalculatorMath].
 */
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
    private var pendingAdditive: String? = null // какой +/− ждёт завершения × ÷?
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
                currentInput = CalculatorMath.formatNumber(term)
                display = currentInput
                updateExpression()
            }
            return
        }

        // удаление последней цифры
        currentInput = if (currentInput.length <= 1) "0" else currentInput.dropLast(1)
        display = currentInput
        updateExpression()
    }

    // Полный сброс калькулятора
    fun onClear() = clearAll()

    fun onOperator(op: String) {
        // Продолжаем вычисления от результата.
        if (justEvaluated) {
            justEvaluated = false // больше не "только что был ="
            term = CalculatorMath.parse(currentInput) // результат становится текущим слагаемым
            total = term // и накопленной суммой
            pendingAdditive = null // очищаем отложенный +/-
            lastOp = null // очищаем последнюю операцию
        }

        // проверка оператора
        val value = CalculatorMath.parse(currentInput)
        val isMultiplicative = op == "*" || op == "/"

        // защита от «двойного оператора»
        if (waitingForOperand && lastOp != null) {
            lastOp = op
            currentOperationSymbol = CalculatorMath.symbolOf(op)
            updateExpression()
            return
        }

        // Применяем предыдущую операцию к введённому числу.
        apply(value)

        lastOp = op
        currentOperationSymbol = CalculatorMath.symbolOf(op)

        if (!isMultiplicative) {
            // + или − : сворачиваем текущий term в total.
            if (pendingAdditive != null) {
                total = CalculatorMath.applyOp(total, term, pendingAdditive!!) ?: Double.NaN
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

        val value = CalculatorMath.parse(currentInput)

        // Применяем последнюю операцию.
        apply(value)

        // Финальное свёртывание отложенного +/–.
        val result = if (pendingAdditive != null) {
            CalculatorMath.applyOp(total, term, pendingAdditive!!) ?: Double.NaN
        } else {
            term
        }

        currentInput = CalculatorMath.formatResult(result)
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

    /** Квадратный корень (унарная операция) */
    fun onSqrt() {
        if (justEvaluated) {
            // продолжаем от результата
            justEvaluated = false
        }

        val value = CalculatorMath.parse(currentInput)
        if (value < 0.0) {
            showError()
            return
        }

        currentInput = CalculatorMath.formatResult(kotlin.math.sqrt(value))
        display = currentInput
        // после √ можно сразу нажимать оператор
        waitingForOperand = false
        updateExpression()
    }

    /** Степень xʸ (высокий приоритет, как × и ÷) */
    fun onPower() {
        if (justEvaluated) {
            justEvaluated = false
            term = CalculatorMath.parse(currentInput)
            total = term
            pendingAdditive = null
            lastOp = null
        }

        val value = CalculatorMath.parse(currentInput)

        // защита от «двойного оператора»
        if (waitingForOperand && lastOp != null) {
            lastOp = "^"
            currentOperationSymbol = "^"
            updateExpression()
            return
        }

        apply(value)

        lastOp = "^"
        currentOperationSymbol = "^"
        waitingForOperand = true
        updateExpression()
    }

    // -------------------- внутренние методы --------------------

    // Объявляем куда положить текущее число
    private fun apply(value: Double) {
        when (lastOp) {
            "*", "/" -> term = CalculatorMath.applyOp(term, value, lastOp!!) ?: Double.NaN
            "^" -> term = CalculatorMath.applyOp(term, value, "^") ?: Double.NaN
            "+", "-" -> term = value
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
                val left = if (pendingAdditive != null) {
                    CalculatorMath.formatNumber(total)
                } else {
                    CalculatorMath.formatNumber(term)
                }
                val op = lastOp ?: pendingAdditive
                if (op != null) "$left ${CalculatorMath.symbolOf(op)}" else ""
            }
        }
    }

    // собирает полное выражение для истории
    private fun buildFullExpression(lastValue: Double): String {
        val left = if (pendingAdditive != null) {
            CalculatorMath.formatNumber(total)
        } else {
            CalculatorMath.formatNumber(term)
        }
        val op = lastOp ?: pendingAdditive
        return if (op != null) {
            "$left ${CalculatorMath.symbolOf(op)} ${CalculatorMath.formatNumber(lastValue)}"
        } else {
            CalculatorMath.formatNumber(lastValue)
        }
    }

    private fun showError() {
        currentInput = "Ошибка"
        display = "Ошибка"
        expression = ""
        currentOperationSymbol = ""
        justEvaluated = true
        waitingForOperand = true
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
}
