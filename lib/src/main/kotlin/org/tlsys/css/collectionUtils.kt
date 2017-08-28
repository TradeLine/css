package org.tlsys.css

internal inline fun <T> newArray(): Array<T> = js("new Array").unsafeCast<Array<T>>()
internal inline fun <T> Array<T>.add(value: T): Unit = this.asDynamic().push(value)
internal inline fun <T> Array<T>.addAll(array: Array<out T>) {
    this.asDynamic().push.apply(this.asDynamic(), array)
}

internal inline fun String.add(s: String) = asDynamic() + s

external class JStringBuilder {

}

inline fun newStringBuilder(): JStringBuilder = js("{s:\"\"}")

inline fun JStringBuilder.append(str: String) {
    this.asDynamic().s += str
}

inline fun JStringBuilder.asString(): String = this.asDynamic().s

inline fun throwError(message: String) = js("throw new Error(message)")