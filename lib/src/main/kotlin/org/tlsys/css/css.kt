package org.tlsys.css

import org.w3c.dom.HTMLStyleElement
import org.w3c.dom.css.CSSStyleDeclaration
import kotlin.browser.document
import kotlin.dom.Closeable

interface CssBodyProvider {
    fun generateCss(): String
}

interface CSSClassBuilder {
    fun add(name: String, f: CSSClass.() -> Unit): CSSClass
    fun template(f: CSSTemplate.() -> Unit): CSSTemplate {
        val c = ClassBuilderImp()
        c.f()
        return c
    }
}

interface CssSimpleClass : CSSStyleDeclaration {
    fun rgb(r: Double, g: Double, b: Double) = "rgb($r,$g,$b)"
    fun rgba(r: Double, g: Double, b: Double, a: Double) = "rgba($r,$g,$b,$a)"
}

interface CSSClass : CSSClassBuilder, CssSimpleClass {

    fun then(name: String, function: CSSClass.() -> Unit): CSSClass

    fun child(name: String, function: CSSClass.() -> Unit): CSSClass
    fun and(name: String, function: CSSClass.() -> Unit): CSSClass
    fun extend(template: CSSTemplate)

    /*
    val Float.px: String
        get() = "$this.px"

    val Float.persent: String
        get() = "$this.%"

    val Int.px: String
        get() = "$this.px"

    val Int.persent: String
        get() = "$this.%"
    */
}

interface CSSCustomBuilder : CssBodyProvider, CSSClassBuilder

interface CSSTemplate : CSSClass

object CSS {
    operator fun invoke(f: CSSClassBuilder.() -> Unit): StyleBinder.Style {
        val c = BaseCSSBuilder()
        c.f()

        return StyleBinder.bind(c.generateCss())
    }

    private var autoGenIt = 0

    class NamedStyle(val name: String, val bind: StyleBinder.Style)

    fun style(f: CSSClass.() -> Unit): NamedStyle {
        val name = "st${autoGenIt++}"
        return style(name, f)
    }

    fun style(name: String, f: CSSClass.() -> Unit): NamedStyle {
        return NamedStyle(name = name, bind = invoke {
            add(".$name", f)
        })
    }

    fun custom(): CSSCustomBuilder = BaseCSSBuilder()
}

private open class BaseCSSBuilder : CSSCustomBuilder {
    override fun generateCss(): String {
        var l = 0
        val sb = StringBuilder()
        for (f in classes) {
            f.value.genCss(f.key) {
                sb.append(it)
            }
        }
        return sb.toString()
    }

    @JsName(name = "\$_classes")
    val classes = HashMap<String, ClassBuilderImp>()

    override fun add(name: String, f: CSSClass.() -> Unit): CSSClass {
        val cb = ClassBuilderImp()
        cb.f()
        classes[name] = cb
        return cb
    }
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

private open class ClassBuilderImp : CSSClass, CSSTemplate, BaseCSSBuilder() {
    /*
    @JsName(name = "\$_nodes")
    val nodes = HashMap<String, ClassBuilderImp>()

    override fun add(name: String, f: CSSClass.() -> Unit) {
        val cb = ClassBuilderImp()
        cb.f()
        nodes[name] = cb
    }
    */

    fun getAllPropertys(): Map<String, String> {
        val properys = HashMap<String, String>()
        val PROPERTY_GETTER: (String, String) -> Unit = { k, v ->
            properys[convertProperty(k)] = v
        }

        val SELF_VAR = this
        js("(function(){for(var key in SELF_VAR) if (!key.startsWith('\$_')&&(typeof SELF_VAR[key] !='function')){PROPERTY_GETTER(key,SELF_VAR[key])}})()")
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
        for (d in classes)
            d.value.genCss("$self ${d.key}", f)
    }

    @JsName(name = "\$_ands")
    val ands = HashMap<String, ClassBuilderImp>()

    override fun and(name: String, f: CSSClass.() -> Unit): CSSClass {
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

    override fun child(name: String, f: CSSClass.() -> Unit): CSSClass {
        val cb = ClassBuilderImp()
        cb.f()
        childs[name] = cb
        return cb
    }
}