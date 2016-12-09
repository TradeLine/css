package org.tlsys.css

import org.w3c.dom.HTMLStyleElement
import org.w3c.dom.css.CSSStyleDeclaration
import kotlin.browser.document
import kotlin.dom.Closeable

interface CSSRoot : CSSClassBuilder {
}

interface CSSClassBuilder {
    /*
    operator fun String.invoke(f: CSSClass.() -> Unit) {
        add(this, f)
    }
    */

    fun add(name: String, f: CSSClass.() -> Unit)
    fun template(f: CSSTemplate.() -> Unit): CSSTemplate {
        val c = ClassBuilderImp()
        c.f()
        return c
    }
}

fun CSSClass.hover(f: CSSClass.() -> Unit) {
    then(":hover", f)
}

fun CSSClass.focus(f: CSSClass.() -> Unit) {
    then(":focus", f)
}

interface CSSClass : CSSClassBuilder, CSSStyleDeclaration {

    fun then(name: String, function: CSSClass.() -> Unit): CSSClass

    fun child(name: String, function: CSSClass.() -> Unit): CSSClass
    fun and(name: String, function: CSSClass.() -> Unit): CSSClass
    fun extend(template: CSSTemplate)
}

interface CSSTemplate : CSSClass

object CSS {
    operator fun invoke(f: CSSRoot.() -> Unit): Closeable {
        val c = CSS1()
        c.f()
        c.buildCss()
        return c
    }
}

private class CSS1() : CSSClassBuilder, CSSRoot, Closeable {
    override fun add(name: String, f: CSSClass.() -> Unit) {
        val cb = ClassBuilderImp()
        cb.f()
        classes[name] = cb
    }

    val style = (document.createElement("style") as HTMLStyleElement)

    override fun close() {
        if (style.parentNode !== null)
            style.parentNode!!.removeChild(style)
    }

    private fun genBody(): String {
        var l = 0
        val sb = StringBuilder()
        for (f in classes) {
            f.value.genCss(f.key) {
                sb.append(it)
            }
        }
        return sb.toString()
    }

    fun buildCss() {
        console.info(genBody())
        style.apply {
            innerHTML = genBody()
            type = "text/css"
            document.head!!.appendChild(this)
        }
    }

    val classes = HashMap<String, ClassBuilderImp>()
}

private fun convertProperty(str: String): String {
    var out = ""
    for (i in 0..str.length) {
        val c = str[i]
        val l = c.toLowerCase()
        if (c == l) out += c else out += "-$l"
    }
    return out
}

private class ClassBuilderImp : CSSClass, CSSTemplate {
    @JsName(name = "\$_nodes")
    val nodes = HashMap<String, ClassBuilderImp>()

    override fun add(name: String, f: CSSClass.() -> Unit) {
        val cb = ClassBuilderImp()
        cb.f()
        nodes[name] = cb
    }

    fun getAllPropertys(): Map<String, String> {
        val properys = HashMap<String, String>()
        val PROPERTY_GETTER: (String, String) -> Unit = { k, v ->
            properys[convertProperty(k)] = v
        }

        val SELF_VAR = this
        js("(function(){for(var key in SELF_VAR) if (!key.startsWith('\$_')&&(typeof SELF_VAR[key] !='function')){console.info('PROPERTY! ' + key);PROPERTY_GETTER(key,SELF_VAR[key])}})()")
        return properys
    }

    private fun drawBody(out: Appendable) {
        for (s in getAllPropertys()) {
            out.append("\t${s.key}:${s.value};\n")
        }
    }

    fun genCss(self: String, f: (String) -> Unit) {
        console.dir(this)
        val body = StringBuilder()
        drawBody(body)
        for (s in extends)
            s.drawBody(body)
        f("$self {\n$body}\n")
        for (d in ands)
            d.value.genCss("$self.${d.key}", f)
        for (d in thens)
            d.value.genCss("$self${d.key}", f)
        for (d in childs)
            d.value.genCss("$self>${d.key}", f)
        for (d in nodes)
            d.value.genCss("$self ${d.key}", f)
    }

    @JsName(name = "\$_ands")
    val ands = HashMap<String, ClassBuilderImp>()

    override fun and(name: String, f: CSSClass.() -> Unit):CSSClass {
        val cb = ClassBuilderImp()
        cb.f()
        ands[name] = cb
        return cb
    }

    @JsName(name = "\$_thens")
    val thens = HashMap<String, ClassBuilderImp>()

    override fun then(name: String, f: CSSClass.() -> Unit): CSSClass {
        val cb = ClassBuilderImp()
        cb.f()
        thens[name] = cb
        return cb
    }

    @JsName(name = "\$_extends")
    val extends = ArrayList<ClassBuilderImp>()

    override fun extend(template: CSSTemplate) {
        extends += template as ClassBuilderImp
    }

    @JsName(name = "\$_childs")
    val childs = HashMap<String, ClassBuilderImp>()

    override fun child(name: String, f: CSSClass.() -> Unit):CSSClass {
        val cb = ClassBuilderImp()
        cb.f()
        childs[name] = cb
        return cb
    }
}