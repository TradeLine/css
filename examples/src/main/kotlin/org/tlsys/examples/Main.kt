package org.tlsys.examples

import org.tlsys.css.CSS
import org.w3c.dom.HTMLButtonElement
import kotlin.browser.document

private const val BTN_STYLE = "toggleStyle"
val css = CSS {
    ".$BTN_STYLE" then {
        backgroundColor = "#red"
    }

    val baseBtn = template {
        padding = "3px 16px 3px 16px"
        borderRadius = "4px"
        /*
        ":active" then {
            top = "1px"
            left = "1px"
            position = "relative"
        }
        */
    }

    ".confirmBtn" {
        extend(baseBtn)
        backgroundColor = "#8cc152"
        border = "1px solid #8cc152"
        color = "#fff"
    }

    ".cancelBtn" {
        extend(baseBtn)
        border = "1px solid #a9b2bd"
        backgroundColor = "#fff"
        color = "#000"
    }

    ".d1" {
        border = "solid 1px"
        ".p2" {
            color = "black"

            ">.userName" then {
                color = "red"
            }
            hover {
                color = "green"
            }
        }
    }
}

fun main(args: Array<String>) {

    val btn = (document.getElementById("btn") as HTMLButtonElement)
    btn.onclick = {
        btn.classList.toggle(BTN_STYLE)
    }
}