package main

import react.dom.client.createRoot
import kotlinx.browser.document

fun main() {
    val container = document.getElementById("root")
    if (container != null) {
        val root = createRoot(container)
        root.render(App.create())
    }
}
