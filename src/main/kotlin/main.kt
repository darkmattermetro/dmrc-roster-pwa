package main

import react.dom.client.createRoot
import kotlinx.browser.document
import web.dom.Element

fun main() {
    val container = document.getElementById("root")
    if (container != null) {
        val root = createRoot(container.unsafeCast<Element>())
        root.render(App.create())
    }
}
