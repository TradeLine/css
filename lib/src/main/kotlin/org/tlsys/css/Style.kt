package org.tlsys.css

import org.w3c.dom.HTMLStyleElement
import kotlin.browser.document

@JsName(name="N0")
object StyleBinder {
    @JsName(name="K0")
    class Style(text: String) {
        val style = (document.createElement("style") as HTMLStyleElement).apply {
            innerHTML = text
            type = "text/css"
            document.head!!.appendChild(this)
        }

        fun close() {
            if (style.parentNode !== null)
                style.parentNode!!.removeChild(style)
        }
    }

    @JsName(name="L0")
    fun bind(text: String) = Style(text)

}