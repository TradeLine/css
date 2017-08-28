package org.tlsys.css

internal inline fun <T> newArray(): Array<T> = js("new Array").unsafeCast<Array<T>>()
internal inline fun <T> Array<T>.add(value: T): Unit = this.asDynamic().push(value)
internal inline fun <T> Array<T>.addAll(array: Array<out T>) {
    this.asDynamic().push.apply(this.asDynamic(), array)
}

internal inline fun String.add(s: String) = asDynamic() + s

external class JStringBuilder {

}

inline fun newStringBuilder(): JStringBuilder {
    val o = js("new Object")
    o.s = ""
    return o
}

inline fun JStringBuilder.append(str: String) {
    this.asDynamic().s += str
}

inline fun JStringBuilder.asString(): String = this.asDynamic().s

inline fun throwError(message: String) = js("throw new Error(message)")


external class JMap<K, V>

inline fun <K, V> newMap(): JMap<K, V> = js("new Array")
inline operator fun <K, V> JMap<K, V>.get(key: K?): V? {
    val self = this
    return js("self[key]===undefined?null:self[key]")
}

inline operator fun <K, V> JMap<K, V>.set(key: K?, value: V?): Unit {
    asDynamic()[key] = value
}

external class JElement<First, Second>

inline fun <K, V> newElement(key: K, value: V?): JElement<K, V> {
    return js("[key,value]")
}

inline val <First, Second> JElement<First, Second>.key: First
    get() = asDynamic()[0]

inline val <First, Second> JElement<First, Second>.value: Second?
    get() = asDynamic()[1]

fun <K, V> JMap<K, V>.forEach(f: (JElement<K, V>) -> Unit) {
    val self = this
    js("for(var key in self) {\nf([key,self[key]])\n}")
}