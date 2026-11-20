package com.example.test2

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

/**
 * Обёртка с меню выбора режима (обычный / инженерный).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorApp() {
    // scientific переживает поворот (rememberSaveable)
    var scientific by rememberSaveable { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    // Выделяет место под верхнюю панель экрана
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(if (scientific) "Инженерный" else "Обычный") },
                actions = {
                    // Текстовая кнопка вместо иконки
                    TextButton(onClick = { menuExpanded = true }) {
                        Text("⋮", fontSize = 24.sp)
                    }

                    // Выпадающее меню
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Обычный калькулятор") },
                            onClick = {
                                scientific = false
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Инженерный (√, xʸ)") },
                            onClick = {
                                scientific = true
                                menuExpanded = false
                            }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        // Scaffold даёт корректные внутренние отступы (innerPadding)
        CalculatorScreen(
            modifier = Modifier.padding(innerPadding),
            scientific = scientific
        )
    }
}
