package com.example.test2

import kotlin.math.pow

/**
 * Чистая арифметика калькулятора — без текста и состояния.
 * Форматирование и парсинг вынесены в [TextTools].
 */
object CalculatorMath {

    /** null — ошибка: деление на ноль, 0^0 или неизвестная операция. */
    fun applyOp(a: Double, b: Double, op: String): Double? = when (op) {
        "+" -> a + b
        "-" -> a - b
        "*" -> a * b
        "/" -> if (b == 0.0) null else a / b
        "^" -> {
            if (a == 0.0 && b == 0.0) null          // 0^0
            else a.pow(b)
        }
        else -> null
    }
}
