package org.tlsys.css

import org.w3c.dom.HTMLStyleElement
import kotlin.browser.document
import kotlin.dom.Closeable

interface CssBodyProvider {
    fun generateCss(): String
}

interface CSSClassBuilder {
    fun add(name: String, f: CSSClass.() -> Unit): CSSClass
    fun template(f: CSSTemplate.() -> Unit): CSSTemplate {
        val c = ClassBuilderImp(null)
        c.f()
        return c
    }

    operator fun String.invoke(f: CSSClass.() -> Unit): CSSClass = add(this, f)
    operator fun CSSClassBuilder.unaryPlus()
}

abstract class CssSimpleClass : CssDeclaration {
    fun rgb(r: Double, g: Double, b: Double) = "rgb($r,$g,$b)"
    fun rgba(r: Double, g: Double, b: Double, a: Double) = "rgba($r,$g,$b,$a)"
}

abstract class CSSClass : CSSClassBuilder, CssSimpleClass() {

    /*
    abstract fun then(name: String, function: CSSClass.() -> Unit): CSSClass

    abstract fun child(name: String, function: CSSClass.() -> Unit): CSSClass
    abstract fun and(name: String, function: CSSClass.() -> Unit): CSSClass
    */
    abstract fun extend(template: CSSTemplate)

}

abstract class CSSTemplate : CSSClass()

interface CSSCustomBuilder : CssBodyProvider, CSSClassBuilder

object CSS {
    operator fun invoke(f: CSSClassBuilder.() -> Unit): StyleBinder.Style {
        val c = ClassBuilderImp(null)
        c.f()

        return StyleBinder.bind(c.generateCss())
    }

    private var autoGenIt = 0

    class NamedStyle(val name: String, val bind: StyleBinder.Style)

    fun style(f: CSSClass.() -> Unit): NamedStyle {
        val name = genName()
        return style(name, f)
    }

    fun style(name: String, f: CSSClass.() -> Unit): NamedStyle {
        return NamedStyle(name = name, bind = invoke {
            add(".$name", f)
        })
    }

    fun custom(): CSSCustomBuilder = ClassBuilderImp(null)
    fun genName() = "st${autoGenIt++}"
}

private open interface BaseCSSBuilder : CSSCustomBuilder {

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

private open class ClassBuilderImp(var name: String?) : CSSTemplate(), BaseCSSBuilder {
    override fun CSSClassBuilder.unaryPlus() {
        if (name !== null && name!!.startsWith(" "))
            name = name!!.substring(1)
    }

    override fun generateCss(): String {
        var l = 0
        val sb = StringBuilder()
        for (f in classes) {
            f.genCss(null) {
                sb.append(it)
            }
        }
        return sb.toString()
    }

    @JsName(name = "\$_classes")
    val classes = ArrayList<ClassBuilderImp>()

    override fun add(name: String, f: CSSClass.() -> Unit): CSSClass {
        val cb = ClassBuilderImp(" $name")
        cb.f()
        classes += cb
        return cb
    }

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
            out.append("${s.key}:${s.value};")
        }
    }

    fun genCss(self: String?, f: (String) -> Unit) {
        val body = StringBuilder()
        if (self !== null) {
            drawBody(body)
            f("$self{\n$body}\n")
        }
/*
        for (s in extends)
            s.drawBody(body)


        for (d in ands)
            d.value.genCss("$self.${d.key}", f)
        for (d in thens)
            d.value.genCss("$self${d.key}", f)
        for (d in childs)
            d.value.genCss("$self>${d.key}", f)
        */
        for (d in classes) {
            d.genCss("${self ?: ""}${d.name}", f)
        }
    }

    /*
    @JsName(name = "\$_extends")
    val extends = ArrayList<ClassBuilderImp>()
    */

    override fun extend(template: CSSTemplate) {
        val t = template as ClassBuilderImp
        classes.addAll(t.classes)
    }

    /*
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
        val cb = ClassBuilderImp(name)
        cb.f()
        thens[name] = cb
        return cb
    }



    @JsName(name = "\$_childs")
    val childs = HashMap<String, ClassBuilderImp>()

    override fun child(name: String, f: CSSClass.() -> Unit): CSSClass {
        val cb = ClassBuilderImp(name)
        cb.f()
        childs[name] = cb
        return cb
    }
    */
}