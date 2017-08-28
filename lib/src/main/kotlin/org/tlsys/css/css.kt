package org.tlsys.css

import org.w3c.dom.css.CSSStyleDeclaration

class SizeRect(var top: String = "", var left: String = "", var bottom: String = "", var right: String = "")

fun CssDeclaration.padding(f: SizeRect.() -> Unit) {
    val s = SizeRect()
    s.left = "0"
    s.right = "0"
    s.top = "0"
    s.bottom = "0"
    s.f()
    padding = "${s.top} ${s.right} ${s.bottom} ${s.left}"
}

fun CssDeclaration.margin(f: SizeRect.() -> Unit) {
    val s = SizeRect()
    s.f()
    margin = "${s.top} ${s.right} ${s.bottom} ${s.left}"
}

fun CSSStyleDeclaration.padding(f: SizeRect.() -> Unit) {
    val s = SizeRect()
    s.left = "0"
    s.right = "0"
    s.top = "0"
    s.bottom = "0"
    s.f()
    padding = "${s.top} ${s.right} ${s.bottom} ${s.left}"
}

fun CSSStyleDeclaration.margin(f: SizeRect.() -> Unit) {
    val s = SizeRect()
    s.f()
    margin = "${s.top} ${s.right} ${s.bottom} ${s.left}"
}


@JsName(name = "A")
interface CssBodyProvider {
    fun generateCss(): String
}

@JsName(name = "B")
interface CSSClassBuilder {
    @JsName(name = "C")
    fun add(name: String, f: TreeSecretedCssClass.() -> Unit): TreeSecretedCssClass

    @JsName(name = "D")
    fun template(f: CSSTemplate.() -> Unit): CSSTemplate {
        val c = ClassBuilderImp(null)
        c.f()
        return c
    }

    @JsName(name = "E")
    operator fun String.invoke(f: TreeSecretedCssClass.() -> Unit): TreeSecretedCssClass

    @JsName(name = "F")
    infix fun String.then(ff: TreeSecretedCssClass.() -> Unit)// = add("$$this", function)
}

@JsName(name = "J")
interface CssClass : CssDeclaration {
    @JsName(name = "H")
    fun rgb(r: Double, g: Double, b: Double): String

    @JsName(name = "J")
    fun rgba(r: Double, g: Double, b: Double, a: Double): String
}

@JsName(name = "K")
interface SecretedCssClass : CssClass {
    @JsName(name = "L")
    var selector: String

    @JsName(name = "M")
    operator fun not(): TreeSecretedCssClass
}

@JsName(name = "N")
interface TreeCssClass : CSSClassBuilder, CssClass {
    @JsName(name = "O")
    fun extend(template: CSSTemplate)
}

@JsName(name = "P")
interface TreeSecretedCssClass : CSSClassBuilder, CssClass, SecretedCssClass {
    @JsName(name = "Q")
    abstract fun extend(template: CSSTemplate)

    @JsName(name = "T")
    fun hover(f: TreeSecretedCssClass.() -> Unit): CSSClassBuilder

    @JsName(name = "TA")
    fun active(f: TreeSecretedCssClass.() -> Unit): CSSClassBuilder
}

@JsName(name = "W")
interface CSSTemplate : TreeCssClass

@JsName(name = "X")
interface CSSCustomBuilder : CssBodyProvider, CSSClassBuilder

@JsName(name = "Y")
object CSS {
    @JsName(name = "Z")
    operator fun invoke(f: CSSClassBuilder.() -> Unit): StyleBinder.Style {
        val c = ClassBuilderImp(null)
        c.f()

        return StyleBinder.bind(c.generateCss())
    }

    @JsName(name = "A0")
    private var autoGenIt = 0

    @JsName(name = "B0")
    class NamedStyle(val name: String, val bind: StyleBinder.Style)

    @JsName(name = "C0")
    fun style(f: TreeSecretedCssClass.() -> Unit): NamedStyle {
        val name = genName()
        return style(name, f)
    }

    @JsName(name = "D0")
    fun style(name: String, f: TreeSecretedCssClass.() -> Unit): NamedStyle {
        return NamedStyle(name = name, bind = invoke {
            add(".$name", f)
        })
    }

    @JsName(name = "E0")
    fun custom(): CSSCustomBuilder = ClassBuilderImp(null)

    @JsName(name = "F0")
    fun genName() = "st${autoGenIt++}"
}

@JsName(name = "G0")
private open interface BaseCSSBuilder : CSSCustomBuilder {

}

//myExample
//my-example
@JsName(name = "H0")
private fun convertProperty(str: String): String {
    var out = ""
    for (i in 0..str.length - 1) {
        val c = str[i]
        val l = c.toLowerCase()
        if (c == l)
            out += c
        else
            out += "-$l"// "-" + l
    }
    return out
}

@JsName(name = "J0")
private open class ClassBuilderImp(
        @JsName(name = "\$_Q0")
        var name: String?) : CSSTemplate, BaseCSSBuilder, TreeSecretedCssClass {
    override fun active(f: TreeSecretedCssClass.() -> Unit): CSSClassBuilder = add("$:active", f)

    override fun hover(f: TreeSecretedCssClass.() -> Unit): CSSClassBuilder = add("$:hover", f)

    override fun rgb(r: Double, g: Double, b: Double): String = "rgb($r,$g,$b)"

    override fun rgba(r: Double, g: Double, b: Double, a: Double) = "rgba($r,$g,$b,$a)"

    override fun String.invoke(f: TreeSecretedCssClass.() -> Unit): TreeSecretedCssClass = add(this, f)

    override fun String.then(ff: TreeSecretedCssClass.() -> Unit) {
        add("$$this", ff)
    }

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

    override fun generateCss(): String {
        val sb = newStringBuilder()
        for (f in classes) {
            f.genCss(null) {
                sb.append(it)
            }
        }
        return sb.asString()
    }

    @JsName(name = "\$_S0")
    val classes = newArray<ClassBuilderImp>()

    override fun add(name: String, f: TreeSecretedCssClass.() -> Unit): TreeSecretedCssClass {
        var name = name
        if (name.startsWith("$")) {
            name = name.substring(1)
        } else {
            name = " $name"
        }
        val cb = ClassBuilderImp(name)
        cb.f()
        classes.add(cb)
        return cb
    }

    @JsName(name = "W0")
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

    @JsName(name = "R0")
    fun getAllPropertys(): JMap<String, String> {
        val properys = newMap<String, String>()
        val PROPERTY_GETTER: (String, String) -> Unit = { k, v ->
            properys[convertProperty(k)] = v
        }

        val SELF_VAR = this
        js("(function(){for(var key in SELF_VAR) if (!key.startsWith('\$_')&&(typeof SELF_VAR[key] !='function')){PROPERTY_GETTER(key,SELF_VAR[key])}})()")
        return properys
    }

    @JsName(name = "T0")
    private fun drawBody(out: JStringBuilder) {
        getAllPropertys().forEach { s ->
            out.append("\t${s.key}:${s.value};\n")
        }
    }

    @JsName(name = "Y0")
    fun genCss(self: String?, f: (String) -> Unit) {
        val body = newStringBuilder()
        var selfName = "${self ?: ""}${name ?: ""}"
        if (name !== null) {
            drawBody(body)
            f("$selfName{\n${body.asString()}}\n")
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