package org.tlsys.css

import org.w3c.dom.HTMLStyleElement
import kotlin.browser.document
import kotlin.dom.Closeable

object StyleBinder {
    class Style(text: String) : Closeable {
        val style = (document.createElement("style") as HTMLStyleElement).apply {
            innerHTML = text
            type = "text/css"
            document.head!!.appendChild(this)
        }

        override fun close() {
            if (style.parentNode !== null)
                style.parentNode!!.removeChild(style)
        }
    }

    fun bind(text: String) = Style(text)

}