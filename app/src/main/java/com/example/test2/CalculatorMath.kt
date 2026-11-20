package com.example.test2

import kotlin.math.pow
import kotlin.math.round

/**
 * Чистая арифметика и форматирование чисел — без состояния и UI.
 */
object CalculatorMath {

    /** Парсит строку с запятой в Double. Некорректный ввод даёт 0.0. */
    fun parse(str: String): Double =
        str.replace(',', '.').toDoubleOrNull() ?: 0.0

    /** Преобразует внутренний код операции в отображаемый символ (×, ÷). */
    fun symbolOf(op: String): String = when (op) {
        "*" -> "×"
        "/" -> "÷"
        "^" -> "^"
        else -> op
    }

    /** Чистая арифметика. null — ошибка (деление на ноль, 0^0, неизвестная операция). */
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

    /** Форматирует число: целые без ".0", дробные — с запятой. */
    fun formatNumber(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Ошибка"
        return if (value % 1.0 == 0.0) value.toLong().toString()
        else value.toString().replace('.', ',')
    }

    /** Форматирует результат вычисления: округляет до 10 знаков. */
    fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Ошибка"
        val rounded = round(value * 1e10) / 1e10
        return formatNumber(rounded)
    }
}
