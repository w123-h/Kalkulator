package com.example.test2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.test2.ui.theme.Test2Theme

/**
 * Точка входа: только запуск приложения.
 * Вся логика калькулятора вынесена в отдельные файлы.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Разрешаем контенту рисоваться под статус-баром и навигационной панелью.
        enableEdgeToEdge()
        setContent {
            Test2Theme {
                CalculatorApp()
            }
        }
    }
}
