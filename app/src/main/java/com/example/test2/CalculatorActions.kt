package com.example.test2

/**
 * Преобразует нажатие кнопки в действие над состоянием калькулятора.
 */
fun CalculatorState.onButtonClick(label: String) {
    when (label) {
        "C" -> onClear()
        "⌫" -> onBackspace()
        "+", "-", "*", "/" -> onOperator(label)
        "=" -> onEquals()
        "," -> onDecimal()
        "√" -> onSqrt()
        "^" -> onPower()
        else -> onDigit(label)
    }
}
