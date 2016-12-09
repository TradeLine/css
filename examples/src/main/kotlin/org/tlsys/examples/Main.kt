package org.tlsys.examples

import org.tlsys.css.CSS
import org.tlsys.css.CSSClassBuilder
import org.w3c.dom.HTMLButtonElement
import kotlin.browser.document
import kotlin.dom.Closeable
import kotlin.dom.onClick

var css: Closeable? = null

fun createCSS() {
    if (css !== null)
        css!!.close()

    css = CSS {

        val baseBtn = template {
            border = "none 0px"
        }

        add(".confirmBtn") {
            extend(baseBtn)
            backgroundColor = "gree"
        }

        add(".cancelBtn") {
            extend(baseBtn)
            backgroundColor = "#CCC"
        }

        add(".d1") {
            border = "solid 1px"
            add(".p2") {
                color = "black"
                add(".userName") {
                    color = "red"
                }
            }
        }
    }
}

fun main(args: Array<String>) {

    val btn = (document.getElementById("btn") as HTMLButtonElement)
    btn.onClick {
        if (css !== null) {
            css!!.close()
            css = null
        } else {
            createCSS()
        }
    }

    createCSS()
}