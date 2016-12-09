package org.tlsys.examples

import org.tlsys.css.CSS
import org.tlsys.css.CSSClassBuilder
import org.tlsys.css.hover
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
            padding = "3px 16px 3px 16px"
            borderRadius = "4px"
            /*
            then(":active") {
                top = "1px"
                left = "1px"
                position = "relative"
            }
            */
        }

        add(".confirmBtn") {
            extend(baseBtn)
            backgroundColor = "#8cc152"
            border = "1px solid #8cc152"
            color = "#fff"
        }

        add(".cancelBtn") {
            extend(baseBtn)
            border = "1px solid #a9b2bd"
            backgroundColor = "#fff"
            color = "#000"
        }

        add(".d1") {
            border = "solid 1px"
            add(".p2") {
                color = "black"
                child(".userName") {
                    color = "red"
                }
                hover {
                    color = "green"
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