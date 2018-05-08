package org.tlsys.css

import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import kotlin.js.Promise

private var animIt = 0

class AnimationBuilder<T : Element>(val parent: T) {

    private interface Closeable {
        fun close()
    }

    private fun Element.on(event: String, listener: (Event) -> Unit): Closeable {
        val e: (Event) -> dynamic = {
            listener(it)
        }
        addEventListener(event, e)

        return object : Closeable {
            override fun close() {
                removeEventListener(event, e)
            }

        }
    }

    private val id = animIt++

    private val start = newArray<T.() -> Unit>()
    private val end = newArray<T.() -> Unit>()

    private val steps = newMap<Int, CssClass>()

    private val initCss = CSS.custom()
    private val stepCss = CSS.custom()


    var function = "linear"
    var duration: Int = 0

    infix fun Int.step(css: CssClass.() -> Unit) {
        step(this, css)
    }

    operator fun Int.invoke(css: CssClass.() -> Unit) {
        step(this, css)
    }


    fun step(percent: Int, text: CssClass.() -> Unit) {
        if (percent < 0 || percent > 100)
            throw IllegalArgumentException("Bad animate percent $percent")

        val g = steps[percent]
        if (g !== null)
            g.text()
        else {
            steps[percent] = stepCss.add("$percent%", text)
        }
    }

    fun start(start: CssClass.() -> Unit) {
        step(0, start)
    }

    fun end(end: CssClass.() -> Unit) {
        step(100, end)
    }

    private var initClass: CssClass? = null

    fun init(builder: CssClass.() -> Unit) {
        if (initClass === null) {
            initClass = initCss.add(".i$id", builder)
        } else {
            initClass!!.builder()
        }
    }

    private var cssBind: StyleBinder.Style? = null

    fun start() = Promise<Unit> { d, c ->
        val keysName = "k$id"
        var cssText = "@keyframes $keysName{${stepCss.generateCss()}}"
        val animName = "a$id"

        cssText += ".$animName{animation-name:$keysName; animation-duration:${duration}ms;animation-timing-function:$function;}"
        if (initClass !== null)
            cssText += "${initCss.generateCss()}"

        cssBind = StyleBinder.bind(cssText)

        var onStart: Closeable? = null
        var onStop: Closeable? = null

        onStart = parent.on("animationstart", {
            for (s in start)
                s(parent)
            onStart!!.close()
        })


        onStop = parent.on("animationend", {
            onStop!!.close()
            for (s in end)
                s(parent)

            if (initClass != null) {
                parent.classList.remove("i$id")
            }

            parent.classList.remove(animName)
            cssBind!!.close()
            d(Unit)
        })

        if (initClass != null)
            parent.classList.add("i$id")
        parent.classList.add(animName)
    }

    fun onStart(event: T.() -> Unit) {
        start.add(event)
    }

    fun onEnd(event: T.() -> Unit) {
        end.add(event)
    }
}

fun <T : HTMLElement> T.animate(build: AnimationBuilder<T>.() -> Unit): AnimationBuilder<T> {
    val ab = AnimationBuilder<T>(this)
    ab.build()
    return ab
}
