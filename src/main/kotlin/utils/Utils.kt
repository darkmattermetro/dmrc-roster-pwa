package utils

import kotlinx.browser.window

fun formatDouble(value: Double, decimals: Int = 2): String {
    return js("(${value}).toFixed($decimals)").toString()
}
