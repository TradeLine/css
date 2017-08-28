package org.tlsys.css

import org.w3c.dom.HTMLStyleElement
import kotlin.browser.document

@JsName(name = "N0")
object StyleBinder {
    @JsName(name = "K0")
    class Style(text: String) {
        val style = run {
            if (document.head === null)
                throwError("Head element is NULL")
            val style = document.createElement("style").unsafeCast<HTMLStyleElement>()
            style.innerHTML = text
            style.type = "text/css"
            document.head.asDynamic().appendChild(style)
        }

        fun close() {
            if (style.parentNode !== null)
                style.parentNode.asDynamic().removeChild(style)
        }
    }

    @JsName(name = "L0")
    fun bind(text: String) = Style(text)

}