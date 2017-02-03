package org.tlsys.css

import org.w3c.dom.HTMLStyleElement
import kotlin.browser.document
import kotlin.dom.Closeable

interface CssBodyProvider {
    fun generateCss(): String
}

interface CSSClassBuilder {
    fun add(name: String, f: TreeSecretedCssClass.() -> Unit): TreeSecretedCssClass
    fun template(f: CSSTemplate.() -> Unit): CSSTemplate {
        val c = ClassBuilderImp(null)
        c.f()
        return c
    }

    operator fun String.invoke(f: TreeSecretedCssClass.() -> Unit): TreeSecretedCssClass = add(this, f)
    infix fun String.then(function: TreeSecretedCssClass.() -> Unit) = add("$$this", function)
}

interface CssClass : CssDeclaration {
    fun rgb(r: Double, g: Double, b: Double) = "rgb($r,$g,$b)"
    fun rgba(r: Double, g: Double, b: Double, a: Double) = "rgba($r,$g,$b,$a)"
}

interface SecretedCssClass : CssClass {
    var selector: String
    operator fun not(): TreeSecretedCssClass
}

interface TreeCssClass : CSSClassBuilder, CssClass {
    fun extend(template: CSSTemplate)
}

interface TreeSecretedCssClass : CSSClassBuilder, CssClass, SecretedCssClass {
    abstract fun extend(template: CSSTemplate)
    fun hover(f: TreeSecretedCssClass.() -> Unit): CSSClassBuilder {
        return add("$:hover", f)
    }
}

interface CSSTemplate : TreeCssClass

interface CSSCustomBuilder : CssBodyProvider, CSSClassBuilder

object CSS {
    operator fun invoke(f: CSSClassBuilder.() -> Unit): StyleBinder.Style {
        val c = ClassBuilderImp(null)
        c.f()

        return StyleBinder.bind(c.generateCss())
    }

    private var autoGenIt = 0

    class NamedStyle(val name: String, val bind: StyleBinder.Style)

    fun style(f: TreeSecretedCssClass.() -> Unit): NamedStyle {
        val name = genName()
        return style(name, f)
    }

    fun style(name: String, f: TreeSecretedCssClass.() -> Unit): NamedStyle {
        return NamedStyle(name = name, bind = invoke {
            add(".$name", f)
        })
    }

    fun custom(): CSSCustomBuilder = ClassBuilderImp(null)
    fun genName() = "st${autoGenIt++}"
}

private open interface BaseCSSBuilder : CSSCustomBuilder {

}
//myExample
//my-example
private fun convertProperty(str: String): String {
    var out = ""
    for (i in 0..str.length-1) {
        val c = str[i]
        val l = c.toLowerCase()
        if (c == l)
            out += c
        else
            out += "-$l"// "-" + l
    }
    return out
}

private open class ClassBuilderImp(
        @JsName(name = "\$_name")
        var name: String?) : CSSTemplate, BaseCSSBuilder, TreeSecretedCssClass {
    override var selector: String
        get() = name!!
        set(value) {
            name = value
        }

    override fun not(): TreeSecretedCssClass {
        if (name == null)
            TODO()
        name = ":not(${name!!.trimStart()})"
        return this
    }

/*
    override fun CSSClassBuilder.unaryPlus() {
        console.info("INvoke on \"$name\"")
        nextStyleIsThen = true
    }
*/
    override fun generateCss(): String {
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

    override fun add(name: String, f: TreeSecretedCssClass.() -> Unit): TreeSecretedCssClass {
        console.info("ADD \"$name\"")
        var name = name
        if (name.startsWith("$")) {
            name = name.substring(1)
        } else {
            name = " $name"
        }
        val cb = ClassBuilderImp(name)
        cb.f()
        classes += cb
        return cb
    }

    fun close() {
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
            out.append("\t${s.key}:${s.value};\n")
        }
    }

    fun genCss(self: String?, f: (String) -> Unit) {
        val body = StringBuilder()
        var selfName = "${self ?: ""}${name ?: ""}"
        if (name !== null) {
            drawBody(body)
            f("$selfName{\n$body}\n")
        }

        for (d in classes) {
            d.genCss(selfName, f)
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