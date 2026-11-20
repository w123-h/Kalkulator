package com.example.test2

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.test2.ui.theme.Test2Theme

/**
 * Предпросмотр калькулятора в Android Studio.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CalculatorPreview() {
    Test2Theme {
        CalculatorApp()
    }
}
