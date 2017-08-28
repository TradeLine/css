package org.tlsys.css

internal inline fun <T> newArray(): Array<T> = js("new Array").unsafeCast<Array<T>>()
internal inline fun <T> Array<T>.add(value: T): Unit = this.asDynamic().push(value)
internal inline fun <T> Array<T>.addAll(array: Array<out T>) {
    this.asDynamic().push.apply(this.asDynamic(), array)
}